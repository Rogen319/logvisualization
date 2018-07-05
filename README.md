# LogVisualization

##Description
This is a project trying to corelate the trace of microservices with the logs of the microservices. It is based on kubernetes and istio.And elasticsearch is used to store the log and zipkin to gather the trace and span information.

##Deployment
Before you begin, you need to install istio in your kubernetes cluster. Just execute the following command:
* kubectl apply -f istio-demo.yaml

1. First, deployment the train ticket system by executing the following commands:
    * kubectl create -f <(istioctl kube-inject -f ts-deployment-part1.yml)
    * kubectl create -f <(istioctl kube-inject -f ts-deployment-part2.yml)
    * kubectl create -f <(istioctl kube-inject -f ts-deployment-part3.yml)
    * istioctl create -f trainticket-gateway.yaml
2. Then, deploy the elasticsearch and filebeat by the following commands:
    * kubectl create -f logging-stack.yaml
    * kubectl create -f filebeat-kubernetes.yaml