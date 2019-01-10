package escore.response;

import java.util.Set;

public class GetRequestTypesRes extends GeneralResponse {
    private Set<String> requestTypes;

    public GetRequestTypesRes() {
        super();
    }

    public Set<String> getRequestTypes() {
        return requestTypes;
    }

    public void setRequestTypes(Set<String> requestTypes) {
        this.requestTypes = requestTypes;
    }
}
