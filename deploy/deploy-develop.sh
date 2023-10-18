#!/bin/bash
sshpass -p '!@#HOSilm' ssh -o StrictHostKeyChecking=no -p 22 "root@110.45.181.72" << EOF


cd /usr/local/lib/develop-pipeline/ingest-manager

docker-compose ps
docker-compose down
docker-compose up -d --build
docker-compose ps

exit
EOF
