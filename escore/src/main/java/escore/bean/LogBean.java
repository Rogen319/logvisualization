package escore.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LogBean {
    @JsonProperty("TraceId")
    private String traceId = "";

    @JsonProperty("SpanId")
    private String spanId = "";

    @JsonProperty("ParentSpanId")
    private String parentSpanId = "";

    @JsonProperty("LogType")
    private String logType = "";

    @JsonProperty("URI")
    private String uri = "";

    @JsonProperty("RequestType")
    private String requestType = "";

    private String log = "";

    private KubernetesMeatadata kubernetes;

    public LogBean() {

    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getSpanId() {
        return spanId;
    }

    public void setSpanId(String spanId) {
        this.spanId = spanId;
    }

    public String getParentSpanId() {
        return parentSpanId;
    }

    public void setParentSpanId(String parentSpanId) {
        this.parentSpanId = parentSpanId;
    }

    public String getLogType() {
        return logType;
    }

    public void setLogType(String logType) {
        this.logType = logType;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public KubernetesMeatadata getKubernetes() {
        return kubernetes;
    }

    public void setKubernetes(KubernetesMeatadata kubernetes) {
        this.kubernetes = kubernetes;
    }
}
