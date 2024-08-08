#!/bin/bash

# 변수 설정
REPO_PATH="/repository/spring-gift-point"
BUILD_PATH="$REPO_PATH/build/libs"
DEPLOY_PATH="/home/ubuntu/"

# 환경 변수 설정
KAKAO_CLIENT_ID="4369468b478c9011e369b969b4605fe4"
KAKAO_REDIRECT_URI="http://localhost:8080"

# 현재 실행 중인 애플리케이션의 PID 확인
CURRENT_PID=$(pgrep -f "spring-gift-0.0.1-SNAPSHOT.jar")

# 현재 애플리케이션 종료
if [ -z "$CURRENT_PID" ]; then
  echo "> No application is currently running."
else
  echo "> Killing current application with PID: $CURRENT_PID"
  kill -15 $CURRENT_PID
  sleep 5
fi

# 최신 코드 가져오기 및 빌드
echo "> Pulling latest code from repository"
cd $REPO_PATH
git pull

echo "> Building the project"
./gradlew bootJar

# 새로운 JAR 파일 찾기
NEW_JAR=$(ls $BUILD_PATH/*.jar | grep -v original)

# 새 애플리케이션 배포
echo "> Copying new build to deployment path"
cp $NEW_JAR $DEPLOY_PATH

# 애플리케이션 시작
DEPLOY_JAR="$DEPLOY_PATH/$(basename $NEW_JAR)"
echo "> Starting new application"
# nohup java -jar $DEPLOY_JAR > /dev/null 2> /dev/null < /dev/null &
nohup java -jar $DEPLOY_JAR \
  --kakao.client-id=$KAKAO_CLIENT_ID \
  --kakao.redirect-uri=$KAKAO_REDIRECT_URI \
  > /dev/null 2> /dev/null < /dev/null &