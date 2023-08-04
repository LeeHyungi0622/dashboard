package io.dtonic.dhubingestmodule.nifi.client;

import io.swagger.client.api.ConnectionsApi;
import io.swagger.client.api.FlowApi;
import io.swagger.client.api.FlowfileQueuesApi;
import io.swagger.client.api.ProcessGroupsApi;
import io.swagger.client.api.ProcessorsApi;
import io.swagger.client.api.TemplatesApi;

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

    private FlowApi flow = new FlowApi();
    private ProcessorsApi processors = new ProcessorsApi();
    private ProcessGroupsApi processGroups = new ProcessGroupsApi();
    private FlowfileQueuesApi flowfileQueues = new FlowfileQueuesApi();
    private ConnectionsApi connections = new ConnectionsApi();
    private TemplatesApi templates = new TemplatesApi();

    @PostConstruct
    public void init() {
        flow.setApiClient(niFiClientEntity.getNifiSwaggerApiClient());
        processors.setApiClient(niFiClientEntity.getNifiSwaggerApiClient());
        processGroups.setApiClient(niFiClientEntity.getNifiSwaggerApiClient());
        flowfileQueues.setApiClient(niFiClientEntity.getNifiSwaggerApiClient());
        connections.setApiClient(niFiClientEntity.getNifiSwaggerApiClient());
        templates.setApiClient(niFiClientEntity.getNifiSwaggerApiClient());
    }
}
