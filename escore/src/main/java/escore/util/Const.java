package escore.util;

public class Const {
    public static final String ZIPKIN_SPAN_INDEX = "zipkin:span-*";
    public static final String ZIPKIN_SPAN_TYPE = "span";
    public static final String LOGSTASH_LOG_INDEX = "logstash-*";
    public static final String LOGSTASH_LOG_TYPE = "beats";
    public static final String K8S_POD_INDEX = "k8s_pod";
    public static final String K8S_POD_TYPE = "pod";
    public static final String K8S_NODE_INDEX = "k8s_node";
    public static final String K8S_NODE_TYPE = "node";
    public static final String TRACE_STATUS_INDEX = "trace_status";
    public static final String TRACE_STATUS_TYPE = "status";
    public static final int NORMAL_TRACE_FLAG = 0;
    public static final int ERROR_TRACE_FLAG = 1;
}
