package escore.response;

import java.util.List;

public class GetInstanceNamesFromESRes {

    private boolean status;
    private String message;
    private List<String> instanceNames;

    public GetInstanceNamesFromESRes() {

    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getInstanceNames() {
        return instanceNames;
    }

    public void setInstanceNames(List<String> instanceNames) {
        this.instanceNames = instanceNames;
    }
}
