# holon/sqlite

FROM ubuntu:14.04

COPY dbsetup.sh /dbsetup.sh

RUN apt-get update && apt-get -y install rlwrap sqlite3 socat
RUN chmod 755 /dbsetup.sh
RUN /dbsetup.sh

EXPOSE 12345