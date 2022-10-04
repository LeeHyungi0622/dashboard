package io.dtonic.dhubingestmodule.nifi.client;

import com.github.hermannpencole.nifi.swagger.client.ConnectionsApi;
import com.github.hermannpencole.nifi.swagger.client.FlowApi;
import com.github.hermannpencole.nifi.swagger.client.FlowfileQueuesApi;
import com.github.hermannpencole.nifi.swagger.client.ProcessGroupsApi;
import com.github.hermannpencole.nifi.swagger.client.ProcessorsApi;
import javax.annotation.PostConstruct;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * NiFi Swegger Client Setup
 * @FileName NiFiClient.java
 * @Project D.hub Ingest Manager
 * @Brief
 * @Version 1.0
 * @Date 2022. 9. 27.
 * @Author Justin
 */
@Data
@Component
public class NiFiClient {

    @Autowired
    private final NiFiClientEntity niFiClientEntity;

    private FlowApi flowApiSwagger = new FlowApi();
    private ProcessorsApi processorsApiSwagger = new ProcessorsApi();
    private ProcessGroupsApi processGroupsApiSwagger = new ProcessGroupsApi();
    private FlowfileQueuesApi flowfileQueuesApiSwagger = new FlowfileQueuesApi();
    private ConnectionsApi connectionsApiSwagger = new ConnectionsApi();

    @PostConstruct
    public void init() {
        flowApiSwagger.setApiClient(niFiClientEntity.getNifiSwaggerApiClient());
        processorsApiSwagger.setApiClient(niFiClientEntity.getNifiSwaggerApiClient());
        processGroupsApiSwagger.setApiClient(niFiClientEntity.getNifiSwaggerApiClient());
        flowfileQueuesApiSwagger.setApiClient(niFiClientEntity.getNifiSwaggerApiClient());
        connectionsApiSwagger.setApiClient(niFiClientEntity.getNifiSwaggerApiClient());
    }
}
