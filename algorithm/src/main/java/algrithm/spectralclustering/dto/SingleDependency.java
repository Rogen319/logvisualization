package algrithm.spectralclustering.dto;

import java.io.Serializable;

public class SingleDependency implements Serializable{

    private static final long serialVersionUID = 3710140897196447774L;

    private String parent;
    private String child;
    private int callCount;

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getChild() {
        return child;
    }

    public void setChild(String child) {
        this.child = child;
    }

    public int getCallCount() {
        return callCount;
    }

    public void setCallCount(int callCount) {
        this.callCount = callCount;
    }
}
