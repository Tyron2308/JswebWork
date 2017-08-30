FROM ubuntu

## ============
## INSTALL SBT |
## ============
RUN apt-get update && apt-get install -y apt-transport-https openjdk-8-jdk openjdk-8-jre

RUN \ 
echo "deb https://dl.bintray.com/sbt/debian /" | tee -a /etc/apt/sources.list.d/sbt.list && \
	   apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 2EE0EA64E40A89B84B2DF73499E82A75642AC823 && \
	   apt-get update && \
	   apt-get install -y sbt


## ========================
## INSTALL server project |
## ========================

RUN			mkdir -p /root/server

COPY		scalaServer /root/server

RUN			cd /root/server && sbt compile

COPY		launch_server.sh /root/start_server.sh

RUN			chmod +x /root/start_server.sh

ENTRYPOINT	/root/start_server.sh

WORKDIR		/root
