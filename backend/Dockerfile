# 1. 사용할 기본 이미지 (OpenJDK 17)
FROM openjdk:17-jdk-slim

# 2. 타임존 설정 추가
ENV TZ=Asia/Seoul
RUN ln -sf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 3. 작업 디렉토리 설정
WORKDIR /app

# 4. 빌드된 JAR 파일을 컨테이너 내부로 복사
COPY build/libs/*.jar app.jar

# 5. 컨테이너가 실행될 때 실행할 명령어 지정
CMD ["java", "-jar", "app.jar"]
