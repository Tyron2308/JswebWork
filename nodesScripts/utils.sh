#!/bin/bash

CLUSTER_CONF_PATH="cluster.conf"
source $CLUSTER_CONF_PATH

function quit {
  exit
}

function isIP {
  if ! [[ $1 =~ ^[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}$ ]]
  then return 1
  else return 0
  fi
}

function fileExist {
  if [[ -e $1 ]]
  then return 0
  else return 1
  fi
}

function usage {
  echo $1
  quit
}

function error {
  echo "Error: $1"
  quit
}


function printSrvInfo {
  echo " $1  --  $2"
}

function srvList {
  echo "Server list:"
  echo "idx --  ip address"
  for i in "${!srvAddr[@]}"
  do
	printSrvInfo $i ${srvAddr[$i]}
  done
}

function extendedUsage {
  echo $1
  srvList
  quit
}
