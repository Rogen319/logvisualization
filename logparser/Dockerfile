FROM java:8-jre

ADD ./target/logparser-0.1.0.jar /app/
CMD ["java", "-Xmx200m", "-jar", "/app/logparser-0.1.0.jar"]

EXPOSE 16319