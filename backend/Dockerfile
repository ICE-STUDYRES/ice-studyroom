# 1. 사용할 기본 이미지 (OpenJDK 17 JRE)
FROM eclipse-temurin:17-jre-alpine

# 2. 필수 라이브러리 추가 (C++ 표준 라이브러리)
RUN apk add --no-cache libstdc++

# 3. 타임존 설정 추가
ENV TZ=Asia/Seoul
RUN apk add --no-cache tzdata && \
    ln -sf /usr/share/zoneinfo/$TZ /etc/localtime && \
    echo $TZ > /etc/timezone

# 4. 작업 디렉토리 설정
WORKDIR /app
RUN mkdir -p logs

# 5. CI/CD에서 빌드된 JAR 파일을 컨테이너 내부로 복사
COPY build/libs/*.jar app.jar

# 5. 컨테이너 실행 명령어 지정
CMD ["java", "-jar", "app.jar"]
