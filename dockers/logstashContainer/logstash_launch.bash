#!/bin/bash

## ===================
## Utility functions |
## ===================

function quit {
  exit
}

function usage {
  echo "usage: ./logstash_launch.bash [name] [path/to/logstash.conf] [path/to/logstash.yml]"
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

if [[ $# -lt 3 ]]; then 
  usage
fi

function fileExist {
  if [[ -e $1 ]]
  then return 0
  else return 1
  fi
}

CONTAINER_NAME=$1
LOGSTASH_CONF_PATH=$2
SETTING_YML_PATH=$3
#PIPELINE_DIR_PATH=$2
#SETTING_DIR_PATH=$3

if [[ $CONTAINER_NAME == "" ]]
then error "invalid container name"
fi

if ! fileExist $LOGSTASH_CONF_PATH; then
  error "invalid conf path: $LOGSTASH_CONF_PATH"
fi

if ! fileExist $SETTING_YML_PATH; then
  error "invalid setting path: $SETTING_YML_PATH"
fi

## =====================
## Launching container |
## =====================

CMD="docker run"
DAEMON_FLAG="-d"
RM_FLAG="-rm"
INTERACTIVE_FLAG="-it"
PIPELINE_FILE_BIND="-v $LOGSTASH_CONF_PATH:/usr/share/logstash/pipeline/logstash.conf"
SETTING_FILE_BIND="-v $SETTING_YML_PATH:/usr/share/logstash/config/logstash.yml"
BASE_IMAGE="docker.elastic.co/logstash/logstash:5.5.0"

$CMD \
  $CONTAINER_NAME_OPT \
  $DAEMON_FLAG \
  $RM_FLAG \
  $INTERACTIVE_FLAG \
  $PIPELINE_FILE_BIND \
  $SETTING_FILE_BIND \
  $BASE_IMAGE
