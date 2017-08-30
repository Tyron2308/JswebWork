#!/bin/bash

VHOST="will.cptefr.com"
EMAIL="wilfried@jswebproduction.com"
LINK_PROXY="-e VIRTUAL_HOST=$VHOST"

SSL_CERTIFICATES_STORAGE="-v $HOME/sslCert:/root/sslKeys"
WWW_STORAGE="-v $HOME/www:/srv/http"
LOGS_STORAGE="-v $HOME/logs:/jsweb"
SSL_PORTS="-p 443:443"
HTTP_PORTS="-p 80:80"
BASE_IMAGE="lamp_base"
LCRYPT_HOST="-e LETSENCRYPT_HOST=$VHOST"
LCRYPT_MAIL="-e LETSENCRYPT_EMAIL=$EMAIL"

docker run --name lamp $LINK_PROXY $LCRYPT_HOST $LCRYPT_MAIL $WWW_STORAGE $LOGS_STORAGE --expose 80 -d $BASE_IMAGE
