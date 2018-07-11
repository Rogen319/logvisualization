# LogVisualization

## Description   
This is a project trying to corelate the trace of microservices with the logs of the microservices. It is based on kubernetes and istio.And elasticsearch is used to store the log and zipkin to gather the trace and span information.</p>

## Deployment     
Before you begin, you need to install istio in your kubernetes cluster. Just execute the following command:
* kubectl apply -f istio-demo.yaml

1. First, deployment the train ticket system by executing the following commands:
    * kubectl create -f <(istioctl kube-inject -f ts-deployment-part1.yml)
    * kubectl create -f <(istioctl kube-inject -f ts-deployment-part2.yml)
    * kubectl create -f <(istioctl kube-inject -f ts-deployment-part3.yml)
    * istioctl create -f trainticket-gateway.yaml
2. Then, deploy the elasticsearch and filebeat by the following commands:
    * kubectl create -f logging-stack.yaml
    * kubectl create -f logstash.yaml
    * kubectl create -f filebeat-kubernetes.yaml
    
## Uninstall   
To uninstall the whole system, execute the following commands:
   *  kubectl delete -f filebeat-kubernetes.yaml
   *  kubectl delete -f logstash.yaml
   *  kubectl delete -f logging-stack.yaml
   *  istioctl delete -f trainticket-gateway.yaml
   *  kubectl delete -f ts-deployment-part3.yml
   *  kubectl delete -f ts-deployment-part2.yml
   *  kubectl delete -f ts-deployment-part1.yml