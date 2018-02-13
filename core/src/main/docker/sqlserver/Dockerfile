# holon/sqlserver

FROM microsoft/mssql-server-linux:2017-latest

COPY entrypoint.sh /entrypoint.sh
COPY entrypoint.sh /import-data.sh
COPY init.sql /init.sql

RUN chmod +x /entrypoint.sh
RUN chmod +x /import-data.sh

EXPOSE 1433

#ENTRYPOINT ["/entrypoint.sh"]
CMD /bin/bash ./entrypoint.sh