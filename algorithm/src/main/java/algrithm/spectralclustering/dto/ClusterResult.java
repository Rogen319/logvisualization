package algrithm.spectralclustering.dto;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ClusterResult implements Serializable {
    private static final long serialVersionUID = -3217151368632933939L;

    private String time;
    private String result;
    private List<String> services;
    private int clusterCount;
    private String clusterSize;
    private List<List<String>> clusters;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public List<List<String>> getClusters() {
        return clusters;
    }

    public void setClusters(List<List<String>> clusters) {
        this.clusters = clusters;
    }

    public String getClusterSize() {
        return clusterSize;
    }

    public void setClusterSize(String clusterSize) {
        this.clusterSize = clusterSize;
    }

    public List<String> getServices() {
        return services;
    }

    public void setServices(List<String> services) {
        this.services = services;
    }

    public int getClusterCount() {
        return clusterCount;
    }

    public void setClusterCount(int clusterCount) {
        this.clusterCount = clusterCount;
    }

    public String getTime() {
        Date date = new Date();
        SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return time.format(date);
    }

    public void setTime(String time) {
        this.time = time;
    }
}
