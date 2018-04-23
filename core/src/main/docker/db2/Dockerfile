# holon/db2

FROM ibmcom/db2express-c:10.5.0.5-3.10.0

COPY entrypoint.sh /entrypoint.sh

RUN chmod 755 /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]

EXPOSE 50000
