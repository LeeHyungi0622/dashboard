package io.dtonic.dhubingestmodule.nifi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.dtonic.dhubingestmodule.common.code.MonitoringCode;
import io.dtonic.dhubingestmodule.history.aop.task.TaskHistory;
import io.dtonic.dhubingestmodule.nifi.client.NiFiApiClient;
import io.swagger.client.model.ConnectableDTO;
import io.swagger.client.model.ConnectionDTO;
import io.swagger.client.model.ConnectionEntity;
import io.swagger.client.model.ConnectionsEntity;
import io.swagger.client.model.DropRequestEntity;
import io.swagger.client.model.FunnelDTO;
import io.swagger.client.model.FunnelEntity;
import io.swagger.client.model.PortDTO;
import io.swagger.client.model.PortEntity;
import io.swagger.client.model.PositionDTO;
import io.swagger.client.model.RevisionDTO;
import io.swagger.client.model.ConnectionDTO.LoadBalanceCompressionEnum;
import io.swagger.client.model.ConnectionDTO.LoadBalanceStrategyEnum;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class NiFiConnectionSVC {

    @Autowired
    private NiFiApiClient niFiClient;

    @Autowired
    private NiFiPortSVC niFiPortSVC;

    @Autowired
    private NiFiProcessGroupSVC niFiProcessGroupSVC;

    /**
     * Clear Queues in process group to delete pipeline
     *
     * @param processGroupId
     * @return success/fail boolean
     */
    @TaskHistory(taskName = MonitoringCode.CLEAR_QUEUE_PROCESSGROUP)
    public Boolean clearQueuesInProcessGroup(Integer commandId, String processorGroupId) {
        try {
            DropRequestEntity result = niFiClient.getProcessGroups().createEmptyAllConnectionsRequest(processorGroupId);
            log.info(
                "Request Clear Queues in Process Group : Process Group ID = [{}]",
                processorGroupId
            );
            if (result != null) {
                try {
                    Integer retryCnt = 0;
                    while (retryCnt < 3) {
                        Boolean res = checkclearQueuesInProcessGroup(processorGroupId , result.getDropRequest().getId());
                        if (res){
                            log.info("Success Clear Queue Pipeline : Processor Group ID = {}", processorGroupId);
                            return true;
                        } else {
                            log.warn("Nifi status exist Run Proccessor or Invalid Proccessor", res);
                            log.warn("Retry Check {} Pipeline Status : Processor Group ID = {}", retryCnt, processorGroupId);
                        }
                        /* Sleep 0.3s */
                        Thread.sleep(300);
                        retryCnt++;
                    } 
                    return false;
                } catch (InterruptedException e) {
                    log.error("Interrupt Exception", e);
                    return false;
                }
            } else {
                log.error("Fail to Clear Queue Pipeline : Processor Group ID = {}", processorGroupId);
                return false;
            }
        } catch (Exception e) {
            log.error(
                "Fail to Clear Queues in Process Group : Process Group ID = [{}]",
                processorGroupId,
                e
            );
            return false;
        }
    }

    private boolean checkclearQueuesInProcessGroup(String processorGroupId, String requestId) {
        try {
            DropRequestEntity requestResult = niFiClient.getProcessGroups().getDropAllFlowfilesRequest(processorGroupId, requestId);
            return requestResult.getDropRequest().isFinished();
        } catch (Exception e) {
            log.error(
                "Fail to Check Queues in Process Group : Process Group ID = [{}]",
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
            PositionDTO positionDTO = new PositionDTO();
            positionDTO.setX(0D);
            positionDTO.setY(0D);
            funnelDTO.setPosition(positionDTO);
            body.setComponent(funnelDTO);
            body.setRevision(revision);
            body.setDisconnectedNodeAcknowledged(false);

            FunnelEntity resultEntity = niFiClient.getProcessGroups().createFunnel(ingestProcessGroupId, body);

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
            component.setLoadBalanceCompression(LoadBalanceCompressionEnum.DO_NOT_COMPRESS);
            component.setLoadBalanceStrategy(LoadBalanceStrategyEnum.DO_NOT_LOAD_BALANCE);

            // Set up Source Funnel
            ConnectableDTO source = new ConnectableDTO();
            source.setId(funnelId);
            source.setGroupId(ingestProcessGroupId);
            source.setType(ConnectableDTO.TypeEnum.FUNNEL);
            component.setSource(source);

            // Set up Destination Transmitter
            ConnectableDTO destination = new ConnectableDTO();
            destination.setId(transmitterId);
            destination.setGroupId(ingestProcessGroupId);
            destination.setType(ConnectableDTO.TypeEnum.PROCESSOR);
            component.setDestination(destination);

            RevisionDTO revision = new RevisionDTO();
            revision.setVersion(0L);

            body.setComponent(component);
            body.setRevision(revision);
            body.setDisconnectedNodeAcknowledged(false);

            ConnectionEntity resultEntity = niFiClient.getProcessGroups().createConnection(ingestProcessGroupId, body);
            log.info(
                "Create Connection From Funnel To Transmitter In Ingest Manager : connection ID = [{}]",
                resultEntity.getId()
            );
        } catch (Exception e) {
            log.error("Fail to Create Connection From Funnel To Transmitter", e);
        }
    }
    
    /**
     * [Retrieve Pipeline Detail by id]
     * if pipeline is not exist, return 400
     * else return pipeline detail
     * @param commandId this param is using in aop task history
     * @param processGroupId this param is root process group id
     * @param pipelineName this param is using output port name
     * @return output port id
     * 
     * @since 2023. 8. 16
     * @version 1.2.0
     * @auther Justin
     */
    @TaskHistory(taskName = MonitoringCode.CREATE_OUTPUTPORT_PIPELINE)
    public String createOutputInPipeline(Integer commandId, String processGroupId, String pipelineName){
        try {
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

            PortEntity resultPortEntity = niFiClient.getProcessGroups().createOutputPort(processGroupId, body);

            log.info(
                "Create Output in in Process Group : [{}] : output PortEntity = [{}]", processGroupId,
                resultPortEntity.getId()
            );
            return resultPortEntity.getId();
        } catch (Exception e) {
            log.error("Fail to Create Output in Process Group : [{}]", processGroupId, e);
            return null;
        }
        
        
    }
    /**
     * Create Connection From Output To Funnel
     *
     * @param rootProcessGroupId pipeline process group id
     * @param sourceProcessGroupId from process group id
     * @param destinationProcessGroupId to process group id
     */
    @TaskHistory(taskName = MonitoringCode.CREATE_CONNECTION_BETWEEN_OUTPUT_FUNNEL)
    public Boolean createConnectionFromOutputToFunnel(
        Integer commandId, 
        String rootProcessGroupId,
        String sourceProcessGroupId,
        String destProcessGroupId
    ) throws Exception {
        // Search Output in Source Processor Group
        PortDTO sourceOutput = niFiPortSVC.searchOutputInProcessorGroup(sourceProcessGroupId);
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
        FunnelEntity destOutput = niFiPortSVC.searchFunnelInProcessGroup(destProcessGroupId);
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
        niFiClient.getProcessGroups().createConnection(rootProcessGroupId, body);
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
    @TaskHistory(taskName = MonitoringCode.DELETE_CONNECTION)
    public boolean deleteConnectionToFunnel(Integer commandId, String connectionId) {
        try {
            /* Get Connection Revision */
            String version = niFiClient
                .getConnections()
                .getConnection(connectionId)
                .getRevision()
                .getVersion()
                .toString();
            /* delete connection */
            niFiClient.getConnections().deleteConnection(connectionId, version, null,null);
            log.info("Success Delete Connection {} To Funnel", connectionId);
            return true;
        } catch (Exception e) {
            log.error("Fail to Delete Connection {} To Funnel", connectionId, e);
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
    @TaskHistory(taskName = MonitoringCode.CREATE_CONNECTION_BETWEEN_PROCESSGROUP_OUTPUT)
    public Boolean createConnectionFromProcessGroupToOutput(
        Integer commandId,
        String rootProcessGroupId,
        String sourceProcessGroupId,
        String destProcessGroupId
    ) throws Exception{
        // Search Output in Source Processor Group
        PortDTO sourceOutput = niFiPortSVC.searchOutputInProcessorGroup(sourceProcessGroupId);
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
        PortDTO destOutput = niFiPortSVC.searchOutputInProcessorGroup(destProcessGroupId);
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
        niFiClient.getProcessGroups().createConnection(rootProcessGroupId, body);
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
    @TaskHistory(taskName = MonitoringCode.CREATE_CONNECTION_BETWEEN_PROCESSGROUP)
    public Boolean createConnectionBetweenProcessGroup(
        Integer commandId,
        String rootProcessGroupId,
        String sourceProcessGroupId,
        String destinationProcessGroupId
    ) throws Exception{
        // Search Output in Source Processor Group
        PortDTO sourceOutput = niFiPortSVC.searchOutputInProcessorGroup(sourceProcessGroupId);
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
        PortDTO destInput = niFiPortSVC.searchInputInProcessorGroup(destinationProcessGroupId);
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
        niFiClient.getProcessGroups().createConnection(rootProcessGroupId, body);
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
    @TaskHistory(taskName = MonitoringCode.CLEAR_QUEUE_FUNNEL)
    public Boolean clearQueuesInConnectionToFunnel(Integer commandId, String sourceProcessorGroupID) {
        ConnectionsEntity connections = searchConnectionsInProcessorGroup(
            niFiProcessGroupSVC.searchProcessGroupInProcessGroup("root", "Ingest Manager")
        );
        if (connections.getConnections().size() == 0) {
            log.error("Empty Connection From Pipeline To Funnel In IngestManager");
            return null;
        } else {
            for (ConnectionEntity connection : connections.getConnections()) {
                if (connection.getSourceGroupId().equals(sourceProcessorGroupID)) {
                    try {
                        DropRequestEntity result = niFiClient.getFlowfileQueues().createDropRequest(connection.getId());
                        log.info(
                            "Request Clear Queues In Connection From {} To Transmitter",
                            sourceProcessorGroupID
                        );
                        if (result != null) {
                            try {
                                Integer retryCnt = 0;
                                while (retryCnt < 3) {
                                    // 다른 메서드 해야함
                                    Boolean res = checkClearQueueInConnection(connection.getId() , result.getDropRequest().getId());
                                    if (res){
                                        log.info("Success Clear Queue Pipeline : Processor Group ID = {}", sourceProcessorGroupID);
                                        return true;
                                    } else {
                                        log.warn("Nifi status exist Run Proccessor or Invalid Proccessor", res);
                                        log.warn("Retry Check {} Pipeline Status : Processor Group ID = {}", retryCnt, sourceProcessorGroupID);
                                    }
                                    /* Sleep 0.3s */
                                    Thread.sleep(300);
                                    retryCnt++;
                                } 
                                return false;
                            } catch (InterruptedException e) {
                                log.error("Interrupt Exception", e);
                                return false;
                            }
                        } else {
                            log.error("Fail to Clear Queue Pipeline : Processor Group ID = {}", sourceProcessorGroupID);
                            return false;
                        }
                    } catch (Exception e) {
                        log.error("Fail to Clear Queues In Connection To Funnel", e);
                        return null;
                    }
                }
            }
            log.error("Not Found Connection Processor Group : Id = [{}]", sourceProcessorGroupID);
            return null;
        }
    }

    private Boolean checkClearQueueInConnection(String connectionId, String requestId) {
        try {
            DropRequestEntity requestResult = niFiClient.getFlowfileQueues().getDropRequest(connectionId, requestId);
            return requestResult.getDropRequest().isFinished();
        } catch (Exception e) {
            log.error(
                "Fail to Chect Queues In Connection : Connection ID = [{}]",
                connectionId,
                e
            );
            return false;
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
                .getProcessGroups()
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
        try {
            ConnectionsEntity result = niFiClient
                .getProcessGroups()
                .getConnections(processorGroupId);
            return result;
        } catch (Exception e) {
            log.error("Fail to Connection in Processor Group", e);
            return null;
        }
    }
}
