package algrithm.spectralclustering.enumeration;

public enum ServiceExcludeEnum {
    ISTIO_TELEMETRY("istio-telemetry"),
    ISTIO_MIXER("istio-mixer"),
    ISTIO_POLICY("istio-policy"),
    ISTIO_INGRESSGATEWAY("istio-ingressgateway");

    private String serviceName;

    ServiceExcludeEnum(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}
