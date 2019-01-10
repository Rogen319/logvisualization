package algrithm.sequence.dto;

import java.io.Serializable;

public class GeneralResponse implements Serializable {

    private static final long serialVersionUID = -8665058187508372251L;

    private boolean status;
    private String message;

    public GeneralResponse() {

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
}
