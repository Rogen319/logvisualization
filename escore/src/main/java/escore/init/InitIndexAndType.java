package escore.init;

import com.fasterxml.jackson.databind.ObjectMapper;
import escore.bean.NodeInfo;
import escore.bean.PodInfo;
import escore.response.GetNodesListResponse;
import escore.response.GetPodsListResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class InitIndexAndType implements CommandLineRunner {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private TransportClient client;

    public static final String K8S_INDEX_POD = "k8s_pod";
    public static final String K8S_INDEX_NODE = "k8s_node";
    public static final String TRACE_STATUS = "trace_status";
//    public static final String REQUEST_TRACE_RELATION_INDEX = "rt_relation";

    @Override
    public void run(String... strings) throws Exception {
        IndicesAdminClient indicesAdminClient = client.admin().indices();

//        indicesAdminClient.prepareDelete(K8S_INDEX_POD, K8S_INDEX_NODE, REQUEST_TRACE_RELATION_INDEX).execute().actionGet();

        //Judge if the k8s and rt indices already exists
        IndicesExistsResponse indicesExistsResponse = indicesAdminClient.prepareExists(K8S_INDEX_POD, K8S_INDEX_NODE, TRACE_STATUS).
                execute().actionGet();
        log.info(String.format("Indices [%s, %s, %s] exists? %b", K8S_INDEX_POD, K8S_INDEX_NODE, TRACE_STATUS, indicesExistsResponse.isExists()));

        //If not, create the three indices
        if(!indicesExistsResponse.isExists()){
            //Pod index
            CreateIndexResponse createIndexResponse = indicesAdminClient.prepareCreate(K8S_INDEX_POD)
                    .execute().actionGet();
            if (createIndexResponse.isAcknowledged()) {
                log.info(String.format("Index [%s] has been created successfully!", K8S_INDEX_POD));

                //Add pod type and insert pod information
                addPodType();
                insertPodData();
            } else {
                log.info(String.format("Fail to create index [%s]!", K8S_INDEX_POD));
            }

            //Node index
            createIndexResponse = indicesAdminClient.prepareCreate(K8S_INDEX_NODE)
                    .execute().actionGet();
            if (createIndexResponse.isAcknowledged()) {
                log.info(String.format("Index [%s] has been created successfully!", K8S_INDEX_NODE));

                //Add node type and insert node information
                addNodeType();
                insertNodeData();
            } else {
                log.info(String.format("Fail to create index [%s]!", K8S_INDEX_NODE));
            }

            //Request-Trace index
            createIndexResponse = indicesAdminClient.prepareCreate(TRACE_STATUS)
                    .execute().actionGet();
            if (createIndexResponse.isAcknowledged()) {
                log.info(String.format("Index [%s] has been created successfully!", TRACE_STATUS));

                //Add request trace relation type
                addStatusType();
            } else {
                log.info(String.format("Fail to create index [%s]!", TRACE_STATUS));
            }
        }
    }

    //Add node type in the node index
    private void addNodeType(){
        client.admin().indices().preparePutMapping(K8S_INDEX_NODE)
                .setType("node")
                .setSource("{\n" +
                        "  \"properties\": {\n" +
                        "    \"name\": {\n" +
                        "      \"type\": \"text\"\n" +
                        "    },\n" +
                        "    \"ip\": {\n" +
                        "      \"type\": \"text\"\n" +
                        "    },\n" +
                        "    \"status\": {\n" +
                        "      \"type\": \"text\"\n" +
                        "    },\n" +
                        "    \"kubeProxyVersion\": {\n" +
                        "      \"type\": \"text\"\n" +
                        "    },\n" +
                        "    \"kubeletVersion\": {\n" +
                        "      \"type\": \"text\"\n" +
                        "    },\n" +
                        "    \"operatingSystem\": {\n" +
                        "      \"type\": \"text\"\n" +
                        "    },\n" +
                        "    \"osImage\": {\n" +
                        "      \"type\": \"text\"\n" +
                        "    },\n" +
                        "    \"containerRuntimeVersion\": {\n" +
                        "      \"type\": \"text\"\n" +
                        "    }\n" +
                        "  }\n" +
                        "}", XContentType.JSON)
                .get();
    }

    //Insert node data in the node index
    private void insertNodeData() throws Exception{
        //Get and add nodes information
        GetNodesListResponse result = restTemplate.getForObject(
                "http://logvisualization-k8sapi:18319/api/getNodesList",
                GetNodesListResponse.class);
        if(result.isStatus()){
            ObjectMapper mapper = new ObjectMapper();
            for(NodeInfo nodeInfo : result.getNodes()){
                byte[] json = mapper.writeValueAsBytes(nodeInfo);
                client.prepareIndex(K8S_INDEX_NODE,"node").setSource(json, XContentType.JSON).get();
            }
        }else{
            log.info("Fail to get the node list information!");
        }
    }

    //Add pod type in the pod index
    private void addPodType(){
        client.admin().indices().preparePutMapping(K8S_INDEX_POD)
                .setType("pod")
                .setSource("{\n" +
                        "  \"properties\": {\n" +
                        "    \"name\": {\n" +
                        "      \"type\": \"text\"\n" +
                        "    },\n" +
                        "    \"nodeName\": {\n" +
                        "      \"type\": \"text\"\n" +
                        "    },\n" +
                        "    \"serviceName\": {\n" +
                        "      \"type\": \"text\"\n" +
                        "    },\n" +
                        "    \"status\": {\n" +
                        "      \"type\": \"text\"\n" +
                        "    },\n" +
                        "    \"nodeIP\": {\n" +
                        "      \"type\": \"text\"\n" +
                        "    },\n" +
                        "    \"podIP\": {\n" +
                        "      \"type\": \"text\"\n" +
                        "    },\n" +
                        "    \"startTime\": {\n" +
                        "      \"type\": \"text\"\n" +
                        "    },\n" +
                        "    \"labels\": {\n" +
                        "      \"type\": \"object\",\n" +
                        "      \"dynamic\": true\n" +
                        "    },\n" +
                        "    \"containers\": {\n" +
                            "  \"properties\": {\n" +
                            "    \"name\": {\n" +
                            "      \"type\": \"text\"\n" +
                            "    },\n" +
                            "    \"imageName\": {\n" +
                            "      \"type\": \"text\"\n" +
                            "    },\n" +
                            "    \"imageVersion\": {\n" +
                            "      \"type\": \"text\"\n" +
                            "    },\n" +
                            "    \"ports\": {\n" +
                                "  \"properties\": {\n" +
                                "    \"containerPort\": {\n" +
                                "      \"type\": \"float\"\n" +
                                "    },\n" +
                                "    \"protocol\": {\n" +
                                "      \"type\": \"text\"\n" +
                                "    }\n" +
                                "  }\n" +
                            "    }\n" +
                            "  }\n" +
                        "    }\n" +
                        "  }\n" +
                        "}", XContentType.JSON)
                .get();
    }

    //Insert pod data in the pod index
    private void insertPodData() throws Exception{
        //Get and add pods information
        GetPodsListResponse result = restTemplate.getForObject(
                "http://logvisualization-k8sapi:18319/api/getPodsList",
                GetPodsListResponse.class);
        if(result.isStatus()){
            ObjectMapper mapper = new ObjectMapper();
            for(PodInfo podInfo : result.getPods()){
                byte[] json = mapper.writeValueAsBytes(podInfo);
                client.prepareIndex(K8S_INDEX_POD,"pod").setSource(json, XContentType.JSON).get();
            }
        }else{
            log.info("Fail to get the pod list information!");
        }
    }

    //Add relation type in the trace_status index
    private void addStatusType(){
        client.admin().indices().preparePutMapping(TRACE_STATUS)
                .setType("status")
                .setSource("{\n" +
                        "  \"properties\": {\n" +
                        "    \"status\": {\n" +
                        "      \"type\": \"text\"\n" +
                        "    },\n" +
                        "    \"traceId\": {\n" +
                        "      \"type\": \"text\"\n" +
                        "    }\n" +
                        "  }\n" +
                        "}", XContentType.JSON)
                .get();
    }
}
