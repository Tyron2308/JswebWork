#!/bin/bash

CMD="delete"
SUB_CMD="indices"
UNIT="days"
NBR="1"
TIME_UNIT="--time-unit $UNIT"
THRESHOLD="--older-than $NBR"
BIND_CONF="-v $PWD/curator.yml:/root/.curator/curator.yml"
BIND_ACTION="-v $PWD/ActionFile.yml:/root/.curator/ActionFile.yml"

docker run $BIND_CONF $BIND_ACTION --rm bobrik/curator:5.1.1 /root/.curator/ActionFile.yml
