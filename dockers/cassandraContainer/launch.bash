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
  echo "usage: ./launch.bash [name] [Broadcast IP] [path/to/volume] ([-l otherNodeIp])"
  quit
}

function error {
  echo "Error: $1"
  quit
}

function createDir {
  mkdir -p $1
  if [[ -e $1 ]]
  then return 0
  else return 1
  fi
}

function delete {
  rm -rf $1
}

function cleaning {
  if [[ $DIR_CREATED == 1 ]]
  then delete $1
  fi
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

CONTAINER_NAME=$1
BROADCAST_IP=$2
VOLUME_DIR_PATH=$3
LINK_FLAG=$4
LINK_NODE=$5

if [[ $CONTAINER_NAME == "" ]]
then error "invalid container name"
fi

if ! isIP $BROADCAST_IP; then 
  error "invalid ip address"
fi

## =================
## Creating volume |
## =================

if [[ $VOLUME_DIR_PATH == "" ]]; then
  error "invalid volume path"
fi

if ! [[ -e $VOLUME_DIR_PATH ]]; then
  createDir $VOLUME_DIR_PATH
  if [[ $? == 1 ]]; then 
	error "impossible to create volume directory: $VOLUME_DIR_PATH"
  else DIR_CREATED=1
  fi
fi

## =====================
## Launching container |
## =====================

CMD="docker run"
DAEMON_FLAG="-d"
NODE_PORT="7000"
NODE_TSL_PORT="7001"
CLIENT_PORT="9042"
CONTAINER_NAME_OPT="--name $CONTAINER_NAME"
VOLUME_OPT="-v $VOLUME_DIR_PATH:/var/lib/cassandra"
BROADCAST_OPT="-e CASSANDRA_BROADCAST_ADDRESS=$BROADCAST_IP"
SEED_OPT="-e CASSANDRA_SEEDS=$LINK_NODE"
NODE_PORT_OPT="-p $NODE_PORT:$NODE_PORT"
NODE_TSL_PORT_OPT="-p $NODE_TSL_PORT:$NODE_TSL_PORT"
CLIENT_PORT_OPT="-p $CLIENT_PORT:$CLIENT_PORT"
BASE_IMAGE="cassandra"

if [[ $LINK_FLAG == "" ]]; then
  $CMD \
	$CONTAINER_NAME_OPT \
	$VOLUME_OPT \
	$DAEMON_FLAG \
	$BROADCAST_OPT \
	$NODE_PORT_OPT \
	$NODE_TSL_PORT_OPT \
	$CLIENT_PORT_OPT \
	$BASE_IMAGE
  ## $CMD $CONTAINER_NAME_OPT $VOLUME_OPT $DAEMON_FLAG --net=host $BASE_IMAGE
else
  if [[ $LINK_FLAG == "-l" ]]; then
	if ! isIP $LINK_NODE; then
	  cleaning $VOLUME_DIR_PATH
	  error "invalid linked node ip address"
	fi
	$CMD \
	  $CONTAINER_NAME_OPT \
	  $VOLUME_OPT \
	  $DAEMON_FLAG \
	  $BROADCAST_OPT \
	  $NODE_PORT_OPT \
	  $NODE_TSL_PORT_OPT \
	  $CLIENT_PORT_OPT \
	  $SEED_OPT \
	  $BASE_IMAGE
	## $CMD $CONTAINER_NAME_OPT $VOLUME_OPT $DAEMON_FLAG --net=host $SEED_OPT $BASE_IMAGE
  else
	cleaning $VOLUME_DIR_PATH
	error "wrong link option, use -l"
  fi
fi
