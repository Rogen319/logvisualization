package k8sapi.bean;

import java.util.List;

public class V1LoadBalancerStatus {
    private List<V1LoadBalancerIngress> ingress;

    public V1LoadBalancerStatus(){

    }

    public List<V1LoadBalancerIngress> getIngress() {
        return ingress;
    }

    public void setIngress(List<V1LoadBalancerIngress> ingress) {
        this.ingress = ingress;
    }
}
