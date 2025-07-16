#!/bin/sh

# S3에서 설정 파일 가져오기
#aws s3 cp s3://itseats-config/application.yml /opt/application.yml
#aws s3 cp s3://itseats-config/application-prod.yml /opt/application-prod.yml
#aws s3 cp s3://itseats-config/application-jwt.yml /opt/application-jwt.yml
#aws s3 cp s3://itseats-config/application-oauth.yml /opt/application-oauth.yml

aws --endpoint-url https://s3.studio1122.net \
  s3 cp s3://itseats-config/application.yml /opt/application.yml \
  --no-sign-request
aws --endpoint-url https://s3.studio1122.net \
  s3 cp s3://itseats-config/application-prod.yml /opt/application-prod.yml \
  --no-sign-request
aws --endpoint-url https://s3.studio1122.net \
  s3 cp s3://itseats-config/application-jwt.yml /opt/application-jwt.yml \
  --no-sign-request
aws --endpoint-url https://s3.studio1122.net \
  s3 cp s3://itseats-config/application-oauth.yml /opt/application-oauth.yml \
  --no-sign-request

# 애플리케이션 실행
exec java -Duser.timezone=Asia/Seoul -jar itseats-server.jar \
  --spring.config.location=\
/opt/application.yml,\
/opt/application-prod.yml,\
/opt/application-jwt.yml,\
/opt/application-oauth.yml
