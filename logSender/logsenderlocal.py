#!/usr/bin/env python

import socket
import time



//script python pour envoyer au server des logs.

TCP_IP = '127.0.0.1'
TCP_PORT = 5005
BUFFER_SIZE = 4096


import sys

##for arg in sys.argv:
if len(sys.argv) > 1:
    filename = sys.argv[1]         
else:
    filename = "sample"

print("Sending file ..." + filename)
with open(filename) as file:
    for line in file:
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s.connect((TCP_IP, TCP_PORT))
        s.send(line.encode('utf-8'))
        ##time.sleep(0.1)
        s.close()

print("Done.")
