# Dockerfile

# ----------------------------------------------------
# 1단계: 빌드 (JDK 17)
FROM openjdk:17-jdk-slim AS build

# Gradle wrapper와 설정 파일 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 소스 코드 복사
COPY src src

# Gradle 캐시 파일 다운로드 (빌드 속도 향상)
RUN ./gradlew dependencies

# JAR 파일 빌드
RUN ./gradlew bootJar

# ----------------------------------------------------
# 2단계: 실행 (JRE 17)
FROM openjdk:17-jre-slim

EXPOSE 9000
WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# GCP 인증 문제 해결: config 폴더와 키 파일을 컨테이너 내부로 복사
COPY src/main/resources/config /app/config

# GCP Vision AI 및 Firebase 인증: 환경 변수로 키 파일 경로 지정
# Cloud Run에서 이 경로를 자동으로 인식하여 인증.
ENV GOOGLE_APPLICATION_CREDENTIALS=/app/config/gcp-service-account.json

ENTRYPOINT ["java", "-jar", "app.jar"]