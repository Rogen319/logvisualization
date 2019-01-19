#!/bin/sh
kubectl create -f <$(istioctl kube-inject -f ts-deployment-part2.yml)
kubectl create -f <$(istioctl kube-inject -f ts-deployment-part3.yml)