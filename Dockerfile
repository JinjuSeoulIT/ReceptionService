# Java 17 런타임 (가볍고 안정)
FROM eclipse-temurin:17-jre

# 컨테이너 내부 작업 디렉터리
WORKDIR /app

# 빌드된 JAR 복사
COPY build/libs/reception-0.0.1-SNAPSHOT.jar app.jar

# Spring Boot 포트
EXPOSE 8181

# 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
