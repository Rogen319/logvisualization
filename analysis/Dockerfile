FROM java:8-jre

ADD ./target/analysis-0.1.0.jar /app/
CMD ["java", "-Xmx200m", "-jar", "/app/analysis-0.1.0.jar"]

EXPOSE 15319