#!/bin/bash
su - db2inst1 -c "db2start && db2 -td@ -f /init.sql"

nohup /usr/sbin/sshd -D 2>&1 > /dev/null &
while true; do sleep 1000; done
