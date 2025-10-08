# Dockerfile

# ----------------------------------------------------
# 1단계: 빌드 (JDK 17)
FROM eclipse-temurin:17-jdk-alpine AS build

# 빌드 작업 디렉토리를 /app으로 설정합니다. (복사 경로 통일)
WORKDIR /app

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
# 태그를 안정적인 eclipse-temurin으로 변경합니다.
# FROM openjdk:17-jre-slim-buster
FROM eclipse-temurin:17-jre-alpine

EXPOSE 9000
WORKDIR /app

# 빌드된 JAR 파일 복사 (1단계에서 WORKDIR을 /app으로 설정했으므로 경로 수정)
COPY --from=build /app/build/libs/*.jar app.jar

# GCP 인증 문제 해결: config 폴더와 키 파일을 컨테이너 내부로 복사
COPY src/main/resources/config /app/config

# GCP Vision AI 및 Firebase 인증: 환경 변수로 키 파일 경로 지정
# Cloud Run에서 이 경로를 자동으로 인식하여 인증.
ENV GOOGLE_APPLICATION_CREDENTIALS=/app/config/gcp-service-account.json

# Spring Boot 앱의 포트를 9000으로 명시합니다.
ENTRYPOINT ["java", "-jar", "app.jar"]