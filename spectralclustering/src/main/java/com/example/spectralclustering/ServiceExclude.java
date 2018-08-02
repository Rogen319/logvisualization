package com.example.spectralclustering;

public enum ServiceExclude {
    ISTIO_TELEMETRY("istio-telemetry"),
    ISTIO_MIXER("istio-mixer"),
    ISTIO_POLICY("istio-policy"),
    ISTIO_INGRESSGATEWAY("istio-ingressgateway");

    private String serviceName;

    ServiceExclude(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}
