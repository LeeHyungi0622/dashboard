package io.dtonic.dhubingestmodule.nifi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.hermannpencole.nifi.swagger.ApiException;
import com.github.hermannpencole.nifi.swagger.client.model.BundleDTO;
import com.github.hermannpencole.nifi.swagger.client.model.ConnectableDTO;
import com.github.hermannpencole.nifi.swagger.client.model.ConnectionDTO;
import com.github.hermannpencole.nifi.swagger.client.model.ConnectionEntity;
import com.github.hermannpencole.nifi.swagger.client.model.ConnectionsEntity;
import com.github.hermannpencole.nifi.swagger.client.model.FunnelEntity;
import com.github.hermannpencole.nifi.swagger.client.model.InputPortsEntity;
import com.github.hermannpencole.nifi.swagger.client.model.OutputPortsEntity;
import com.github.hermannpencole.nifi.swagger.client.model.PortDTO;
import com.github.hermannpencole.nifi.swagger.client.model.PortEntity;
import com.github.hermannpencole.nifi.swagger.client.model.PositionDTO;
import com.github.hermannpencole.nifi.swagger.client.model.ProcessGroupDTO;
import com.github.hermannpencole.nifi.swagger.client.model.ProcessGroupEntity;
import com.github.hermannpencole.nifi.swagger.client.model.ProcessorConfigDTO;
import com.github.hermannpencole.nifi.swagger.client.model.ProcessorDTO;
import com.github.hermannpencole.nifi.swagger.client.model.ProcessorDTO.StateEnum;
import com.github.hermannpencole.nifi.swagger.client.model.ProcessorEntity;
import com.github.hermannpencole.nifi.swagger.client.model.ProcessorsEntity;
import com.github.hermannpencole.nifi.swagger.client.model.RevisionDTO;
import com.github.hermannpencole.nifi.swagger.client.model.TemplateDTO;
import com.github.hermannpencole.nifi.swagger.client.model.TemplateEntity;
import com.github.hermannpencole.nifi.swagger.client.model.TemplatesEntity;
import io.dtonic.dhubingestmodule.common.component.Properties;
import io.dtonic.dhubingestmodule.nifi.client.NiFiClient;
import io.dtonic.dhubingestmodule.nifi.vo.NiFiComponentVO;
import io.dtonic.dhubingestmodule.nifi.vo.PropertyVO;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NiFiSwaggerSVC {

    @Autowired
    private NiFiClient niFiClient;

    @Autowired
    private ObjectMapper nifiObjectMapper;

    public String searchTempletebyName(String TempleteName) {
        TemplatesEntity result = niFiClient.getFlowApiSwagger().getTemplates();
        List<TemplateEntity> templateList = result.getTemplates();
        for (TemplateEntity template : templateList) {
            TemplateDTO templateinfo = template.getTemplate();
            String installTemplateName = templateinfo.getName();
            if (TempleteName.equals(installTemplateName)) {
                return templateinfo.getId();
            } else {
                log.error("Not Found Template Name : [{}]", TempleteName);
                return null;
            }
        }
        return null;
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

    public String createProcessGroup(String pipelineName, String rootProcessorGroupId) {
        ProcessGroupEntity body = new ProcessGroupEntity();
        ProcessGroupDTO processorGroupSetting = new ProcessGroupDTO();
        RevisionDTO processorGroupRevision = new RevisionDTO();

        processorGroupSetting.setName(pipelineName);

        processorGroupRevision.setVersion(0L);
        processorGroupRevision.setClientId(rootProcessorGroupId);

        body.setComponent(processorGroupSetting);
        body.setRevision(processorGroupRevision);

        ProcessGroupEntity result = niFiClient
            .getProcessGroupsApiSwagger()
            .createProcessGroup(rootProcessorGroupId, body);
        return result.getId();
    }

    public String createOutputInPipeline(String processGroupId, String pipelineName) {
        PortEntity body = new PortEntity();
        PortDTO component = body.getComponent();
        component.setName(pipelineName + " output");

        PositionDTO position = new PositionDTO();
        position.setX(0.0);
        position.setY(0.0);
        component.setPosition(position);

        body.setComponent(component);
        try {
            PortEntity result = niFiClient
                .getProcessGroupsApiSwagger()
                .createOutputPort(processGroupId, body);
            log.info("Create Output in Pipeline : output PortEntity= {}", result);
            return result.getId();
        } catch (ApiException e) {
            log.error("Fail to Create Output in Pipeline : Pipeline Name = [{}]", pipelineName);
            e.printStackTrace();
            return null;
        }
    }

    public ProcessorsEntity searchProcessorsInProcessGroup(String processorGroupId) {
        ProcessorsEntity result = niFiClient
            .getProcessGroupsApiSwagger()
            .getProcessors(processorGroupId, false);
        return result;
    }

    public ConnectionsEntity searchConnectionsInProcessorGroup(String processorGroupId) {
        ConnectionsEntity result = niFiClient
            .getProcessGroupsApiSwagger()
            .getConnections(processorGroupId);
        return result;
    }

    /**
     * Search Funnel In Ingest Manager
     * When search funnel, get first element of funnels array because one funnel exist in Ingest Manager
     *
     * @param sourceProcessorGroupID pipeline processor group id
     * @return FunnelID
     */
    public String searchFunnelInProcessGroup(String processGroupId) {
        for (FunnelEntity funnel : niFiClient
            .getProcessGroupsApiSwagger()
            .getFunnels(processGroupId)
            .getFunnels()) {
            log.info("Search Funnel In Ingest Manager : Funnel ID = [{}]", funnel.getId());
            return funnel.getId();
        }
        log.info("Empty Funnel In Ingest Manager");
        return null;
    }

    /**
     * Clear Queues in Connection from pipeline to Funnel
     * When delete pipeline process group, must clear queues in connection between pipeline process group and funnel.
     *
     * @param sourceProcessorGroupID pipeline processor group id
     * @return String connectionId
     */
    protected String clearQueuesInConnectionToFunnel(String sourceProcessorGroupID) {
        ConnectionsEntity connections = searchConnectionsInProcessorGroup("root");
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
                processor.getComponent().getConfig().setSchedulingPeriod(property.getInputValue());
            } else {
                processorProperties.replace(property.getName(), property.getInputValue());
            }
        }
        processor.getComponent().getConfig().setProperties(processorProperties);
        return processor;
    }

    public void updateProcessorsInAdaptor(
        String processorGroupId,
        NiFiComponentVO niFiComponentVO
    ) {
        ProcessorsEntity processors = searchProcessorsInProcessGroup(processorGroupId);
        if (processors.getProcessors().size() == 0) {
            log.error(
                "Empty Processor in Processor Group : Processor Group ID = {}",
                processorGroupId
            );
        } else {
            for (ProcessorEntity processor : processors.getProcessors()) {
                if (processor.getComponent().getName().equals(niFiComponentVO.getName())) {
                    ProcessorEntity updateProcessor = updateProcessorProperties(
                        processor,
                        niFiComponentVO.getProperties()
                    );
                    niFiClient
                        .getProcessorsApiSwagger()
                        .updateProcessor(updateProcessor.getId(), updateProcessor);
                }
            }
        }
    }

    public void createConnectionBetweenProcessGroup(
        String rootProcessGroupId,
        String sourceProcessGroupId,
        String destinationProcessGroupId
    ) {
        // Search Output in Source Processor Group
        PortDTO sourceOutput = searchOutputInProcessorGroup(sourceProcessGroupId);
        // Create connection
        ConnectionEntity body = new ConnectionEntity();
        ConnectionDTO component = body.getComponent();
        component.setFlowFileExpiration("0 sec");
        component.setBackPressureDataSizeThreshold("1 GB");
        component.setBackPressureObjectThreshold(10000L);
        // component.setload
        ConnectableDTO source = component.getSource();
        source.setId(sourceOutput.getId());
        source.setGroupId(sourceOutput.getParentGroupId());
        ConnectableDTO.TypeEnum sourceType = nifiObjectMapper.convertValue(
            sourceOutput.getType(),
            ConnectableDTO.TypeEnum.class
        );
        source.setType(sourceType);

        // Search Input in Destination Processor Group
        PortDTO destInput = searchInputInProcessorGroup(destinationProcessGroupId);
        ConnectableDTO dest = component.getDestination();
        dest.setId(destInput.getId());
        dest.setGroupId(destInput.getParentGroupId());
        ConnectableDTO.TypeEnum destType = nifiObjectMapper.convertValue(
            destInput.getType(),
            ConnectableDTO.TypeEnum.class
        );
        dest.setType(destType);

        body.setComponent(component);
        niFiClient.getProcessGroupsApiSwagger().createConnection(rootProcessGroupId, body);
    }

    /**
     * Create connection from Funnel to Transmitter.
     *
     * @param funnelId Funnel id
     * @param transmitterId Transmitter id
     */
    public void createConnectionFromFunnelToTransmitter(String funnelId, String transmitterId) {
        try {
            // Create connection
            ConnectionEntity body = new ConnectionEntity();
            ConnectionDTO component = body.getComponent();
            component.setFlowFileExpiration("0 sec");
            component.setBackPressureDataSizeThreshold("1 GB");
            component.setBackPressureObjectThreshold(10000L);

            // Set up Source Funnel
            ConnectableDTO source = component.getSource();
            source.setId(funnelId);
            source.setGroupId("root");
            source.setType(ConnectableDTO.TypeEnum.FUNNEL);

            // Set up Destination Transmitter
            ConnectableDTO destination = component.getDestination();
            destination.setId(transmitterId);
            destination.setGroupId("root");
            destination.setType(ConnectableDTO.TypeEnum.PROCESSOR);

            body.setComponent(component);
            niFiClient.getProcessGroupsApiSwagger().createConnection("root", body);
            log.info("Create Connection From Funnel To Transmitter");
        } catch (Exception e) {
            log.error("Fail to Create Connection From Funnel To Transmitter");
        }
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
            return true;
        } catch (Exception e) {
            log.error("Fail to Delete Connection From {} To Transmitter", sourceProcessorGroupID);
            return false;
        }
    }

    public PortDTO searchOutputInProcessorGroup(String processGroupId) {
        try {
            OutputPortsEntity outputPorts = niFiClient
                .getProcessGroupsApiSwagger()
                .getOutputPorts(processGroupId);
            if (outputPorts.getOutputPorts().size() == 0) {
                log.error(
                    "Empty Output Port in Processor Group : Processor Group ID = {}",
                    processGroupId
                );
                return null;
            } else if (outputPorts.getOutputPorts().size() > 1) {
                log.error(
                    "Too Many (2 or more) Output Port in Processor Group : Template ID = {}",
                    processGroupId
                );
                return null;
            } else {
                for (PortEntity port : outputPorts.getOutputPorts()) {
                    PortDTO portComponent = port.getComponent();
                    return portComponent;
                }
            }
            return null;
        } catch (ApiException e) {
            log.error("Fail to Search output port in processor Group.", e);
            e.printStackTrace();
            return null;
        }
    }

    public PortDTO searchInputInProcessorGroup(String processGroupId) {
        try {
            InputPortsEntity inputPorts = niFiClient
                .getProcessGroupsApiSwagger()
                .getInputPorts(processGroupId);
            if (inputPorts.getInputPorts().size() == 0) {
                log.error(
                    "Empty Input Port in Processor Group : Processor Group ID = {}",
                    processGroupId
                );
                return null;
            } else if (inputPorts.getInputPorts().size() > 1) {
                log.error(
                    "Too Many (2 or more) Input Port in Processor Group : Template ID = {}",
                    processGroupId
                );
                return null;
            } else {
                for (PortEntity port : inputPorts.getInputPorts()) {
                    PortDTO portComponent = port.getComponent();
                    return portComponent;
                }
            }
            return null;
        } catch (ApiException e) {
            log.error("Fail to Search input port in processor Group.", e);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Create Transmitter in Ingest Manager.
     * Transmitter is sending NGSI-LD format data to DataCore Ingest Interface.
     *
     * @return Transmitter ID
     */
    public String createTransmitter() {
        ProcessorEntity body = new ProcessorEntity();
        RevisionDTO revision = new RevisionDTO();
        revision.setVersion(0L);

        ProcessorDTO component = new ProcessorDTO();
        component.setType("org.apache.nifi.processors.standard.InvokeHTTP");

        BundleDTO bundle = new BundleDTO();
        bundle.setGroup("org.apache.nifi");
        bundle.setArtifact("nifi-standard-nar");
        bundle.setVersion("1.16.3");
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
                .getProcessGroupsApiSwagger()
                .createProcessor("root", body);
            log.info(
                "Create Transmitter in Ingest Manager : Transmitter ID = [{}]",
                result.getId()
            );
            return result.getId();
        } catch (ApiException e) {
            log.error("Fail to Create Transmitter Processor in Ingest Manager");
            return null;
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
            ProcessorEntity transmitter = getTransmitter(transmitterId);
            /* Properties Update */
            Map<String, String> properties = transmitter.getComponent().getConfig().getProperties();
            properties.replace("HTTP Method", "POST");
            properties.replace(
                "Remote URL",
                niFiClient.getNiFiClientEntity().getProperties().getDatacoreIngestUrl()
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

            niFiClient.getProcessorsApiSwagger().updateProcessor(transmitterId, transmitter);
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
    private ProcessorEntity getTransmitter(String transmitterId) {
        try {
            ProcessorEntity transmitter = niFiClient
                .getProcessorsApiSwagger()
                .getProcessor(transmitterId);
            log.info("Success Get Info of Transmitter : Transmitter ID = [{}]", transmitterId);
            return transmitter;
        } catch (Exception e) {
            log.error("Fail to get Info of Transmitter : Transmitter ID = [{}]", transmitterId);
            return null;
        }
    }
}
