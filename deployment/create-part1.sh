#!/bin/sh
kubectl create -f logging-stack.yaml
kubectl create -f logstash.yaml
kubectl create -f filebeat-kubernetes.yaml
kubectl create -f ts-deployment-part1.yml