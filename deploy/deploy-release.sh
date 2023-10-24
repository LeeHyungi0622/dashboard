#!/bin/bash

# env varies
SSH_HOST="110.45.181.73"
SSH_USER="root"
SSH_PW='!@#HOSilm'
INGEST_MANAGER_VERSION=$1
NIFI_VERSION="beta"
DEPLOY_PATH="/home/packaging"

# SSH로 호스트 서버 접속
sshpass -p "$SSH_PW" ssh -o StrictHostKeyChecking=no "$SSH_USER"@"$SSH_HOST" -p 22 << EOF
# 해당 모듈 디렉토리로 이동
cd $DEPLOY_PATH/ingest-manager

# 기존 Datacore Docker 이미지 삭제 command
#echo "=> Remove existing built docker images..."
#echo "=> << Remove docker image docker images... >>"
#docker rmi -f ingest-manager:$INGEST_MANAGER_VERSION
#docker rmi -f nifi:$NIFI_VERSION

echo "=> Build docker images..."
echo "=> << Build docker images... >>"
docker build -t ingest-manager:$INGEST_MANAGER_VERSION --build-arg version=$INGEST_MANAGER_VERSION --no-cache .
docker build -t nifi:$NIFI_VERSION --build-arg version=$NIFI_VERSION --no-cache -f Dockerfile.NiFi .

#  Docker image를 저장 디렉토리 생성 (없으면)
mkdir -p $DEPLOY_PATH/docker-images

echo "=> << Save docker images... >>"
docker save ingest-manager:$INGEST_MANAGER_VERSION -o $DEPLOY_PATH/docker-images/ingest-manager:$INGEST_MANAGER_VERSION.tar
docker save nifi:$NIFI_VERSION -o $DEPLOY_PATH/docker-images/nifi:$NIFI_VERSION.tar


exit
EOF