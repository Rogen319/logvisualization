package escore.init;

import com.fasterxml.jackson.databind.ObjectMapper;
import escore.bean.NodeInfo;
import escore.bean.PodInfo;
import escore.response.GetNodesListResponse;
import escore.response.GetPodsListResponse;
import escore.util.Const;
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

    @Override
    public void run(String... strings) throws Exception {
        IndicesAdminClient indicesAdminClient = client.admin().indices();

//        indicesAdminClient.prepareDelete(K8S_INDEX_POD, K8S_INDEX_NODE, REQUEST_TRACE_RELATION_INDEX).execute().actionGet();

        //Judge if the k8s and rt indices already exists
        IndicesExistsResponse indicesExistsResponse = indicesAdminClient.prepareExists(Const.K8S_POD_INDEX, Const.K8S_NODE_INDEX, Const.TRACE_STATUS_INDEX).
                execute().actionGet();
        log.info(String.format("Indices [%s, %s, %s] exists? %b", Const.K8S_POD_INDEX, Const.K8S_NODE_INDEX, Const.TRACE_STATUS_INDEX, indicesExistsResponse.isExists()));

        //If not, create the three indices
        if(!indicesExistsResponse.isExists()){
            //Pod index
            CreateIndexResponse createIndexResponse = indicesAdminClient.prepareCreate(Const.K8S_POD_INDEX)
                    .execute().actionGet();
            if (createIndexResponse.isAcknowledged()) {
                log.info(String.format("Index [%s] has been created successfully!", Const.K8S_POD_INDEX));

                //Add pod type and insert pod information
                addPodType();
                insertPodData();
            } else {
                log.info(String.format("Fail to create index [%s]!", Const.K8S_POD_INDEX));
            }

            //Node index
            createIndexResponse = indicesAdminClient.prepareCreate(Const.K8S_NODE_INDEX)
                    .execute().actionGet();
            if (createIndexResponse.isAcknowledged()) {
                log.info(String.format("Index [%s] has been created successfully!", Const.K8S_NODE_INDEX));

                //Add node type and insert node information
                addNodeType();
                insertNodeData();
            } else {
                log.info(String.format("Fail to create index [%s]!", Const.K8S_NODE_INDEX));
            }

            //Request-Trace index
            createIndexResponse = indicesAdminClient.prepareCreate(Const.TRACE_STATUS_INDEX)
                    .execute().actionGet();
            if (createIndexResponse.isAcknowledged()) {
                log.info(String.format("Index [%s] has been created successfully!", Const.TRACE_STATUS_INDEX));

                //Add request trace relation type
                addStatusType();
            } else {
                log.info(String.format("Fail to create index [%s]!", Const.TRACE_STATUS_INDEX));
            }
        }
    }

    //Add node type in the node index
    private void addNodeType(){
        client.admin().indices().preparePutMapping(Const.K8S_NODE_INDEX)
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
                client.prepareIndex(Const.K8S_NODE_INDEX,"node").setSource(json, XContentType.JSON).get();
            }
        }else{
            log.info("Fail to get the node list information!");
        }
    }

    //Add pod type in the pod index
    private void addPodType(){
        client.admin().indices().preparePutMapping(Const.K8S_POD_INDEX)
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
                client.prepareIndex(Const.K8S_POD_INDEX,"pod").setSource(json, XContentType.JSON).get();
            }
        }else{
            log.info("Fail to get the pod list information!");
        }
    }

    //Add relation type in the trace_status index
    private void addStatusType(){
        client.admin().indices().preparePutMapping(Const.TRACE_STATUS_INDEX)
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
