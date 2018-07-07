FROM java:8-jre

ADD ./target/k8sapi-0.1.0.jar /app/
CMD ["java", "-Xmx200m", "-jar", "/app/k8sapi-0.1.0.jar"]

EXPOSE 18319