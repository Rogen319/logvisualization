package zipkinapi.bean;

public class ZipkinDependency {
    private String parent;
    private String child;
    private long callCount;

    public ZipkinDependency() {

    }

    public ZipkinDependency(String parent, String child, long callCount) {
        this.parent = parent;
        this.child = child;
        this.callCount = callCount;
    }

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

    public long getCallCount() {
        return callCount;
    }

    public void setCallCount(long callCount) {
        this.callCount = callCount;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof ZipkinDependency) {
            ZipkinDependency other = (ZipkinDependency) obj;
            if (this.getParent().equals(other.getParent()) && this.getChild().equals(other.getChild()))
                return true;
            else
                return false;
        }
        return false;
    }

    @Override
    public int hashCode() {
        String id = this.parent + this.child;
        return id.hashCode();
    }
}
