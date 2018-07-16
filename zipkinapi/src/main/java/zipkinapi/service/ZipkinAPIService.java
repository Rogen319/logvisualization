package zipkinapi.service;

import zipkinapi.response.GetDependenciesResponse;

public interface ZipkinAPIService {
    GetDependenciesResponse getDependencies();
}
