package com.spring.schoolmate.service;

import com.spring.schoolmate.dto.neis.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NeisApiService {
    private final WebClient webClient;

    //  NEIS API KEY
    @Value("${neis.api-key}")
    private String apiKey;

    // NEIS API URL
    @Value("${neis.base-url}")
    private String baseUrl;

    // 학교 정보
    @Value("${neis.path.school-info}")
    private String schoolInfoPath;

    // 급식 정보
    @Value("${neis.path.meal-service}")
    private String mealServicePath;

    // 학사 일정
    @Value("${neis.path.school-schedule}")
    private String schoolSchedulePath;

    // 고등학교 시간표
    @Value("${neis.path.his-timetable}")
    private String hisTimetablePath;

    // 중학교 시간표
    @Value("${neis.path.mis-timetable}")
    private String misTimetablePath;

    // 초등학교 시간표
    @Value("${neis.path.els-timetable}")
    private String elsTimetablePath;

    // 학과명
    @Value("${neis.path.school-major}")
    private String schoolMajorPath;

    // 학급정보
    @Value("${neis.path.class-info}")
    private String classInfoPath;

    /**
     * 학교 구분(일반고/특성화고)에 따라 분기하여 학과 목록을 조회하는 메서드
     */
    public List<String> findMajorsBySchoolType(String educationOfficeCode, String schoolCode) {
        log.info("학교 종류에 따른 학과 목록 조회 시작: scCode={}, schoolCode={}", educationOfficeCode, schoolCode);

        // 1. 특성화고 학과 정보 조회를 먼저 시도합니다.
        List<SchoolMajorRow> majorRows = getSchoolMajors(educationOfficeCode, schoolCode);

        // 2. 특성화고 학과 정보가 있으면 해당 정보를 가공하여 반환합니다.
        if (majorRows != null && !majorRows.isEmpty()) {
            log.info("[특성화고] schoolMajorInfo API를 통해 학과 목록을 조회합니다. ({}개)", majorRows.size());
            return majorRows.stream()
                    .map(SchoolMajorRow::getMajorName) // DTO에 학과명 필드가 DDDEP_NM 또는 유사한 이름일 수 있으니 확인 필요
                    .filter(Objects::nonNull)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
        }

        // 3. 특성화고 학과 정보가 없으면, 일반고일 가능성을 염두에 두고 기존 로직을 실행합니다.
        log.warn("[학과 조회] schoolMajorInfo API에서 학과를 찾을 수 없습니다. classInfo API로 재시도합니다. scCode={}, schoolCode={}", educationOfficeCode, schoolCode);
        List<ClassInfoRow> classInfoRows = getClassInfo(educationOfficeCode, schoolCode, "1", "고등학교", null);

        if (classInfoRows.isEmpty()) {
            log.warn("[학과 조회] 모든 API에서 학과 정보를 찾지 못했습니다. scCode={}, schoolCode={}", educationOfficeCode, schoolCode);
            return Collections.emptyList(); // 최종적으로 정보가 없으면 빈 리스트 반환
        }

        log.info("[일반고] classInfo API를 통해 학과 목록을 조회합니다.");
        return classInfoRows.stream()
                .map(ClassInfoRow::getMajorName)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * 재사용성을 위해 단일 학교 정보를 조회하는 private 헬퍼 메서드
     * schoolName 파라미터를 schoolCode로 변경하여 더 정확한 조회가 가능하도록 개선
     */
    private SchoolInfoRow getSchoolInfoRow(String educationOfficeCode, String schoolCode) {
        String url = UriComponentsBuilder.fromUriString(baseUrl + schoolInfoPath)
                .queryParam("KEY", apiKey)
                .queryParam("Type", "json")
                .queryParam("pIndex", 1)
                .queryParam("pSize", 10) // 단일 학교 조회를 목적으로 하므로 pSize를 줄임
                .queryParam("ATPT_OFCDC_SC_CODE", educationOfficeCode)
                .queryParam("SD_SCHUL_CODE", schoolCode)
                .build(true)
                .toUriString();

        SchoolInfoRes response = webClient.get().uri(url).retrieve().bodyToMono(SchoolInfoRes.class).block();

        if (response != null && response.getSchoolInfo() != null && response.getSchoolInfo().size() > 1) {
            List<SchoolInfoRow> rows = response.getSchoolInfo().get(1).getRow();
            if (rows != null && !rows.isEmpty()) {
                return rows.get(0); // 첫 번째 결과를 반환
            }
        }
        return null;
    }

    /**
     * 학교 이름과 학교급으로 NEIS에서 학교 목록을 검색합니다.
     *
     * @param schoolName  검색할 학교 이름
     * @param schoolLevel 학교종류명 ("초등학교", "중학교", "고등학교")
     * @return 검색된 학교 정보 목록
     */
    public List<SchoolInfoRow> searchSchool(String schoolName, String schoolLevel) {

        // 2. WebClient를 사용하여 NEIS API에 보낼 최종 URL을 조립
        String url = UriComponentsBuilder.fromUriString(baseUrl + schoolInfoPath)
                .queryParam("KEY", apiKey)
                .queryParam("Type", "json")
                .queryParam("pIndex", 1)
                .queryParam("pSize", 100)
                .queryParam("SCHUL_NM", schoolName)
                .queryParam("SCHUL_KND_SC_NM", schoolLevel)
                .build()
                .toUriString();
        log.info("Requesting to NEIS API with URL: {}", url);

        // 3. WebClient를 사용하여 GET 요청을 보내고, 응답을 NeisSchoolInfoResponse DTO로 받습니다.
        SchoolInfoRes response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(SchoolInfoRes.class)
                .block();
        log.info("Raw response from NEIS API: {}", response);

        if (response != null && response.getSchoolInfo() != null && !response.getSchoolInfo().isEmpty() && response.getSchoolInfo().size() > 1) {
            return Objects.requireNonNullElse(response
                            .getSchoolInfo()
                            .get(1)
                            .getRow(), Collections.emptyList());
        }
        return Collections.emptyList();
    }

    /**
     * 특정 학교의 '기간' 내 급식 정보를 조회합니다. (메소드명 변경 및 파라미터 수정)
     * @param educationOfficeCode 시도교육청코드
     * @param schoolCode 학교 행정표준코드
     * @param startDate 조회 시작일 (YYYYMMDD)
     * @param endDate 조회 종료일 (YYYYMMDD)
     * @return 급식 정보 목록
     */
    public List<MealInfoRow> getMealInfo(String educationOfficeCode, String schoolCode, String startDate, String endDate ) {
        // 1. WebClient를 사용하여 NEIS API에 보낼 최종 URL을 조립합니다.
        String url = UriComponentsBuilder.fromUriString(baseUrl + mealServicePath)
                .queryParam("KEY", apiKey)
                .queryParam("Type", "json")
                .queryParam("pIndex", 1)
                .queryParam("pSize", 100)
                .queryParam("ATPT_OFCDC_SC_CODE", educationOfficeCode)
                .queryParam("SD_SCHUL_CODE", schoolCode)
                .queryParam("MLSV_FROM_YMD", startDate)
                .queryParam("MLSV_TO_YMD", endDate)
                .build(true) // 인코딩 옵션
                .toUriString();
        log.info("Requesting NEIS API URL: {}", url);

        // 2. WebClient를 사용하여 GET 요청을 보내고, 응답을 MealServiceRes DTO로 받습니다.
        MealInfoRes response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(MealInfoRes.class)
                .block();

        if (response != null
                && response.getMealServiceDietInfo() != null
                && !response.getMealServiceDietInfo().isEmpty()
                && response.getMealServiceDietInfo().size() > 1) {
            return Objects.requireNonNullElse(
                    response.getMealServiceDietInfo()
                            .get(1).
                            getRow(), Collections.emptyList());
        }
        return Collections.emptyList();
    }

    /**
     * 시도교육청코드, 학교코드, 기간으로 NEIS에서 학사일정을 검색합니다.
     *
     * @param educationOfficeCode 시도교육청코드 (ATPT_OFCDC_SC_CODE)
     * @param schoolCode          학교 행정표준코드 (SD_SCHUL_CODE)
     * @param startDate           조회 시작 일자 (YYYYMMDD)
     * @param endDate             조회 종료 일자 (YYYYMMDD)
     * @return 검색된 학사일정 정보 목록
     */
    public List<SchoolScheduleRow> getSchoolSchedule(String educationOfficeCode,
                                                     String schoolCode,
                                                     String startDate,
                                                     String endDate) {

        // 1. WebClient를 사용하여 NEIS API에 보낼 최종 URL을 조립합니다.
        String url = UriComponentsBuilder.fromUriString(baseUrl + schoolSchedulePath)
                .queryParam("KEY", apiKey)
                .queryParam("Type", "json")
                .queryParam("pIndex", 1)
                .queryParam("pSize", 100)
                .queryParam("ATPT_OFCDC_SC_CODE", educationOfficeCode) // 시도교육청 코드
                .queryParam("SD_SCHUL_CODE", schoolCode) // 학교 코드
                .queryParam("AA_FROM_YMD", startDate) // 시작 일자
                .queryParam("AA_TO_YMD", endDate) // 종료 일자
                .build(true) // 인코딩 옵션
                .toUriString();

        log.info("Requesting NEIS API URL: {}", url);

        // 2. WebClient를 사용하여 GET 요청을 보내고, 응답을 SchoolScheduleRes DTO로 받습니다.
        SchoolScheduleRes response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(SchoolScheduleRes.class)
                .block(); // 비동기 응답을 동기적으로 기다립니다.

        // 3. 응답 결과에서 실제 데이터(row)가 있는지 확인하고 반환합니다.
        if (response == null || response.getSchoolSchedule() == null || response.getSchoolSchedule().size() < 2) {
            return Collections.emptyList();
        }
        List<SchoolScheduleRow> rows = response.getSchoolSchedule().get(1).getRow();
        return Objects.requireNonNullElse(rows, Collections.emptyList());
    }

    // 학과 정보 조회 메소드
    /**
     * NEIS API로 특정 학교의 학과 정보를 조회합니다. (주로 특성화고/마이스터고용)
     * @param educationOfficeCode 시도교육청코드
     * @param schoolCode 학교 행정표준코드
     * @return 검색된 학과 정보 목록
     */
    public List<SchoolMajorRow> getSchoolMajors(String educationOfficeCode, String schoolCode) {
        String url = UriComponentsBuilder.fromUriString(baseUrl + schoolMajorPath)
                .queryParam("KEY", apiKey)
                .queryParam("Type", "json")
                .queryParam("pIndex", 1)
                .queryParam("pSize", 100)
                .queryParam("ATPT_OFCDC_SC_CODE", educationOfficeCode)
                .queryParam("SD_SCHUL_CODE", schoolCode)
                .build(true)
                .toUriString();

        // SchoolMajorRes DTO를 사용
        SchoolMajorRes response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(SchoolMajorRes.class)
                .block();

        if (response == null || response.getSchoolMajorInfo() == null || response.getSchoolMajorInfo().size() < 2) {
            return Collections.emptyList();
        }
        List<SchoolMajorRow> rows = response.getSchoolMajorInfo().get(1).getRow();
        return Objects.requireNonNullElse(rows, Collections.emptyList());
    }

    /**
     * NEIS API로 특정 학교, 특정 학년의 반 목록 정보를 조회합니다.
     * @param educationOfficeCode 시도교육청코드
     * @param schoolCode 학교 행정표준코드
     * @param grade 학년
     * @return 검색된 반 정보 목록 (예: "1", "2", "3"...)
     */
    public List<ClassInfoRow> getClassInfo(String educationOfficeCode, String schoolCode, String grade, String schoolLevel, String majorName) {
        log.info("학급정보 API 호출: scCode={}, schoolCode={}, grade={}, level={}, majorName={}",
                educationOfficeCode, schoolCode, grade, schoolLevel, majorName);

        // [핵심 1] NEIS API에 요청할 때는 '학과(DDDEP_NM)' 파라미터를 아예 보내지 않습니다.
        // 해당 학년의 모든 반 정보를 일단 전부 다 받아옵니다.
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl + classInfoPath)
                .queryParam("KEY", apiKey)
                .queryParam("Type", "json")
                .queryParam("pIndex", 1)
                .queryParam("pSize", 100) // 반 개수가 100개를 넘는 경우는 거의 없으므로 100으로 설정
                .queryParam("ATPT_OFCDC_SC_CODE", educationOfficeCode)
                .queryParam("SD_SCHUL_CODE", schoolCode)
                .queryParam("AY", String.valueOf(LocalDate.now().getYear()))
                .queryParam("GRADE", grade);

        String url = builder.build().encode(StandardCharsets.UTF_8).toUriString();
        log.info("... 최종 요청 URL (학과 필터링 없음): {}", url);

        ClassInfoRes response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(ClassInfoRes.class)
                .block();

        // API 응답이 없거나 비어있으면 빈 리스트 반환
        if (response == null || response.getClassInfo() == null || response.getClassInfo().isEmpty() || response.getClassInfo().size() < 2) {
            return Collections.emptyList();
        }

        // 전체 반 목록을 가져옴
        List<ClassInfoRow> allClassRows = response.getClassInfo().get(1).getRow();
        if (allClassRows == null || allClassRows.isEmpty()) {
            return Collections.emptyList();
        }

        // [핵심 2] 반 이름을 숫자  로 변환하여 오름차순으로 정렬합니다. (예: 1, 2, ... 10, 11)
        allClassRows.sort(Comparator.comparingInt(row -> {
            try {
                return Integer.parseInt(row.getClassName());
            } catch (NumberFormatException e) {
                return 0; // 숫자로 변환할 수 없는 반 이름은 맨 앞으로 정렬
            }
        }));
        log.info("... 반 목록 정렬 완료. ({}개)", allClassRows.size());

        // [핵심 3] 고등학교이고 학과명이 주어진 경우, 백엔드에서 직접 필터링합니다.
        if ("고등학교".equals(schoolLevel) && majorName != null && !majorName.isBlank()) {
            log.info("... '{}' 학과로 필터링 시작", majorName);
            return allClassRows.stream()
                    .filter(row -> majorName.equals(row.getMajorName()))
                    .collect(Collectors.toList());
        }

        // 초/중학교이거나, 고등학교지만 학과 필터링이 필요 없는 경우 정렬된 전체 목록 반환
        return allClassRows;
    }

    /**
     * 학교급에 맞는 시간표 정보를 조회합니다.
     */
    public List<TimetableRes> getTimetable(String schoolLevel, String educationOfficeCode, String schoolCode, String startDate, String endDate, String grade, String classNo, String majorName) {
        String url = createTimetableUrl(schoolLevel, educationOfficeCode, schoolCode, startDate, endDate, grade, classNo, majorName);
        log.info(">>>> 시간표 최종 요청 URL: {}", url);

        switch (schoolLevel) {
            case "초등학교":
                ElsTimetableRes elsResponse = webClient.get().uri(url).retrieve().bodyToMono(ElsTimetableRes.class).block();
                if (elsResponse != null && elsResponse.getElsTimetable() != null && elsResponse.getElsTimetable().size() > 1) {
                    List<ElsTimetableRow> rows = elsResponse.getElsTimetable().get(1).getRow();
                    return rows.stream()
                            .map(row -> TimetableRes.builder()
                                    .timetableDate(row.getTimetableDate())
                                    .schoolName(row.getSchoolName())
                                    .period(row.getPeriod())
                                    .subjectName(row.getSubjectName())
                                    .build())
                            .collect(Collectors.toList());
                }
                break;
            case "중학교":
                MisTimetableRes misResponse = webClient.get().uri(url).retrieve().bodyToMono(MisTimetableRes.class).block();
                if (misResponse != null && misResponse.getMisTimetable() != null && misResponse.getMisTimetable().size() > 1) {
                    List<MisTimetableRow> rows = misResponse.getMisTimetable().get(1).getRow();
                    return rows.stream()
                            .map(row -> TimetableRes.builder()
                                    .timetableDate(row.getTimetableDate())
                                    .schoolName(row.getSchoolName())
                                    .period(row.getPeriod())
                                    .subjectName(row.getSubjectName())
                                    .build())
                            .collect(Collectors.toList());
                }
                break;
            case "고등학교":
                HisTimetableRes hisResponse = webClient.get().uri(url).retrieve().bodyToMono(HisTimetableRes.class).block();
                if (hisResponse != null && hisResponse.getHisTimetable() != null && hisResponse.getHisTimetable().size() > 1) {
                    List<HisTimetableRow> rows = hisResponse.getHisTimetable().get(1).getRow();
                    return rows.stream()
                            .map(row -> TimetableRes.builder()
                                    .timetableDate(row.getTimetableDate())
                                    .schoolName(row.getSchoolName())
                                    .departmentName(row.getDepartmentName())
                                    .period(row.getPeriod())
                                    .subjectName(row.getSubjectName())
                                    .build())
                            .collect(Collectors.toList());
                }
                break;
            default:
                throw new IllegalArgumentException("잘못된 학교급 정보입니다: " + schoolLevel);
        }

        HisTimetableRes hisResponse = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(HisTimetableRes.class)
                .block();

        log.info("NEIS Timetable raw response: {}", hisResponse);
        return Collections.emptyList();
    }

    /**
     * 시간표 조회를 위한 NEIS API URL을 생성하는 공통 로직입니다.
     * (범위 조회를 지원하도록 수정)
     */
    private String createTimetableUrl(String schoolLevel, String educationOfficeCode, String schoolCode, String startDate, String endDate, String grade, String classNo, String majorName) {
        String path;
        switch (schoolLevel) {
            case "초등학교" -> path = elsTimetablePath;
            case "중학교" -> path = misTimetablePath;
            case "고등학교" -> path = hisTimetablePath;
            default -> throw new IllegalArgumentException("잘못된 학교급 정보입니다: " + schoolLevel);
        }
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl + path)
                .queryParam("KEY", apiKey)
                .queryParam("Type", "json")
                .queryParam("pIndex", 1)
                .queryParam("pSize", 100)
                .queryParam("ATPT_OFCDC_SC_CODE", educationOfficeCode)
                .queryParam("SD_SCHUL_CODE", schoolCode)
                .queryParam("TI_FROM_YMD", startDate)
                .queryParam("TI_TO_YMD", endDate)
                .queryParam("GRADE", grade)
                .queryParam("CLASS_NM", classNo);

        // 고등학교의 경우에만 학과명 파라미터를 추가합니다.
        if ("고등학교".equals(schoolLevel) && majorName != null && !majorName.isBlank() && !majorName.equals("일반학과")) {
            builder.queryParam("DDDEP_NM", majorName);
        }

        return builder.encode(StandardCharsets.UTF_8).build().toUriString();
    }
}