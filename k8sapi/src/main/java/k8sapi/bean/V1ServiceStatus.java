package k8sapi.bean;

public class V1ServiceStatus {
    private V1LoadBalancerStatus loadBalancer;

    public V1ServiceStatus(){

    }

    public V1LoadBalancerStatus getLoadBalancer() {
        return loadBalancer;
    }

    public void setLoadBalancer(V1LoadBalancerStatus loadBalancer) {
        this.loadBalancer = loadBalancer;
    }
}
