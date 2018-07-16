FROM java:8-jre

ADD ./target/zipkinapi-0.1.0.jar /app/
CMD ["java", "-Xmx200m", "-jar", "/app/zipkinapi-0.1.0.jar"]

EXPOSE 19319