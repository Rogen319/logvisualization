package k8sapi.service;

import k8sapi.response.GetNodesListResponse;
import k8sapi.response.GetPodsListResponse;

public interface KubernetesAPIService {
    GetNodesListResponse getNodesList();
    GetPodsListResponse getPodsListAPI();
}
