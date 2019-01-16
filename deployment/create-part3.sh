#!/bin/sh
istioctl create -f trainticket-gateway.yaml
kubectl create -f zipkin-create.yaml
kubectl create -f zipkin-ui.yaml