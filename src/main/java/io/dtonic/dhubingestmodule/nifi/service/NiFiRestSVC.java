package io.dtonic.dhubingestmodule.nifi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.dtonic.dhubingestmodule.common.code.MonitoringCode;
import io.dtonic.dhubingestmodule.common.code.NifiStatusCode;
import io.dtonic.dhubingestmodule.common.code.TaskStatusCode;
import io.dtonic.dhubingestmodule.common.service.DataCoreRestSVC;
import io.dtonic.dhubingestmodule.nifi.client.NiFiClientEntity;
import io.dtonic.dhubingestmodule.nifi.vo.DropRequestEntity;
import io.dtonic.dhubingestmodule.nifi.vo.NiFiComponentVO;
import io.dtonic.dhubingestmodule.nifi.vo.PropertyVO;
import io.dtonic.dhubingestmodule.pipeline.mapper.PipelineMapper;
import io.dtonic.dhubingestmodule.pipeline.vo.TaskVO;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.nifi.web.api.dto.ConnectableDTO;
import org.apache.nifi.web.api.dto.ConnectionDTO;
import org.apache.nifi.web.api.dto.FunnelDTO;
import org.apache.nifi.web.api.dto.PortDTO;
import org.apache.nifi.web.api.dto.PositionDTO;
import org.apache.nifi.web.api.dto.RevisionDTO;
import org.apache.nifi.web.api.entity.ActivateControllerServicesEntity;
import org.apache.nifi.web.api.entity.ConnectionEntity;
import org.apache.nifi.web.api.entity.ControllerServiceEntity;
import org.apache.nifi.web.api.entity.ControllerServicesEntity;
//import org.apache.nifi.web.api.entity.DropRequestEntity;
import org.apache.nifi.web.api.entity.FlowEntity;
import org.apache.nifi.web.api.entity.FunnelEntity;
import org.apache.nifi.web.api.entity.InstantiateTemplateRequestEntity;
import org.apache.nifi.web.api.entity.PortEntity;
import org.apache.nifi.web.api.entity.ProcessGroupEntity;
import org.apache.nifi.web.api.entity.ProcessGroupFlowEntity;
import org.apache.nifi.web.api.entity.ProcessGroupStatusEntity;
import org.apache.nifi.web.api.entity.ProcessorEntity;
import org.apache.nifi.web.api.entity.ProcessorRunStatusEntity;
import org.apache.nifi.web.api.entity.ProcessorStatusSnapshotEntity;
import org.apache.nifi.web.api.entity.ScheduleComponentsEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;

/**
 * NiFi Services using NiFi REST Client
 * @FileName NiFiRestSVC.java
 * @Project D.hub Ingest Manager
 * @Brief
 * @Version 1.0
 * @Date 2022. 9. 27.
 * @Author Justin
 */
@Slf4j
@Service
public class NiFiRestSVC {

    @Autowired
    private DataCoreRestSVC dataCoreRestSVC;

    @Autowired
    private NiFiClientEntity niFiClientEntity;

    @Autowired
    private ObjectMapper nifiObjectMapper;

    @Autowired
    private NiFiSwaggerSVC niFiSwaggerSVC;

    @Autowired
    private PipelineMapper pipelineMapper;

    /**
     * Create Dummy Template
     *
     * @param rootProcessorGroupId Parent Process ID
     * @param templateId create template ID
     * @param accessToken set up nifi access token
     * @return ProcessGroup ID created Adaptor
     */
    public String createDummyTemplate(String templateName, String rootProcessorGroupId, String templateId) {
        try {
            List<String> paths = new ArrayList<String>();
            Map<String, String> headers = new HashMap<String, String>();

            headers.put("Content-Type", "application/json");
            InstantiateTemplateRequestEntity body = new InstantiateTemplateRequestEntity();
            body.setTemplateId(templateId);
            body.setDisconnectedNodeAcknowledged(false);
            if (templateName.equals("collector")) body.setOriginX(0.0D);
            else if (templateName.equals("filter")) body.setOriginX(740.0D);
            else if (templateName.equals("converter")) body.setOriginX(1480.0D);
            else body.setOriginX(0.0D);
            body.setOriginY(0.0D);
            paths.add("process-groups");
            paths.add(rootProcessorGroupId);
            paths.add("template-instance");

            ResponseEntity<String> result = dataCoreRestSVC.post(
                niFiClientEntity.getProperties().getNifiUrl() + niFiClientEntity.getBASE_URL(),
                paths,
                headers,
                body,
                null,
                niFiClientEntity.getAccessToken(),
                String.class
            );

            FlowEntity resultFlowEntity = nifiObjectMapper.readValue(
                result.getBody(),
                FlowEntity.class
            );
            Set<ProcessGroupEntity> resultProcessGroup = resultFlowEntity
                .getFlow()
                .getProcessGroups();
            if (resultProcessGroup.size() == 0) {
                log.error("Empty Processor Group in Template : Template ID = {}", templateId);
                return null;
            } else if (resultProcessGroup.size() > 1) {
                log.error(
                    "Too Many (2 or more) Processor Group in Template : Template ID = {}",
                    templateId
                );
                return null;
            } else {
                for (ProcessGroupEntity e : resultProcessGroup) {
                    String adaptorId = e.getId();
                    return adaptorId;
                }
            }
            return null;
        } catch (Exception e) {
            log.error("Fail to Create Dummy Template.", e);
            return null;
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

    public void updateControllersInAdaptor(String processorGroupId, NiFiComponentVO properies)
        throws JsonProcessingException {
        ControllerServicesEntity controllers = searchControllersInProcessorGroup(processorGroupId);
        if (controllers.getControllerServices().size() == 0) {
            log.error(
                "Empty Controller in Processor Group : Processor Group ID = {}",
                processorGroupId
            );
        } else {
            for (ControllerServiceEntity controller : controllers.getControllerServices()) {
                if (controller.getComponent().getName().equals(properies.getName())) {
                    log.info("Controller name : {}", controller.getComponent().getName());
                    if (properies.getRequiredProps().size() != 0) {
                        controller =
                            updateControllerProperties(controller, properies.getRequiredProps());
                    } else {
                        log.error("Empty Required Props Elements In {}", properies.getName());
                    }
                    if (properies.getOptionalProps().size() != 0) {
                        controller =
                            updateControllerProperties(controller, properies.getOptionalProps());
                    }
                    try {
                        List<String> paths = new ArrayList<String>();
                        paths.add("controller-services");
                        paths.add(controller.getId());

                        Map<String, String> headers = new HashMap<String, String>();
                        headers.put("Content-Type", "application/json");

                        ResponseEntity<String> result = dataCoreRestSVC.put(
                            niFiClientEntity.getProperties().getNifiUrl() +
                            niFiClientEntity.getBASE_URL(),
                            paths,
                            headers,
                            controller,
                            null,
                            niFiClientEntity.getAccessToken(),
                            String.class
                        );

                        ControllerServiceEntity resultEntity = nifiObjectMapper.readValue(
                            result.getBody(),
                            ControllerServiceEntity.class
                        );
                        log.info("Update Controllers In Adaptor : Controller = {}", resultEntity);
                    } catch (Exception e) {
                        log.error("Fail to Update Controllers in Adaptor.", e);
                    }
                }
            }
        }
    }

    public ControllerServiceEntity updateControllerProperties(
        ControllerServiceEntity controller,
        List<PropertyVO> properies
    ) {
        Map<String, String> setUpProperties = controller.getComponent().getProperties();
        for (PropertyVO property : properies) {
            setUpProperties.replace(property.getName(), property.getInputValue());
        }
        log.info("Properties : {}", setUpProperties);
        controller.getComponent().setProperties(setUpProperties);

        return controller;
    }

    public ControllerServicesEntity searchControllersInProcessorGroup(String processorGroupId) {
        try {
            List<String> paths = new ArrayList<String>();
            paths.add("flow");
            paths.add("process-groups");
            paths.add(processorGroupId);
            paths.add("controller-services");

            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Content-Type", "application/json");

            Map<String, Object> params = new HashMap<>();
            params.put("includeAncestorGroups", false);
            params.put("includeDescendantGroups", true);

            ResponseEntity<String> result = dataCoreRestSVC.get(
                niFiClientEntity.getProperties().getNifiUrl() + niFiClientEntity.getBASE_URL(),
                paths,
                headers,
                null,
                params,
                niFiClientEntity.getAccessToken(),
                String.class
            );

            ControllerServicesEntity resultEntity = nifiObjectMapper.readValue(
                result.getBody(),
                ControllerServicesEntity.class
            );
            return resultEntity;
        } catch (Exception e) {
            log.error("Fail to Create Dummy Template.", e);
            return null;
        }
    }

    public ProcessorEntity getProcessorInfo(String processorId)
        throws JsonMappingException, JsonProcessingException {
        List<String> paths = new ArrayList<String>();
        paths.add("processors");
        paths.add(processorId);

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

        ProcessorEntity resultEntity = nifiObjectMapper.readValue(
            result.getBody(),
            ProcessorEntity.class
        );
        return resultEntity;
    }

    public Map<String, Integer> getStatusProcessGroup(String processorGroupId)
        throws JsonMappingException, JsonProcessingException {
        List<String> paths = new ArrayList<String>();
        paths.add("flow");
        paths.add("process-groups");
        paths.add(processorGroupId);

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");

        Map<String, Object> params = new HashMap<>();
        params.put("uiOnly", true);

        ResponseEntity<String> result = dataCoreRestSVC.get(
            niFiClientEntity.getProperties().getNifiUrl() + niFiClientEntity.getBASE_URL(),
            paths,
            headers,
            null,
            params,
            niFiClientEntity.getAccessToken(),
            String.class
        );

        ProcessGroupFlowEntity resultEntity = nifiObjectMapper.readValue(
            result.getBody(),
            ProcessGroupFlowEntity.class
        );
        return getNumberOfProcessorStatus(resultEntity);
    }

    protected Map<String, Integer> getNumberOfProcessorStatus(
        ProcessGroupFlowEntity processGroupStatus
    ) {
        Map<String, Integer> result = new HashMap<>();
        Integer runCnt = 0;
        Integer stopCnt = 0;
        Integer invaildCnt = 0;
        try {
            for (ProcessGroupEntity processorStatus : processGroupStatus
                .getProcessGroupFlow()
                .getFlow()
                .getProcessGroups()) {
                runCnt += processorStatus.getRunningCount();
                stopCnt += processorStatus.getStoppedCount();
                invaildCnt += processorStatus.getInvalidCount();
            }
            result.put("Running", runCnt);
            result.put("Stopped", stopCnt);
            result.put("Invaild", invaildCnt);
            return result;
        } catch (Exception e) {
            // TODO: handle exception
            return result;
        }
    }

    public boolean startProcessorGroup(String processorGroupId) {
        try {
            ScheduleComponentsEntity body = new ScheduleComponentsEntity();
            body.setState("RUNNING");
            body.setId(processorGroupId);
            body.setDisconnectedNodeAcknowledged(false);

            List<String> paths = new ArrayList<String>();
            paths.add("flow");
            paths.add("process-groups");
            paths.add(processorGroupId);

            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Content-Type", "application/json");

            ResponseEntity<String> result = dataCoreRestSVC.put(
                niFiClientEntity.getProperties().getNifiUrl() + niFiClientEntity.getBASE_URL(),
                paths,
                headers,
                body,
                null,
                niFiClientEntity.getAccessToken(),
                String.class
            );
            ScheduleComponentsEntity resultEntity = nifiObjectMapper.readValue(
                result.getBody(),
                ScheduleComponentsEntity.class
            );
            if (resultEntity.getClass().equals(ScheduleComponentsEntity.class)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error("Can not Start Processor Groups", e);
            return false;
        }
    }

    /**
     * Start Transmitter
     * When finshed update properties of transmitter, must update transmitter status to RUNNING
     *
     * @param transmitterId Transmitter id
     * @return Success/Fail
     */
    public boolean startTransmitter(String transmitterId) {
        try {
            ProcessorRunStatusEntity body = new ProcessorRunStatusEntity();
            body.setState("RUNNING");

            ProcessorEntity transmitter = getProcessorInfo(transmitterId);
            RevisionDTO revision = transmitter.getRevision();
            body.setRevision(revision);
            body.setDisconnectedNodeAcknowledged(false);

            List<String> paths = new ArrayList<String>();
            paths.add("processors");
            paths.add(transmitterId);
            paths.add("run-status");

            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Content-Type", "application/json");

            dataCoreRestSVC.put(
                niFiClientEntity.getProperties().getNifiUrl() + niFiClientEntity.getBASE_URL(),
                paths,
                headers,
                body,
                null,
                niFiClientEntity.getAccessToken(),
                String.class
            );
            log.info("Success Update Transmitter Status to RUNNING");
            return true;
        } catch (Exception e) {
            log.error("Fail to Update Transmitter Status to RUNNING", e);
            return false;
        }
    }

    /**
     * Stop Transmitter
     * Transmitter status must be update to STOPPED when updating properties of transmitter
     *
     * @param transmitterId Transmitter id
     * @return Success/Fail
     */
    public boolean stopTransmitter(String transmitterId) {
        try {
            ProcessorRunStatusEntity body = new ProcessorRunStatusEntity();
            body.setState("STOPPED");

            ProcessorEntity transmitter = getProcessorInfo(transmitterId);
            RevisionDTO revision = transmitter.getRevision();
            body.setRevision(revision);
            body.setDisconnectedNodeAcknowledged(false);

            List<String> paths = new ArrayList<String>();
            paths.add("processors");
            paths.add(transmitterId);
            paths.add("run-status");

            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Content-Type", "application/json");

            dataCoreRestSVC.put(
                niFiClientEntity.getProperties().getNifiUrl() + niFiClientEntity.getBASE_URL(),
                paths,
                headers,
                body,
                null,
                niFiClientEntity.getAccessToken(),
                String.class
            );
            log.info("Success Update Transmitter Status to STOPPED");
            return true;
        } catch (Exception e) {
            log.error("Fail to Update Transmitter Status to STOPPED", e);
            return false;
        }
    }

    public boolean stopProcessorGroup(String processorGroupId) {
        try {
            ScheduleComponentsEntity body = new ScheduleComponentsEntity();
            body.setState("STOPPED");
            body.setId(processorGroupId);
            body.setDisconnectedNodeAcknowledged(false);

            List<String> paths = new ArrayList<String>();
            paths.add("flow");
            paths.add("process-groups");
            paths.add(processorGroupId);

            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Content-Type", "application/json");

            ResponseEntity<String> result = dataCoreRestSVC.put(
                niFiClientEntity.getProperties().getNifiUrl() + niFiClientEntity.getBASE_URL(),
                paths,
                headers,
                body,
                null,
                niFiClientEntity.getAccessToken(),
                String.class
            );
            ScheduleComponentsEntity resultEntity = nifiObjectMapper.readValue(
                result.getBody(),
                ScheduleComponentsEntity.class
            );
            if (resultEntity.getClass().equals(ScheduleComponentsEntity.class)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error("Can not Stop Processor Groups", e);
            return false;
        }
    }

    /**
     * Disable Controllers in process group to delete pipeline
     *
     * @param processGroupId
     * @return success/fail boolean
     */
    public boolean disableControllers(String processorGroupId) {
        try {
            ActivateControllerServicesEntity body = new ActivateControllerServicesEntity();
            body.setState("DISABLED");
            body.setId(processorGroupId);
            body.setDisconnectedNodeAcknowledged(false);

            List<String> paths = new ArrayList<String>();
            paths.add("flow");
            paths.add("process-groups");
            paths.add(processorGroupId);
            paths.add("controller-services");

            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Content-Type", "application/json");

            dataCoreRestSVC.put(
                niFiClientEntity.getProperties().getNifiUrl() + niFiClientEntity.getBASE_URL(),
                paths,
                headers,
                body,
                null,
                niFiClientEntity.getAccessToken(),
                String.class
            );
            log.info(
                "Success Disable Controllers In Process Group : Process Group ID = [{}]",
                processorGroupId
            );
            return true;
        } catch (Exception e) {
            log.error(
                "Fail to Disable Controllers In Process Group : Process Group ID = [{}]",
                processorGroupId,
                e
            );
            return false;
        }
    }

    /**
     * Enable Controllers in process group
     *
     * @param processGroupId
     */
    public Boolean enableControllers(String processorGroupId) throws Exception{
        ActivateControllerServicesEntity body = new ActivateControllerServicesEntity();
        body.setState("ENABLED");
        body.setId(processorGroupId);
        body.setDisconnectedNodeAcknowledged(false);

        List<String> paths = new ArrayList<String>();
        paths.add("flow");
        paths.add("process-groups");
        paths.add(processorGroupId);
        paths.add("controller-services");

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");

        dataCoreRestSVC.put(
            niFiClientEntity.getProperties().getNifiUrl() + niFiClientEntity.getBASE_URL(),
            paths,
            headers,
            body,
            null,
            niFiClientEntity.getAccessToken(),
            String.class
        );
        log.info("Success Enable Controllers in {}", processorGroupId);
        return true;
    }

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
     * Upload NiFi Templates to register collector/filter/converter
     *
     */
    public void uploadTemplate() {
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver()
            .getResources("classpath:template/*.xml");
            for (Resource template : resources) {
                List<String> paths = new ArrayList<String>();
                paths.add("process-groups");
                paths.add("root");
                paths.add("templates");
                paths.add("upload");

                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "multipart/form-data");

                LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                body.add("template", template);

                ResponseEntity<String> result = dataCoreRestSVC.postTemplate(
                    niFiClientEntity.getProperties().getNifiUrl() + niFiClientEntity.getBASE_URL(),
                    paths,
                    headers,
                    body,
                    null,
                    niFiClientEntity.getAccessToken(),
                    String.class
                );
                if (result == null) {
                    log.info("{} is already Exist", template);
                } else {
                    log.info("Upload Template Name : [{}]", template);
                }
            }
        } catch (IOException e) {
            log.error("Not Found Template Files in src/main/resources/template", e);
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
    

    public boolean monitoring(MonitoringCode action, String processGroupId) throws JsonMappingException, JsonProcessingException, InterruptedException{
        for (int i =0 ; i < 3; i++){
            long startTime = System.currentTimeMillis();
            boolean result = false;
            switch(action){
                case STOP_PIPELINE: {
                    if(stopProcessorGroup(processGroupId)){
                        while(!result && (System.currentTimeMillis() - startTime < 10000) ){
                            Map<String, Integer> nifiStatus = getStatusProcessGroup(processGroupId);
                            if(nifiStatus.get(NifiStatusCode.NIFI_STATUS_RUNNING.getCode()) == 0){
                                result = true;
                            }
                            
                        }
                            return result;
                    }
                    break;
                }
                case CLEAR_QUEUE: {
                    ResponseEntity<String> response = clearQueuesInProcessGroup(processGroupId);
                    DropRequestEntity resultClearQueue = nifiObjectMapper.readValue(response.getBody(), DropRequestEntity.class);
                    String requestId = resultClearQueue.getDropRequest().getId();
                    if(requestId != null){
                        while(!result && (System.currentTimeMillis() - startTime < 10000)){
                            result = checkclearQueuesInProcessGroup(processGroupId , requestId);
                        }
                        return true;
                    }
                    break;
                }
                case DISABLE_CONTROLLER: {
                    if(disableControllers(processGroupId)){
                        List<Boolean> resList = new ArrayList<Boolean>();
                        while(!result && (System.currentTimeMillis() - startTime < 500)){
                            ControllerServicesEntity controllers = searchControllersInProcessorGroup(processGroupId);
                            for (ControllerServiceEntity controller : controllers.getControllerServices()) {
                                if (controller.getStatus().getRunStatus().equals("DISABLED")){
                                    resList.add(true);
                                }
                            }
                            if(resList.size() == controllers.getControllerServices().size()){
                                result = true;
                            }
                        }
                        return result;
                    }
                    break;
                }
                case DELETE_CONNECTION: {
                    if(niFiSwaggerSVC.deleteConnectionToFunnel(processGroupId)){
                        while(!result && (System.currentTimeMillis() - startTime < 10000)){
                            if(niFiSwaggerSVC.clearQueuesInConnectionToFunnel(processGroupId) == null){
                                result = true;
                            }
                        }
                        return result;
                    }
                    break;
                }
                case DELETE_PIPELINE: {
                    if(niFiSwaggerSVC.deleteProcessGroup(processGroupId)){
                        while(!result && (System.currentTimeMillis() - startTime < 10000)){
                            if(stopProcessorGroup(processGroupId) == false){
                                result = true;
                            }
                        }
                        return result;
                    }
                    break;
                }
                default: {
                    log.error("Invalid Action to Delete Pipeline Process in NiFi : processGroupId = [{}]" + processGroupId);
                    return result;
                }
            }
        }
        return false;
    }
    
    public Integer createTask(TaskVO taskVO) {
        try{
            pipelineMapper.createTask(taskVO);
            return taskVO.getId();
        }catch(Exception e){
            log.error("create task error {}" , e);
            return -1;
        }
    }

    public Boolean updateTask(Integer id, String taskStatusCode) {
        if(pipelineMapper.updateTask(id, taskStatusCode) == 1){
            return true;
        }else{
            log.error("Update Task error");
            return false;
        }
    }
}
