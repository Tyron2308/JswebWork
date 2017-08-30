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
  echo "usage: ./grafana_launch.bash [name]"
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

if [[ $# -lt 1 ]]; then 
  usage
fi

CONTAINER_NAME=$1

if [[ $CONTAINER_NAME == "" ]]
then error "invalid container name"
fi

## =====================
## Launching container |
## =====================

CMD="docker run"
DAEMON_FLAG="-d"
EXT_PORT="3000"
INT_PORT="3000"
CONTAINER_NAME_OPT="--name $CONTAINER_NAME"
BROADCAST_OPT="-e \"GF_SERVER_ROOT_URL=http://$THIS_IP\""
SECURITY_OPT="-e \"GF_SECURITY_ADMIN_PASSWORD=admin\""
PORT_OPT="-p $EXT_PORT:$INT_PORT"
BASE_IMAGE="grafana/grafana"

$CMD \
  $CONTAINER_NAME_OPT \
  $DAEMON_FLAG \
  $BROADCAST_OPT \
  $SECURITY_OPT \
  $PORT_OPT \
  $BASE_IMAGE
