package logapi.config;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;

@Configuration
public class MyConfig {
    @Autowired
    ESConfig esConfig;

    @Bean
    public TransportClient getESClient(){
        try{
            Settings settings = Settings.builder()
                    .put("cluster.name","docker-cluster")
                    .build();
            TransportClient client = new PreBuiltTransportClient(settings)
                    .addTransportAddress(new TransportAddress(InetAddress.getByName(esConfig.getHost()), esConfig.getPort()));
            return client;
        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }
}
