#!/bin/sh
kubectl delete -f zipkin-ui.yaml
kubectl delete -f zipkin-create.yaml
kubectl delete -f filebeat-kubernetes.yaml
kubectl delete -f logstash.yaml
kubectl delete -f logging-stack.yaml
istioctl delete -f trainticket-gateway.yaml
kubectl delete -f ts-deployment-part3.yml
kubectl delete -f ts-deployment-part2.yml
kubectl delete -f ts-deployment-part1.yml