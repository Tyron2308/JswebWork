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
  echo "usage: ./kibana_launch.bash [name] [Elastic IP]"
  quit
}

function error {
  echo "Error: $1"
  quit
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
ELASTIC_IP=$2

if [[ $CONTAINER_NAME == "" ]]
then error "invalid container name"
fi

if ! isIP $ELASTIC_IP; then 
  error "invalid ip address"
fi

## =====================
## Launching container |
## =====================

CMD="docker run"
DAEMON_FLAG="-d"
EXT_PORT="5601"
INT_PORT="5601"
CONTAINER_NAME_OPT="--name $CONTAINER_NAME"
#BROADCAST_OPT="-e SERVER_HOST=$THIS_IP:5601"
SECURITY_OPT="-e XPACK_SECURITY_ENABLED=false"
SEED_OPT="-e ELASTICSEARCH_URL=http://$ELASTIC_IP:9200"
PORT_OPT="-p $EXT_PORT:$INT_PORT"
BASE_IMAGE="docker.elastic.co/kibana/kibana:5.5.0"

$CMD \
  $CONTAINER_NAME_OPT \
  $DAEMON_FLAG \
  $SEED_OPT \
  $SECURITY_OPT \
  $BROADCAST_OPT \
  $PORT_OPT \
  $BASE_IMAGE
