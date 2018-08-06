# LogVisualization

## Description   
This is a project trying to corelate the trace of microservices with the logs of the microservices. It is based on kubernetes and istio.And elasticsearch is used to store the log and zipkin to gather the trace and span information.</p>

## Deployment     
Before you begin, you need to install istio and customerized zipkin in your kubernetes cluster. Just execute the following command:
* kubectl apply -f istio-demo-with-zipkin-to-es.yaml

1. First, deploy the elasticsearch and filebeat by the following commands:
    * kubectl create -f logging-stack.yaml
    * kubectl create -f logstash.yaml
    * kubectl create -f filebeat-kubernetes.yaml
2. Second, deploy the train ticket system by executing the following commands:
    * kubectl create -f <(istioctl kube-inject -f ts-deployment-part1.yml)
    * kubectl create -f <(istioctl kube-inject -f ts-deployment-part2.yml)
    * kubectl create -f <(istioctl kube-inject -f ts-deployment-part3.yml)
    * istioctl create -f trainticket-gateway.yaml
3. To use the zipkin dependency service, execute the following commands:
    * kubectl create -f zipkin-dependency.yaml
4. Deployment our zipkin-ui
    * kubectl create -f zipkin-ui.yaml
    
## Access 
After all of the pods are in running states, you can access the system by the following ip and port:
   *  trainticket：http://10.141.211.163:31380/
   *  kibana：http://10.141.211.163:30001
   *  elasticsearch：http://10.141.211.163:30002/
   *  zipkin：http://10.141.211.163:30005
   *  zipkin-ui: http://10.141.211.163:30006/zipkin
    
## Uninstall   
To uninstall the whole system, execute the following commands:
   *  kubectl delete -f zipkin-ui.yaml
   *  kubectl delete -f zipkin-create.yaml
   *  kubectl delete -f filebeat-kubernetes.yaml
   *  kubectl delete -f logstash.yaml
   *  kubectl delete -f logging-stack.yaml
   *  istioctl delete -f trainticket-gateway.yaml
   *  kubectl delete -f ts-deployment-part3.yml
   *  kubectl delete -f ts-deployment-part2.yml
   *  kubectl delete -f ts-deployment-part1.yml
   
## Notification
1. When build and run the zipkin ui in kubernetes, it may crash loop backoff because "standard_init_linux.go:178: exec user process caused "no such file or directory"".
This is caused by the format of nginx.conf and run.sh. If you open them in Windows, the format may be changed to dos. You need to use the dos2unix to change it to the
unix format.