package escore.service;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ESCoreServiceImpl implements ESCoreService {
    @Autowired
    private TransportClient client;

    @Override
    public String demo() {
        StringBuilder sb = new StringBuilder();

        MatchQueryBuilder builder = QueryBuilders.matchQuery("kubernetes.container.name","ts-login-service");
        SearchResponse response = client.prepareSearch("filebeat-*").setTypes("doc").setQuery(builder).setSize(10000).get();

        SearchHit[] hits = response.getHits().getHits();
        System.out.println(String.format("The length of hits is [%d]", hits.length));
        for (SearchHit hit : hits) {
            System.out.println(hit.getSourceAsString());
            sb.append(hit.getSourceAsString());
        }
        return sb.toString();
    }
}
