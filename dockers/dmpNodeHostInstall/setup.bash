#!/bin/bash

function sectionTitle {
  echo "======================"
  echo "-- $1 --"
  echo "======================"
}

## Commands run as root user
## Preliminaries
sectionTitle Preliminaries

apt update -y && apt upgrade -y
apt install -y \
  sudo \
  vim \
  emacs \
  nmap \
  netcat \
  apt-transport-https \
  ca-certificates \
  curl \
  gnupg2 \
  software-properties-common

sectionTitle "Docker Install"
# adding docker key
curl -fsSL https://download.docker.com/linux/debian/gpg | apt-key add

# setting up the stable repository 
add-apt-repository \
     "deb [arch=amd64] https://download.docker.com/linux/debian \
	    $(lsb_release -cs) \
		   stable"

# Docker installation
apt update
apt install -y docker-ce
apt install -y docker-compose

# Docker Test
sectionTitle "Docker Test"
docker run hello-world

# Docker config
iptables -P FORWARD ACCEPT

#TODO
## Call the firewall script

# /!\ Command for elasticSearch, must be run as root
sysctl -w vm.max_map_count=262144

## This part of the script must be executed directly on the node
## TODO: do another script for this part or find a way to exec this part over ssh pipe
#Creating the new user: adduser will ask for the new password
# sectionTitle "Creating [ admin ] session "
# adduser admin
# sectionTitle "Creating [ dmpnode ] session "
# adduser dmpnode
# adduser dmpnode docker
# 
# sectionTitle "Allow Admin to use Sudo ...."
# printf "using visudo in 3 sec ..."
# sleep 1
# printf "using visudo in 2 sec ..."
# sleep 1
# printf "using visudo in 1 sec ..."
# sleep 1
# visudo
