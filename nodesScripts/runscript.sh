#!/bin/bash

USAGE_MSG="usage: ./runscript.bash [nodeNumber] [scriptPath] [user]"
UTILS_PATH="utils.sh"

source $UTILS_PATH

if [[ $1 == "-h" ]]; then
  extendedUsage "$USAGE_MSG"
fi

if [[ $1 == "-l" ]]; then
  srvList
  quit
fi

if [[ $# -lt 2 ]]
then usage "$USAGE_MSG"
fi

#-------------------------------------

CMD="ssh"
SRV_NBR=$1
SCRIPT_PATH=$2
USER=$3
SRV_ADDR=${srvAddr[$SRV_NBR]}

if [[ $SRV_NBR == "" ]]; then
  usage "$USAGE_MSG"
fi

if [[ $SRV_ADDR == "" ]]; then
  error "invalid node number"
fi

if ! fileExist $SCRIPT_PATH; then
  error "no such file or directory: $SCRIPT_PATH"
fi

if [[ $USER == "" ]]; then
  USER="admin"
fi

$CMD $USER@$SRV_ADDR 'bash -s' < $SCRIPT_PATH
