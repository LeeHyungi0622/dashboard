package io.dtonic.dhubingestmodule.nifi.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import io.swagger.client.model.ActivateControllerServicesEntity;
import io.swagger.client.model.ConnectableDTO;
import io.swagger.client.model.ConnectionDTO;
import io.swagger.client.model.ConnectionEntity;
import io.swagger.client.model.ConnectionsEntity;
import io.swagger.client.model.FunnelDTO;
import io.swagger.client.model.FunnelEntity;
import io.swagger.client.model.PortDTO;
import io.swagger.client.model.PortEntity;
import io.swagger.client.model.PositionDTO;
import io.swagger.client.model.RevisionDTO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class NiFiConnectionSVC {
    
    /**
     * Clear Queues in process group to delete pipeline
     *
     * @param processGroupId
     * @return success/fail boolean
     */
    public ResponseEntity<String> clearQueuesInProcessGroup(String processorGroupId) {
        try {
            ActivateControllerServicesEntity body = new ActivateControllerServicesEntity();
            body.setState("ENABLED");
            body.setId(processorGroupId);
            body.setDisconnectedNodeAcknowledged(false);

            List<String> paths = new ArrayList<String>();
            paths.add("process-groups");
            paths.add(processorGroupId);
            paths.add("empty-all-connections-requests");

            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Content-Type", "application/json");

            ResponseEntity<String> result = dataCoreRestSVC.post(
                niFiClientEntity.getProperties().getNifiUrl() + niFiClientEntity.getBASE_URL(),
                paths,
                headers,
                body,
                null,
                niFiClientEntity.getAccessToken(),
                String.class
            );
            log.info(
                "Success Clear Queues in Process Group : Process Group ID = [{}]",
                processorGroupId
            );
            return result;
        } catch (Exception e) {
            log.error(
                "Fail to Clear Queues in Process Group : Process Group ID = [{}]",
                processorGroupId,
                e
            );
            return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
        }
    }


    public boolean checkclearQueuesInProcessGroup(String processorGroupId, String requestId) {
        try {
            List<String> paths = new ArrayList<String>();
            paths.add("process-groups");
            paths.add(processorGroupId);
            paths.add("empty-all-connections-requests");
            paths.add(requestId);

            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Content-Type", "application/json");

            ResponseEntity<String> result = dataCoreRestSVC.get(
                niFiClientEntity.getProperties().getNifiUrl() + niFiClientEntity.getBASE_URL(),
                paths,
                headers,
                null,
                null,
                niFiClientEntity.getAccessToken(),
                String.class
            );
            log.info(
                "Success Clear Queues in Process Group : Process Group ID = [{}]",
                processorGroupId
            );
            
            DropRequestEntity requestResult = nifiObjectMapper.readValue(result.getBody(), DropRequestEntity.class);
        
            return requestResult.getDropRequest().isFinished();
        
        } catch (Exception e) {
            log.error(
                "Fail to Clear Queues in Process Group : Process Group ID = [{}]",
                processorGroupId,
                e
            );
            return false;
        }
    }

    

    /**
     * Create Funnel to connect Transmitter In Ingest Manager
     *
     * @return Funnel ID
     */
    public String createFunnel(String ingestProcessGroupId) {
        try {
            FunnelEntity body = new FunnelEntity();
            FunnelDTO funnelDTO = new FunnelDTO();
            RevisionDTO revision = new RevisionDTO();
            revision.setVersion(0L);
            PositionDTO positionDTO = new PositionDTO(0D, 0D);
            funnelDTO.setPosition(positionDTO);
            body.setComponent(funnelDTO);
            body.setRevision(revision);
            body.setDisconnectedNodeAcknowledged(false);

            List<String> paths = new ArrayList<String>();
            paths.add("process-groups");
            paths.add(ingestProcessGroupId);
            paths.add("funnels");

            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Content-Type", "application/json");

            ResponseEntity<String> result = dataCoreRestSVC.post(
                niFiClientEntity.getProperties().getNifiUrl() + niFiClientEntity.getBASE_URL(),
                paths,
                headers,
                body,
                null,
                niFiClientEntity.getAccessToken(),
                String.class
            );
            FunnelEntity resultEntity = nifiObjectMapper.readValue(
                result.getBody(),
                FunnelEntity.class
            );
            log.info("Create Funnel In Ingest Manager : Funnel ID = [{}]", resultEntity.getId());
            return resultEntity.getId();
        } catch (Exception e) {
            log.error("Fail to Create Funnel In Ingest Manager", e);
            return null;
        }
    }

    /**
     * Create connection from Funnel to Transmitter.
     *
     * @param ingestProcessGroupId ingestProcessGroup id
     * @param funnelId Funnel id
     * @param transmitterId Transmitter id
     */
    public void createConnectionFromFunnelToTransmitter(
        String ingestProcessGroupId,
        String funnelId,
        String transmitterId
    ) {
        try {
            // Create connection
            ConnectionEntity body = new ConnectionEntity();
            ConnectionDTO component = new ConnectionDTO();
            component.setFlowFileExpiration("0 sec");
            component.setBackPressureDataSizeThreshold("1 GB");
            component.setBackPressureObjectThreshold(10000L);
            component.setFlowFileExpiration("0 sec");
            component.setLoadBalanceCompression("DO_NOT_COMPRESS");
            component.setLoadBalanceStrategy("DO_NOT_LOAD_BALANCE");

            // Set up Source Funnel
            ConnectableDTO source = new ConnectableDTO();
            source.setId(funnelId);
            source.setGroupId(ingestProcessGroupId);
            source.setType("FUNNEL");
            component.setSource(source);

            // Set up Destination Transmitter
            ConnectableDTO destination = new ConnectableDTO();
            destination.setId(transmitterId);
            destination.setGroupId(ingestProcessGroupId);
            destination.setType("PROCESSOR");
            component.setDestination(destination);

            RevisionDTO revision = new RevisionDTO();
            revision.setVersion(0L);

            body.setComponent(component);
            body.setRevision(revision);
            body.setDisconnectedNodeAcknowledged(false);
            List<String> paths = new ArrayList<String>();
            paths.add("process-groups");
            paths.add(ingestProcessGroupId);
            paths.add("connections");

            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Content-Type", "application/json");

            ResponseEntity<String> result = dataCoreRestSVC.post(
                niFiClientEntity.getProperties().getNifiUrl() + niFiClientEntity.getBASE_URL(),
                paths,
                headers,
                body,
                null,
                niFiClientEntity.getAccessToken(),
                String.class
            );
            ConnectionEntity resultEntity = nifiObjectMapper.readValue(
                result.getBody(),
                ConnectionEntity.class
            );
            log.info(
                "Create Connection From Funnel To Transmitter In Ingest Manager : connection ID = [{}]",
                resultEntity.getId()
            );
        } catch (Exception e) {
            log.error("Fail to Create Connection From Funnel To Transmitter", e);
        }
    }
    
    /**
     * Create Output Port In Pipeline Process Group
     *
     * @param processGroupId pipeline processor group id
     * @param pipelineName pipeline name
     * @return output ID
     */
    public String createOutputInPipeline(String processGroupId, String pipelineName) throws Exception{
        PortEntity body = new PortEntity();
        PortDTO component = new PortDTO();
        component.setName(pipelineName + " output");
        component.setAllowRemoteAccess(false);

        PositionDTO position = new PositionDTO();
        position.setX(2220.0);
        position.setY(72.0);
        component.setPosition(position);

        RevisionDTO revision = new RevisionDTO();
        revision.setVersion(0L);

        body.setRevision(revision);
        body.setComponent(component);

        List<String> paths = new ArrayList<String>();
        Map<String, String> headers = new HashMap<String, String>();

        headers.put("Content-Type", "application/json");

        paths.add("process-groups");
        paths.add(processGroupId);
        paths.add("output-ports");
        
        ResponseEntity<String> result = dataCoreRestSVC.post(
            niFiClientEntity.getProperties().getNifiUrl() + niFiClientEntity.getBASE_URL(),
            paths,
            headers,
            body,
            null,
            niFiClientEntity.getAccessToken(),
            String.class
        );

        PortEntity resultPortEntity = nifiObjectMapper.readValue(
            result.getBody(),
            PortEntity.class
        );

        log.info(
            "Create Output in Pipeline : output PortEntity = [{}]",
            resultPortEntity.getId()
        );
        return resultPortEntity.getId();
        
    }
/**
     * Create Connection From Output To Funnel
     *
     * @param rootProcessGroupId pipeline process group id
     * @param sourceProcessGroupId from process group id
     * @param destinationProcessGroupId to process group id
     */
    public Boolean createConnectionFromOutputToFunnel(
        String rootProcessGroupId,
        String sourceProcessGroupId,
        String destProcessGroupId
    ) throws Exception {
        // Search Output in Source Processor Group
        PortDTO sourceOutput = searchOutputInProcessorGroup(sourceProcessGroupId);
        // Create connection
        ConnectionEntity body = new ConnectionEntity();
        ConnectionDTO component = new ConnectionDTO();
        component.setFlowFileExpiration("0 sec");
        component.setBackPressureDataSizeThreshold("1 GB");
        component.setBackPressureObjectThreshold(10000L);
        // component.setload
        ConnectableDTO source = new ConnectableDTO();
        source.setId(sourceOutput.getId());
        source.setGroupId(sourceOutput.getParentGroupId());
        source.setType(ConnectableDTO.TypeEnum.OUTPUT_PORT);
        component.setSource(source);
        FunnelEntity destOutput = searchFunnelInProcessGroup(destProcessGroupId);
        // Search Input in Destination Funnel
        ConnectableDTO dest = new ConnectableDTO();
        dest.setId(destOutput.getId());
        dest.setGroupId(destOutput.getComponent().getParentGroupId());
        dest.setType(ConnectableDTO.TypeEnum.FUNNEL);
        component.setDestination(dest);
        body.setComponent(component);
        // Set up Revision
        RevisionDTO revision = new RevisionDTO();
        revision.setVersion(0L);
        body.setRevision(revision);
        niFiClient.getProcessGroupsApiSwagger().createConnection(rootProcessGroupId, body);
        log.info(
            "Success Create Connection From {} To {} in {}",
            sourceProcessGroupId,
            destProcessGroupId,
            rootProcessGroupId
        );
        return true;
    }
    
    /**
     * Delete connection from pipeline to transmitter.
     *
     * @param sourceProcessorGroupID pipeline processor group id
     * @return success/fail
     */
    public boolean deleteConnectionToFunnel(String sourceProcessorGroupID) {
        try {
            /* Empty Queues In Connection */
            String connectionId = clearQueuesInConnectionToFunnel(sourceProcessorGroupID);
            /* Get Connection Revision */
            String version = niFiClient
                .getConnectionsApiSwagger()
                .getConnection(connectionId)
                .getRevision()
                .getVersion()
                .toString();
            /* delete connection */
            niFiClient.getConnectionsApiSwagger().deleteConnection(connectionId, version, null);
            log.info("Success Delete Connection From {} To Funnel", sourceProcessorGroupID);
            return true;
        } catch (Exception e) {
            log.error("Fail to Delete Connection From {} To Funnel", sourceProcessorGroupID, e);
            return false;
        }
    }

    /**
     * Create Connection From Process Group To Output
     *
     * @param rootProcessGroupId pipeline process group id
     * @param sourceProcessGroupId from process group id
     * @param destinationProcessGroupId to process group id
     */
    public Boolean createConnectionFromProcessGroupToOutput(
        String rootProcessGroupId,
        String sourceProcessGroupId,
        String destProcessGroupId
    ) throws Exception{
        // Search Output in Source Processor Group
        PortDTO sourceOutput = searchOutputInProcessorGroup(sourceProcessGroupId);
        // Create connection
        ConnectionEntity body = new ConnectionEntity();
        ConnectionDTO component = new ConnectionDTO();
        component.setFlowFileExpiration("0 sec");
        component.setBackPressureDataSizeThreshold("1 GB");
        component.setBackPressureObjectThreshold(10000L);
        // component.setload
        ConnectableDTO source = new ConnectableDTO();
        source.setId(sourceOutput.getId());
        source.setGroupId(sourceOutput.getParentGroupId());
        source.setType(ConnectableDTO.TypeEnum.OUTPUT_PORT);
        component.setSource(source);
        PortDTO destOutput = searchOutputInProcessorGroup(destProcessGroupId);
        // Search Input in Destination Funnel
        ConnectableDTO dest = new ConnectableDTO();
        dest.setId(destOutput.getId());
        dest.setGroupId(destOutput.getParentGroupId());
        dest.setType(ConnectableDTO.TypeEnum.OUTPUT_PORT);
        component.setDestination(dest);
        body.setComponent(component);
        // Set up Revision
        RevisionDTO revision = new RevisionDTO();
        revision.setVersion(0L);
        body.setRevision(revision);
        niFiClient.getProcessGroupsApiSwagger().createConnection(rootProcessGroupId, body);
        log.info(
            "Success Create Connection From {} To {} in {}",
            sourceProcessGroupId,
            destProcessGroupId,
            rootProcessGroupId
        );
        return true;
    }
    /**
     * Create Connection Between Process Group Input/Output
     *
     * @param rootProcessGroupId pipeline process group id
     * @param sourceProcessGroupId from process group id
     * @param destinationProcessGroupId to process group id
     */
    public Boolean createConnectionBetweenProcessGroup(
        String rootProcessGroupId,
        String sourceProcessGroupId,
        String destinationProcessGroupId
    ) throws Exception{
        // Search Output in Source Processor Group
        PortDTO sourceOutput = searchOutputInProcessorGroup(sourceProcessGroupId);
        // Create connection
        ConnectionEntity body = new ConnectionEntity();
        ConnectionDTO component = new ConnectionDTO();
        component.setFlowFileExpiration("0 sec");
        component.setBackPressureDataSizeThreshold("1 GB");
        component.setBackPressureObjectThreshold(10000L);
        // component.setload
        ConnectableDTO source = new ConnectableDTO();
        source.setId(sourceOutput.getId());
        source.setGroupId(sourceOutput.getParentGroupId());
        source.setType(ConnectableDTO.TypeEnum.OUTPUT_PORT);
        component.setSource(source);
        // Search Input in Destination Processor Group
        PortDTO destInput = searchInputInProcessorGroup(destinationProcessGroupId);
        ConnectableDTO dest = new ConnectableDTO();
        dest.setId(destInput.getId());
        dest.setGroupId(destInput.getParentGroupId());

        dest.setType(ConnectableDTO.TypeEnum.INPUT_PORT);
        component.setDestination(dest);
        body.setComponent(component);
        // Set up Revision
        RevisionDTO revision = new RevisionDTO();
        revision.setVersion(0L);
        body.setRevision(revision);
        niFiClient.getProcessGroupsApiSwagger().createConnection(rootProcessGroupId, body);
        log.info(
            "Success Create Connection From {} To {} in {}",
            sourceProcessGroupId,
            destinationProcessGroupId,
            rootProcessGroupId
        );
        return true;
    }

    /**
     * Clear Queues in Connection from pipeline to Funnel
     * When delete pipeline process group, must clear queues in connection between pipeline process group and funnel.
     *
     * @param sourceProcessorGroupID pipeline processor group id
     * @return String connectionId
     */
    public String clearQueuesInConnectionToFunnel(String sourceProcessorGroupID) {
        ConnectionsEntity connections = searchConnectionsInProcessorGroup(
            searchProcessGroupInProcessGroup("root", "Ingest Manager")
        );
        if (connections.getConnections().size() == 0) {
            log.error("Empty Connection From Pipeline To Funnel In IngestManager");
            return null;
        } else {
            for (ConnectionEntity connection : connections.getConnections()) {
                if (connection.getSourceGroupId().equals(sourceProcessorGroupID)) {
                    niFiClient.getFlowfileQueuesApiSwagger().createDropRequest(connection.getId());
                    log.info(
                        "Success Clear Queues In Connection From {} To Transmitter",
                        sourceProcessorGroupID
                    );
                    return connection.getId();
                }
            }
            log.error("Not Found Connection Processor Group : Id = [{}]", sourceProcessorGroupID);
            return null;
        }
    }
    
    /**
     * Searching Exist Connection to Processor
     * Check Connection of processor
     *
     * @param rootProcessGroupId processor group id of parent processor
     * @param processorId Find processor id
     * @return Exist/No Exist
     */
    public boolean isExistedConnectionToProcessor(String rootProcessGroupId, String processorId) {
        try {
            ConnectionsEntity result = niFiClient
                .getProcessGroupsApiSwagger()
                .getConnections(rootProcessGroupId);
            for (ConnectionEntity connection : result.getConnections()) {
                if (connection.getDestinationId().equals(processorId)) {
                    log.info("Found Connection To {}", processorId);
                    return true;
                }
            }
            log.info("Not Found Connection To {}", processorId);
            return false;
        } catch (Exception e) {
            log.info("Fail to Found Connection To {}", processorId);
            return false;
        }
    }

    public ConnectionsEntity searchConnectionsInProcessorGroup(String processorGroupId) {
        ConnectionsEntity result = niFiClient
            .getProcessGroupsApiSwagger()
            .getConnections(processorGroupId);
        return result;
    }
}
