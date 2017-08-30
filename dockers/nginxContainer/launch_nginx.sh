#!/bin/bash

CMD="docker run"
NAME="--name nginx-proxy"
DAEMON_FLAG="-d"
HTTP_PORT="-p 80:80"
SSL_PORT="-p 443:443"
LABEL="--label com.github.jrcs.letsencrypt_nginx_proxy_companion.nginx_proxy=true"
BASE_IMAGE="jwilder/nginx-proxy"

NGINX_DIR="$HOME/nginxVolumes"
CERT_DIR="$NGINX_DIR/certs"
VHOST_DIR="$NGINX_DIR/vhosts"
CHALLENGES_DIR="$NGINX_DIR/challenges"
DOCKER_SOCK_DIR="/var/run/docker.sock"

CERT_VOLUME="-v $CERT_DIR:/etc/nginx/certs:ro"
VHOST_VOLUME="-v /etc/nginx/vhost.d"
CHALLENGES_VOLUME="-v /usr/share/nginx/html"
DOCKER_VOLUME="-v $DOCKER_SOCK_DIR:/tmp/docker.sock:ro"

$CMD $DAEMON_FLAG $HTTP_PORT $SSL_PORT $NAME $CERT_VOLUME $VHOST_VOLUME $CHALLENGES_VOLUME $DOCKER_VOLUME $BASE_IMAGE
