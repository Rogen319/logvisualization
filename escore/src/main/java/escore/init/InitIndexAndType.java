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
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class InitIndexAndType implements CommandLineRunner {
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private TransportClient client;

    public static final String K8S_INDEX_POD = "k8s_pod";
    public static final String K8S_INDEX_NODE = "k8s_node";

    @Override
    public void run(String... strings) throws Exception {
        IndicesAdminClient indicesAdminClient = client.admin().indices();

//        indicesAdminClient.prepareDelete(K8S_INDEX_NODE,K8S_INDEX_POD).execute().actionGet();

        //Judge if the k8s indices already exists
        IndicesExistsResponse indicesExistsResponse = indicesAdminClient.prepareExists(K8S_INDEX_POD,K8S_INDEX_NODE).
                execute().actionGet();
        System.out.println(String.format("Indices [%s, %s] exists? %b", K8S_INDEX_POD, K8S_INDEX_NODE, indicesExistsResponse.isExists()));

        //If not, create the two indices
        if(!indicesExistsResponse.isExists()){
            //Pod index
            CreateIndexResponse createIndexResponse = indicesAdminClient.prepareCreate(K8S_INDEX_POD)
                    .execute().actionGet();
            if (createIndexResponse.isAcknowledged()) {
                System.out.println(String.format("Index [%s] has been created successfully!", K8S_INDEX_POD));

                //Add pod type and insert pod information
                addPodType();
                insertPodData();
            } else {
                System.out.println(String.format("Fail to create index [%s]!", K8S_INDEX_POD));
            }

            //Node index
            createIndexResponse = indicesAdminClient.prepareCreate(K8S_INDEX_NODE)
                    .execute().actionGet();
            if (createIndexResponse.isAcknowledged()) {
                System.out.println(String.format("Index [%s] has been created successfully!", K8S_INDEX_NODE));

                //Add node type and insert node information
                addNodeType();
                insertNodeData();
            } else {
                System.out.println(String.format("Fail to create index [%s]!", K8S_INDEX_NODE));
            }
        }
    }

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
            System.out.println("Fail to get the node list information!");
        }
    }

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
            System.out.println("Fail to get the pod list information!");
        }
    }
}
