# pins on 1.11 as it is built against Alpine 3.4 which doesn't die with SSL certificate problem
FROM nginx:1.11-alpine

ENV ZIPKIN_VERSION 2.10.1
ENV ZIPKIN_BASE_URL=http://10.141.212.23:30005

COPY zipkin-ui-2.10.1-SNAPSHOT.jar zipkin-ui-2.10.1-SNAPSHOT.jar

# Setup services
ADD nginx.conf /etc/nginx/conf.d/zipkin.conf.template
ADD run.sh /usr/local/bin/nginx.sh

RUN apk add --update --no-cache nginx curl && \
    rm -rf /var/cache/apk/* /tmp/* /var/tmp/* && \
    mkdir /var/www/html && \
    unzip zipkin-ui-2.10.1-SNAPSHOT.jar 'zipkin-ui/*' -d /var/www/html && \
    mv /var/www/html/zipkin-ui /var/www/html/zipkin && \
    rm -rf zipkin-ui-2.10.1-SNAPSHOT.jar && \
    chmod +x /usr/local/bin/nginx.sh

EXPOSE 9091

CMD ["/usr/local/bin/nginx.sh"]
