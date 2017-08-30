#!/bin/bash

CMD="docker run"
NAME="--name letsencrypt"
DAEMON_FLAG="-d"
BASE_IMAGE="jrcs/letsencrypt-nginx-proxy-companion"

NGINX_DIR="$HOME/nginxVolumes"
CERT_DIR="$NGINX_DIR/certs"
DOCKER_SOCK_DIR="/var/run/docker.sock"

CERT_VOLUME="-v $CERT_DIR:/etc/nginx/certs:rw"
DOCKER_VOLUME="-v $DOCKER_SOCK_DIR:/var/run/docker.sock:ro"
NGINX_VOLUME="--volumes-from nginx-proxy"

$CMD $DAEMON_FLAG $NAME $CERT_VOLUME $DOCKER_VOLUME $NGINX_VOLUME $BASE_IMAGE
