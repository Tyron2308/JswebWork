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
  echo "usage: ./metricbeat_launch.bash [name] [path/to/metricbeat.yml]"
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
CONFIG_FILE=$2

if [[ $CONTAINER_NAME == "" ]]
then error "invalid container name"
fi

if ! fileExist $CONFIG_FILE; then
  error "no such file or directory: $CONFIG_FILE"
fi

## =====================
## Launching container |
## =====================

CMD="docker run"
DAEMON_FLAG="-d"
CONTAINER_NAME_OPT="--name $CONTAINER_NAME"
CONF_VOL_BIND="-v $CONFIG_FILE:/usr/share/metricbeat/metricbeat.yml"
PROC_VOL_BIND="--volume=/proc:/hostfs/proc:ro"
SYS_VOL_BIND="--volume=/sys/fs/cgroup:/hostfs/sys/fs/cgroup:ro"
HOSTFS_VOL_BIND="--volume=/:/hostfs:ro"
NETWORK_OPT="--net=host"
BASE_IMAGE="docker.elastic.co/beats/metricbeat:5.5.0"
## SYS_OPT="-system.hostfs=/hostfs"

$CMD \
  $DAEMON_FLAG \
  $CONTAINER_NAME_OPT \
  $CONF_VOL_BIND \
  $PROC_VOL_BIND \
  $SYS_VOL_BIND \
  $HOSTFS_VOL_BIND \
  $NETWORK_OPT \
  $BASE_IMAGE \
  # $SYS_OPT
