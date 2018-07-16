package zipkinapi.response;

import zipkinapi.bean.ZipkinDependency;

import java.util.List;

public class GetDependenciesResponse extends GeneralResponse {
    private List<ZipkinDependency> zipkinDependencyList;

    public GetDependenciesResponse(){

    }

    public List<ZipkinDependency> getZipkinDependencyList() {
        return zipkinDependencyList;
    }

    public void setZipkinDependencyList(List<ZipkinDependency> zipkinDependencyList) {
        this.zipkinDependencyList = zipkinDependencyList;
    }
}
