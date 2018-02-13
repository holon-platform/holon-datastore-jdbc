#!/bin/bash
su - db2inst1 -c "db2start && db2set DB2COMM=tcpip && db2 create database test"
