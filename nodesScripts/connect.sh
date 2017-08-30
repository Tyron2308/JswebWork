#!/bin/bash

USAGE_MSG="usage:./connect [serverNumber] [user]"
UTILS_SRC_PATH="utils.sh"

source $UTILS_SRC_PATH

if [[ $1 == "-h" ]]; then
  extendedUsage "$USAGE_MSG"
fi

if [[ $1 == "-l" ]]; then
  srvList
  quit
fi

if [[ $# -lt 1 ]]; then
  usage "$USAGE_MSG"
fi

#-------------------------------------

CMD="ssh"
SRV_NBR=$1
USER=$2
SRV_ADDR=${srvAddr[$SRV_NBR]}

if [[ $SRV_NBR == "" ]]; then
  usage "$USAGE_MSG"
fi

if [[ $SRV_ADDR == "" ]]; then
  error "invalid node number"
fi

if [[ $USER == "" ]]; then
  USER="admin"
fi

$CMD $USER@$SRV_ADDR
