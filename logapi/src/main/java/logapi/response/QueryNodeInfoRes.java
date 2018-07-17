package logapi.response;

import logapi.bean.NodeInfo;

public class QueryNodeInfoRes extends GeneralResponse {
    private NodeInfo nodeInfo;

    public QueryNodeInfoRes(){

    }

    public NodeInfo getNodeInfo() {
        return nodeInfo;
    }

    public void setNodeInfo(NodeInfo nodeInfo) {
        this.nodeInfo = nodeInfo;
    }
}
