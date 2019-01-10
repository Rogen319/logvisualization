package zipkinapi.service;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import zipkinapi.bean.ZipkinDependency;
import zipkinapi.response.GetDependenciesResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ZipkinAPIServiceImpl implements ZipkinAPIService {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private static final String ZIPKIN_DEPENDENCY_INDEX = "zipkin:dependency-*";
    private static final String ZIPKIN_DEPENDENCY_TYPE = "dependency";

    @Autowired
    private TransportClient client;

    @Override
    public GetDependenciesResponse getDependencies() {
        GetDependenciesResponse response = new GetDependenciesResponse();
        response.setStatus(false);
        response.setMessage("This is the default message");
        response.setZipkinDependencyList(null);

        //Get the dependencies by querying the es
        SearchResponse scrollResp = client.prepareSearch(ZIPKIN_DEPENDENCY_INDEX).setTypes(ZIPKIN_DEPENDENCY_TYPE)
                .setScroll(new TimeValue(60000))
                .setSize(100).get();

        //Scroll until no hits are returned
        SearchHit[] hits;
        List<ZipkinDependency> zipkinDependencyList = new ArrayList<>();
        while (scrollResp.getHits().getHits().length != 0) { // Zero hits mark the end of the scroll and the while loop
            hits = scrollResp.getHits().getHits();
            log.info(String.format("The length of scroll dependencies search hits is [%d]", hits.length));
            Map<String, Object> map;
            String parent, child;
            int index;
            long callCount;
            for (SearchHit hit : hits) {
                //Handle the hit
                map = hit.getSourceAsMap();
                parent = map.get("parent").toString();
                child = map.get("child").toString();
                callCount = Long.parseLong(map.get("callCount").toString());
                ZipkinDependency zipkinDependency = new ZipkinDependency(parent, child, callCount);

                //Judge if the record exist before
                index = zipkinDependencyList.indexOf(zipkinDependency);
                if (index != -1) {
                    long originCallCount = zipkinDependencyList.get(index).getCallCount();
                    zipkinDependencyList.get(index).setCallCount(originCallCount + callCount);
                    log.info(String.format("The dependency [%s-%s] already exists. Update the callCount from [%d] to [%d].",
                            parent, child, originCallCount, zipkinDependencyList.get(index).getCallCount()));
                } else {
                    log.info(String.format("Add the dependency [%s-%s-%d]", parent, child, callCount));
                    zipkinDependencyList.add(zipkinDependency);
                }
            }
            scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute().actionGet();
        }
        response.setZipkinDependencyList(zipkinDependencyList);
        response.setStatus(true);
        response.setMessage(String.format("Succeed to get the dependencies. The size of dependencies is [%d]", zipkinDependencyList.size()));
        return response;
    }
}
