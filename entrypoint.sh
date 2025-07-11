#!/bin/sh

# S3에서 설정 파일 가져오기
aws s3 cp s3://itseats-config/application.yml /opt/application.yml
aws s3 cp s3://itseats-config/application-prod.yml /opt/application-prod.yml
aws s3 cp s3://itseats-config/application-jwt.yml /opt/application-jwt.yml
aws s3 cp s3://itseats-config/application-oauth.yml /opt/application-oauth.yml

# 애플리케이션 실행
exec java -jar itseats-server.jar \
  --spring.config.location=\
/opt/application.yml,\
/opt/application-prod.yml,\
/opt/application-jwt.yml,\
/opt/application-oauth.yml
