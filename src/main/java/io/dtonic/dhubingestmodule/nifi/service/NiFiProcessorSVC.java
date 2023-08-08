package io.dtonic.dhubingestmodule.nifi.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.dtonic.dhubingestmodule.common.component.Properties;
import io.dtonic.dhubingestmodule.nifi.client.NiFiApiClient;
import io.dtonic.dhubingestmodule.nifi.vo.PropertyVO;
import io.swagger.client.model.BundleDTO;
import io.swagger.client.model.PositionDTO;
import io.swagger.client.model.ProcessorConfigDTO;
import io.swagger.client.model.ProcessorDTO;
import io.swagger.client.model.ProcessorEntity;
import io.swagger.client.model.ProcessorRunStatusEntity;
import io.swagger.client.model.ProcessorsEntity;
import io.swagger.client.model.RevisionDTO;
import io.swagger.client.model.ProcessorRunStatusEntity.StateEnum;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
public class NiFiProcessorSVC {

    @Autowired
    private Properties properties;

    @Autowired
    private NiFiApiClient niFiClient;
    
    /**
     * Create Transmitter in Ingest Manager.
     * Transmitter is sending NGSI-LD format data to DataCore Ingest Interface.
     *
     * @return Transmitter ID
     */
    public String createTransmitter(String ingestProcessGroupId) {
        ProcessorEntity body = new ProcessorEntity();
        RevisionDTO revision = new RevisionDTO();
        revision.setVersion(0L);

        ProcessorDTO component = new ProcessorDTO();
        component.setType("org.apache.nifi.processors.standard.InvokeHTTP");

        BundleDTO bundle = new BundleDTO();
        bundle.setGroup("org.apache.nifi");
        bundle.setArtifact("nifi-standard-nar");
        bundle.setVersion(properties.getNifiVersion());
        component.setBundle(bundle);

        PositionDTO position = new PositionDTO();
        position.setX(0.0);
        position.setY(0.0);
        component.setPosition(position);
        component.setName("Transmitter");
        body.setRevision(revision);
        body.setComponent(component);

        try {
            ProcessorEntity result = niFiClient
                .getProcessGroups()
                .createProcessor(ingestProcessGroupId, body);
            if (result == null) {
                log.error("Fail to Create Transmitter Processor in Ingest Manager");
                return null;
            }
            log.info(
                "Create Transmitter in Ingest Manager : Transmitter ID = [{}]",
                result.getId()
            );
            return result.getId();
        } catch (Exception e) {
            log.error("Fail to Create Transmitter Processor in Ingest Manager", e);
            return null;
        }
    }


    /**
     * Start Transmitter
     * When finshed update properties of transmitter, must update transmitter status to RUNNING
     *
     * @param transmitterId Transmitter id
     * @return Success/Fail
     */
    public boolean updateStatusProcessor(String processorId, StateEnum status) {
        try {
            ProcessorRunStatusEntity body = new ProcessorRunStatusEntity();
            body.setState(status);
            ProcessorEntity transmitter = getProcessorEntity(processorId);
            RevisionDTO revision = transmitter.getRevision();
            body.setRevision(revision);
            body.setDisconnectedNodeAcknowledged(false);
            ProcessorEntity processorRes = niFiClient.getProcessors().updateRunStatus(processorId, body);
            if (processorRes != null) {
                log.info("Success Update Status to [{}] of Processor [{}]", status.getValue(), processorId);
                return true;
            } else {
                log.error("Fail to Update Processor Status to [{}]", status.getValue());
                return false;
            }
        } catch (Exception e) {
            log.error("Error to Update Processor Status to [{}]", status.getValue(), e);
            return false;
        }
    }
  
        /**
     * Update Transmitter Properties
     * transmitter properties update Remote URL to DataCore Ingest Interface and HTTP Method to POST
     *
     * @param transmitterId Transmitter id
     */
    public void updateTransmitter(String transmitterId) {
        try {
            ProcessorEntity transmitter = getProcessorEntity(transmitterId);
            /* Properties Update */
            Map<String, String> nifiProperties = transmitter.getComponent().getConfig().getProperties();
            nifiProperties.replace("HTTP Method", "POST");
            nifiProperties.replace(
                "Remote URL",
                properties.getDatacoreIngestUrl() +
                "/entityOperations/upsert"
            );

            /* DownStream Terminated */
            ProcessorConfigDTO config = transmitter.getComponent().getConfig();
            List<String> autoTerminatedRelationships = new ArrayList<>();
            autoTerminatedRelationships.add("Failure");
            autoTerminatedRelationships.add("No Retry");
            autoTerminatedRelationships.add("Original");
            autoTerminatedRelationships.add("Response");
            autoTerminatedRelationships.add("Retry");
            config.setAutoTerminatedRelationships(autoTerminatedRelationships);

            niFiClient.getProcessors().updateProcessor(transmitterId, transmitter);
            log.info(
                "Success Update Transmitter Properties : Transmitter ID = [{}]",
                transmitterId
            );
        } catch (Exception e) {
            log.info(
                "Fail to Update Transmitter Properties : Transmitter ID = [{}]",
                transmitterId
            );
        }
    }
    /**
     * Get Transmitter Object
     *
     * @param transmitterId Transmitter id
     * @return Transmitter Processor Entity Object
     */
    private ProcessorEntity getProcessorEntity(String processorId) {
        try {
            ProcessorEntity transmitter = niFiClient
                .getProcessors()
                .getProcessor(processorId);
            log.info("Success Get Info of Transmitter : Transmitter ID = [{}]", processorId);
            return transmitter;
        } catch (Exception e) {
            log.error("Fail to get Info of Transmitter : Transmitter ID = [{}]", processorId);
            return null;
        }
    }
    
    /**
     * Update Processor Properties and Scheduling
     *
     * @param ProcessorEntity update processor
     * @param List<PropertyVO> update properties
     * @return ProcessorEntity
     */
    public ProcessorEntity updateProcessorProperties(
        ProcessorEntity processor,
        List<PropertyVO> properies
    ) {
        Map<String, String> processorProperties = processor
            .getComponent()
            .getConfig()
            .getProperties();
        for (PropertyVO property : properies) {
            if (property.getName().equals("Scheduling")) {
                if (property.getDetail().equals("TIMER_DRIVEN")) {
                    processor.getComponent().getConfig().setSchedulingStrategy("TIMER_DRIVEN");
                    processor
                        .getComponent()
                        .getConfig()
                        .setSchedulingPeriod(property.getInputValue());
                    log.debug("{}", processor);
                } else if (property.getDetail().equals("CRON_DRIVEN")) {
                    processor.getComponent().getConfig().setSchedulingStrategy("CRON_DRIVEN");
                    processor
                        .getComponent()
                        .getConfig()
                        .setSchedulingPeriod(property.getInputValue());
                }
            } else if (property.getName().equals("root_key")) {
                if (property.getInputValue().equals("origin")) {
                    processorProperties.put(property.getName(), " ");
                } else if (property.getInputValue() != null) {
                    StringBuffer input = new StringBuffer();
                    String convertStr = property.getInputValue().replace("\"", "");
                    for (String p : convertStr.split("[.]")){
                        if (p.contains(" ")){
                            input.append("['").append(p).append("']").append(".");
                        } else {
                            input.append(p).append(".");
                        }
                    }
                    input.deleteCharAt(input.length() - 1);
                    processorProperties.put(property.getName(), input.toString());
                }
            } else if (processorProperties.get(property.getName()) == null) {
                processorProperties.put(property.getName(), property.getInputValue());
            } else {
                processorProperties.replace(property.getName(), property.getInputValue());
            }
        }
        processor.getComponent().getConfig().setProperties(processorProperties);
        return processor;
    }

    /**
     * Search Processor ID by Processor Name In Process Group
     *
     * @param rootProcessGroupId parent process group id
     * @param processorName processor name want to find
     * @return FunnelID
     */
    public String searchProcessorIdbyName(String rootProcessGroupId, String processorName) {
        ProcessorsEntity result = searchProcessorsInProcessGroup(rootProcessGroupId);
        for (ProcessorEntity processor : result.getProcessors()) {
            ProcessorDTO processorInfo = processor.getComponent();
            if (processorInfo.getName().equals(processorName)) {
                log.info(
                    "Exist Processor [{}] in {} : Processor ID = [{}]",
                    processorName,
                    rootProcessGroupId,
                    processorInfo.getId()
                );
                return processorInfo.getId();
            } else {
                log.info(
                    "Not Found In {} Processor Name : [{}]",
                    rootProcessGroupId,
                    processorName
                );
                return null;
            }
        }
        log.error("Not Found Processors In {}", rootProcessGroupId);
        return null;
    }


    public ProcessorsEntity searchProcessorsInProcessGroup(String processorGroupId) {
        try {
            ProcessorsEntity result = niFiClient
                .getProcessGroups()
                .getProcessors(processorGroupId, false);
            return result;
        } catch (Exception e) {
            return null;
        }
    }

}
