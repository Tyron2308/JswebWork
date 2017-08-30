#!/bin/bash

DIR_CREATED=0

## ===================
## Utility functions |
## ===================

function quit {
  exit
}

function isIP {
  if ! [[ $1 =~ ^[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}$ ]]
  then return 1
  else return 0
  fi
}

function usage {
  echo "usage: ./elastic_launch.bash [name] [path/to/datavolume]"
  quit
}

function error {
  echo "Error: $1"
  quit
}

function fileExist {
  if [[ -e $1 ]]
  then return 0
  else return 1
  fi
}

## ========
## Checks |
## ========

if [[ $1 == "-h" ]]; then 
  usage
fi

if [[ $# -lt 2 ]]; then 
  usage
fi


CONTAINER_NAME=$1
VOLUME_PATH=$2

if [[ $CONTAINER_NAME == "" ]]
then error "invalid container name"
fi

if ! fileExist $VOLUME_PATH; then
  error "no such file or directory: $VOLUME_PATH"
fi

## =====================
## Launching container |
## =====================

CMD="docker run"
DAEMON_FLAG="-d"
CONTAINER_NAME_OPT="--name $CONTAINER_NAME"
EL_PORT="-p 9200:9200"
HOST_OPT="-e \"http.host=$THIS_IP\""
DATA_VOLUME="-v $VOLUME_PATH:/usr/share/elasticsearch/data"
TRANSPORT_OPT="-e \"transport.host=127.0.0.1\""
SECURITY_OPT="-e \"xpack.security.enabled=false\""
ULIMIT="--ulimit nofile=65536:65536"
BASE_IMAGE="docker.elastic.co/elasticsearch/elasticsearch:5.5.0"

$CMD \
  $DAEMON_FLAG \
  $CONTAINER_NAME_OPT \
  $EL_PORT \
  $SECURITY_OPT \
  $HOST_OPT \
  $TRANSPORT_OPT \
  $ULIMIT \
  $BASE_IMAGE \
