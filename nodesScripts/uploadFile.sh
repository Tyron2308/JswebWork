#!/bin/bash

USAGE_MSG="usage:./uploadFile.sh [serverNumber] [filePath] [user]"
UTILS_PATH="utils.sh"

source $UTILS_PATH

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

CMD="scp -r"
SRV_NBR=$1
FILE_PATH=$2
USER=$3
SRV_ADDR=${srvAddr[$SRV_NBR]}

if [[ $SRV_NBR == "" ]]; then
  usage "$USAGE_MSG"
fi

if [[ $SRV_ADDR == "" ]]; then
  error "invalid node number"
fi

if ! fileExist $FILE_PATH; then
  error "invalid path: $FILE_PATH"
fi

if [[ $USER == "" ]]; then
  USER="admin"
fi

$CMD $FILE_PATH $USER@$SRV_ADDR:~/
