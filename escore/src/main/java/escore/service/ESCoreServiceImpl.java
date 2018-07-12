package escore.service;

import escore.bean.RTRelation;
import escore.bean.RequestWithTraceID;
import escore.init.InitIndexAndType;
import escore.response.GetRequestTypesRes;
import escore.response.GetRequestWithTraceIDRes;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ESCoreServiceImpl implements ESCoreService {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TransportClient client;

    //Just for demo
    @Override
    public String demo() {
        StringBuilder sb = new StringBuilder();

        MatchQueryBuilder builder = QueryBuilders.matchQuery("kubernetes.container.name","ts-login-service");
        SearchResponse response = client.prepareSearch("logstash-*").setTypes("beats").setQuery(builder).setSize(10000).get();

        SearchHit[] hits = response.getHits().getHits();
        log.info(String.format("The length of hits is [%d]", hits.length));
        for (SearchHit hit : hits) {
            log.info(hit.getSourceAsString());
            sb.append(hit.getSourceAsString());
        }
        return sb.toString();
    }

    //Get the request types list
    @Override
    public GetRequestTypesRes getRequestTypes() {
        GetRequestTypesRes res = new GetRequestTypesRes();
        res.setStatus(false);
        res.setMessage("This is the origin message!");

        SearchResponse scrollResp = client.prepareSearch(InitIndexAndType.REQUEST_TRACE_RELATION_INDEX).setTypes("relation")
                .setScroll(new TimeValue(60000))
                .setSize(100).get();

        //Scroll until no hits are returned
        SearchHit[] hits;
        Set<String> requestTypes = new HashSet<>();
        while(scrollResp.getHits().getHits().length != 0){ // Zero hits mark the end of the scroll and the while loop
            hits = scrollResp.getHits().getHits();
            log.info(String.format("The length of scroll request type search hits is [%d]", hits.length));
            Map<String, Object> map;
            for (SearchHit hit : hits) {
                //Handle the hit
                map = hit.getSourceAsMap();
                requestTypes.add(map.get("requestType").toString());
            }
            scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute().actionGet();
        }
        res.setRequestTypes(requestTypes);
        res.setStatus(true);
        res.setMessage(String.format("Succeed to get request types. There are [%d] request types now.", requestTypes.size()));
        return res;
    }

    //Get the request type with its trace ids
    @Override
    public GetRequestWithTraceIDRes getRequestWithTraceID() {
        GetRequestWithTraceIDRes res = new GetRequestWithTraceIDRes();
        res.setStatus(false);
        res.setMessage("This is the origin message");
        List<RequestWithTraceID> requestWithTraceIDList = new ArrayList<>();

        //Get request types first
        GetRequestTypesRes getRequestTypesRes = this.getRequestTypes();
        if(getRequestTypesRes.isStatus()){
            MatchQueryBuilder qb;
            SearchResponse scrollResp;
            SearchHit[] hits;
            Map<String, Object> map;
            for(String requestType : getRequestTypesRes.getRequestTypes()){
                //Create the object to store the information
                RequestWithTraceID requestWithTraceID = new RequestWithTraceID();
                requestWithTraceID.setRequestType(requestType);
                List<String> traceIDs = new ArrayList<>();

                qb = QueryBuilders.matchQuery("requestType",requestType);
                scrollResp = client.prepareSearch(InitIndexAndType.REQUEST_TRACE_RELATION_INDEX).setTypes("relation")
                        .setScroll(new TimeValue(60000))
                        .setQuery(qb)
                        .setSize(100).get();
                while(scrollResp.getHits().getHits().length != 0){ // Zero hits mark the end of the scroll and the while loop
                    hits = scrollResp.getHits().getHits();
                    log.info(String.format("The length of scroll requestType:[%s] search hits is [%d]", requestType, hits.length));
                    for (SearchHit hit : hits) {
                        //Handle the hit
                        map = hit.getSourceAsMap();
                        traceIDs.add(map.get("traceId").toString());
                    }
                    scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute().actionGet();
                }
                requestWithTraceID.setTraceIDs(traceIDs);
                requestWithTraceID.setCount(traceIDs.size());
                requestWithTraceIDList.add(requestWithTraceID);
            }
            res.setStatus(true);
            res.setMessage(String.format("Succeed to get the request with trace ids. The size of request types is [%d].", getRequestTypesRes.getRequestTypes().size()));
        }
        res.setRequestWithTraceIDList(requestWithTraceIDList);
        return res;
    }
}
