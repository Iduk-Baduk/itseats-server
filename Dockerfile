# 자바 소스를 빌드해 JAR 생성
FROM eclipse-temurin:17 AS int-build
LABEL description="itseats-server builder"

# 패키지 설치 및 클론
RUN apt-get update && apt-get install -y git && rm -rf /var/lib/apt/lists/*
RUN git clone --branch main https://github.com/Iduk-Baduk/itseats-server.git

WORKDIR /itseats-server
RUN chmod +x gradlew
RUN ./gradlew clean bootJar

# 빌드된 JAR를 경량화 이미지에 복사
FROM eclipse-temurin:17-jre
LABEL description="itseats-server application"
EXPOSE 8080

# AWS CLI 설치 (glibc 기반)
RUN apt-get update && apt-get install -y curl unzip && \
    curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip" && \
    unzip awscliv2.zip && ./aws/install && \
    rm -rf awscliv2.zip aws

COPY --from=int-build itseats-server/build/libs/itseats-server-0.0.1-SNAPSHOT.jar /opt/itseats-server.jar
COPY entrypoint.sh /opt/entrypoint.sh
RUN chmod +x /opt/entrypoint.sh

WORKDIR /opt
ENTRYPOINT ["./entrypoint.sh"]
