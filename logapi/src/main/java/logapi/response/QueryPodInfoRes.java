package logapi.response;

import logapi.bean.PodInfo;

public class QueryPodInfoRes extends GeneralResponse {
    private PodInfo podInfo;

    public QueryPodInfoRes() {

    }

    public PodInfo getPodInfo() {
        return podInfo;
    }

    public void setPodInfo(PodInfo podInfo) {
        this.podInfo = podInfo;
    }
}
