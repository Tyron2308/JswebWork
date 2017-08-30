#!/usr/bin/env python

import socket
import time

TCP_IP = '127.0.0.1'
##TCP_IP = '149.202.91.15'
TCP_PORT = 5005
BUFFER_SIZE = 4096

filename = "access.log.1"


## with open("sample") as file:
##     for line in file:
##         s.send(line.encode('utf-8'))
##         print(line)
##         break

print("Sending file ...")
with open(filename) as file:
    for line in file:
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s.connect((TCP_IP, TCP_PORT))
        s.send(line.encode('utf-8'))
        ##time.sleep(0.1)
        s.close()

print("Done.")
