package io.dtonic.dhubingestmodule.nifi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.hermannpencole.nifi.swagger.ApiException;
import com.github.hermannpencole.nifi.swagger.client.model.BundleDTO;
import com.github.hermannpencole.nifi.swagger.client.model.ConnectableDTO;
import com.github.hermannpencole.nifi.swagger.client.model.ConnectionDTO;
import com.github.hermannpencole.nifi.swagger.client.model.ConnectionEntity;
import com.github.hermannpencole.nifi.swagger.client.model.ConnectionsEntity;
import com.github.hermannpencole.nifi.swagger.client.model.InputPortsEntity;
import com.github.hermannpencole.nifi.swagger.client.model.OutputPortsEntity;
import com.github.hermannpencole.nifi.swagger.client.model.PortDTO;
import com.github.hermannpencole.nifi.swagger.client.model.PortEntity;
import com.github.hermannpencole.nifi.swagger.client.model.PositionDTO;
import com.github.hermannpencole.nifi.swagger.client.model.ProcessGroupDTO;
import com.github.hermannpencole.nifi.swagger.client.model.ProcessGroupEntity;
import com.github.hermannpencole.nifi.swagger.client.model.ProcessorDTO;
import com.github.hermannpencole.nifi.swagger.client.model.ProcessorEntity;
import com.github.hermannpencole.nifi.swagger.client.model.ProcessorsEntity;
import com.github.hermannpencole.nifi.swagger.client.model.RevisionDTO;
import com.github.hermannpencole.nifi.swagger.client.model.TemplateDTO;
import com.github.hermannpencole.nifi.swagger.client.model.TemplateEntity;
import com.github.hermannpencole.nifi.swagger.client.model.TemplatesEntity;
import io.dtonic.dhubingestmodule.nifi.client.NiFiClient;
import io.dtonic.dhubingestmodule.nifi.vo.NiFiComponentVO;
import io.dtonic.dhubingestmodule.nifi.vo.PropertyVO;
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

    public String searchProcessorIdbyName(String rootProcessorGroupId, String processorName) {
        ProcessorsEntity result = searchProcessorsInProcessorGroup(rootProcessorGroupId);
        for (ProcessorEntity processor : result.getProcessors()) {
            ProcessorDTO processorInfo = processor.getComponent();
            if (processorInfo.getName().equals(processorName)) {
                return processorInfo.getId();
            } else {
                log.error("Not Found Processor Name : [{}]", processorName);
                return null;
            }
        }
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

    public void createOutputInPipeline(String processGroupId, String pipelineName) {
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
        } catch (ApiException e) {
            log.error("Fail to Create Output in Pipeline : Pipeline Name = [{}]", pipelineName);
            e.printStackTrace();
        }
    }

    public ProcessorsEntity searchProcessorsInProcessorGroup(String processorGroupId) {
        ProcessorsEntity result = niFiClient
            .getProcessGroupsApiSwagger()
            .getProcessors(processorGroupId, false);
        return result;
    }

    //TODO searchConnectionInProcessorGroup
    public ConnectionsEntity searchConnectionsInProcessorGroup(String processorGroupId) {
        ConnectionsEntity result = niFiClient
            .getProcessGroupsApiSwagger()
            .getConnections(processorGroupId);
        return result;
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
        ProcessorsEntity processors = searchProcessorsInProcessorGroup(processorGroupId);
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

    public void createConnection(
        String rootProcessGroupId,
        String sourceProcessGroupId,
        String destinationProcessGroupId,
        boolean isDestProcessor
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
        if (isDestProcessor) {
            String transmitterId = searchProcessorIdbyName(rootProcessGroupId, "Transmitter");
            ConnectableDTO dest = component.getDestination();
            dest.setId(transmitterId);
            dest.setGroupId(rootProcessGroupId);
            ConnectableDTO.TypeEnum destType = nifiObjectMapper.convertValue(
                "PROCESSOR",
                ConnectableDTO.TypeEnum.class
            );
            dest.setType(destType);
        } else {
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
        }
        body.setComponent(component);
        niFiClient.getProcessGroupsApiSwagger().createConnection(rootProcessGroupId, body);
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

    public ProcessorEntity createTrasmitter() {
        ProcessorEntity body = new ProcessorEntity(); // ProcessorEntity | The processor configuration details.
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
            return result;
        } catch (ApiException e) {
            log.error("Fail to Create Transmitter Processor in Processor Group");
            e.printStackTrace();
            return new ProcessorEntity();
        }
    }
}
