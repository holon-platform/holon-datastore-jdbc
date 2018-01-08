# holon/db2

FROM ibmcom/db2express-c:10.5.0.5-3.10.0

COPY init.sql /init.sql
COPY entrypoint.sh /entrypoint.sh

RUN chmod 755 /entrypoint.sh

EXPOSE 50000

ENTRYPOINT ["/entrypoint.sh"]

CMD ["db2start"]
