#!/bin/bash

# Docker host ports 
DOCKER_WELL_KNOWN_PORTS[0]=2377 ## Swarm Master Port

# HTTP ports
HTTP_PORTS[0]=80	#http
HTTP_PORTS[0]=443	#ssl

# Nginx ports
NGINX_PORTS[0]=5000
NGINX_PORTS[0]=5001

# Elassandra ports
DOCKER_ELASSANDRA_PORTS[0]=7000 ## Intra-node com
DOCKER_ELASSANDRA_PORTS[1]=7001 ## TLS intra-node com
DOCKER_ELASSANDRA_PORTS[2]=7199 ## JMX
DOCKER_ELASSANDRA_PORTS[3]=9042 ## CQL
DOCKER_ELASSANDRA_PORTS[4]=9160 ## Thrift service
DOCKER_ELASSANDRA_PORTS[5]=9200 ## ElasticSearch HTTP

# Cassandra ports
CASSANDRA_PORTS[0]=7000
CASSANDRA_PORTS[1]=7001
CASSANDRA_PORTS[2]=9042

# Kibana ports
KIBANA_PORTS[0]=5601

# Elasticsearch ports
ELASTIC_PORTS[0]=9200

# Grafana ports
GRAFANA_PORTS[0]=3000


# Spark daemon ports


# Zeppelin ports

# Bind Docker interface to Ethernet

iptables -A FORWARD -i docker0 -o eth0 -j ACCEPT
iptables -A FORWARD -i eth0 -o docker0 -j ACCEPT

# Open all docker necessary ports 

iptables -A INPUT -p tcp -m tcp --dport $port -s 0.0.0.0/0 -j ACCEPT
iptables -A OUTPUT -p tcp -m tcp --dport $port -s 0.0.0.0/0 -j ACCEPT
