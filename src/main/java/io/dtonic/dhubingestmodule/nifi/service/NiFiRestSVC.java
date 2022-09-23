package io.dtonic.dhubingestmodule.nifi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dtonic.dhubingestmodule.common.service.DataCoreRestSVC;
import io.dtonic.dhubingestmodule.nifi.client.NiFiClientEntity;
import io.dtonic.dhubingestmodule.nifi.vo.NiFiComponentVO;
import io.dtonic.dhubingestmodule.nifi.vo.PropertyVO;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.nifi.web.api.entity.ControllerServiceEntity;
import org.apache.nifi.web.api.entity.ControllerServicesEntity;
import org.apache.nifi.web.api.entity.FlowEntity;
import org.apache.nifi.web.api.entity.InstantiateTemplateRequestEntity;
import org.apache.nifi.web.api.entity.ProcessGroupEntity;
import org.apache.nifi.web.api.entity.ProcessGroupStatusEntity;
import org.apache.nifi.web.api.entity.ProcessorStatusSnapshotEntity;
import org.apache.nifi.web.api.entity.TemplateEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;

@Slf4j
@Service
public class NiFiRestSVC {

    @Autowired
    private DataCoreRestSVC dataCoreRestSVC;

    @Autowired
    private NiFiClientEntity niFiClientEntity;

    @Autowired
    private ObjectMapper nifiObjectMapper;

    /**
     * Create Dummy Template
     *
     * @param rootProcessorGroupId Parent Process ID
     * @param templateId create template ID
     * @param accessToken set up nifi access token
     * @return ProcessGroup ID created Adaptor
     */
    public <T> String createDummyTemplate(String rootProcessorGroupId, String templateId) {
        try {
            List<String> paths = new ArrayList<String>();
            Map<String, String> headers = new HashMap<String, String>();

            headers.put("Content-Type", "application/json");
            InstantiateTemplateRequestEntity body = new InstantiateTemplateRequestEntity();
            body.setTemplateId(templateId);
            body.setDisconnectedNodeAcknowledged(false);
            body.setOriginX(0.0D);
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

    public void updateControllersInAdaptor(String processorGroupId, NiFiComponentVO properies) {
        ControllerServicesEntity controllers = searchControllersInProcessorGroup(processorGroupId);
        if (controllers.getControllerServices().size() == 0) {
            log.error(
                "Empty Controller in Processor Group : Processor Group ID = {}",
                processorGroupId
            );
        } else {
            for (ControllerServiceEntity controller : controllers.getControllerServices()) {
                ControllerServiceEntity updateController = updateControllerProperties(
                    controller,
                    properies.getProperties()
                );
                try {
                    List<String> paths = new ArrayList<String>();
                    paths.add("controller-services");
                    paths.add(updateController.getId());

                    Map<String, String> headers = new HashMap<String, String>();
                    headers.put("Content-Type", "application/json");

                    ResponseEntity<String> result = dataCoreRestSVC.get(
                        niFiClientEntity.getProperties().getNifiUrl() +
                        niFiClientEntity.getBASE_URL(),
                        paths,
                        headers,
                        updateController,
                        null,
                        niFiClientEntity.getAccessToken(),
                        String.class
                    );

                    ControllerServicesEntity resultEntity = nifiObjectMapper.readValue(
                        result.getBody(),
                        ControllerServicesEntity.class
                    );
                    log.info("Update Controllers In Adaptor : Controller = {}", resultEntity);
                } catch (Exception e) {
                    log.error("Fail to Update Controllers in Adaptor.", e);
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

    //TODO exception check
    protected ProcessGroupStatusEntity getStatusProcessGroup(String processorGroupId)
        throws JsonMappingException, JsonProcessingException {
        List<String> paths = new ArrayList<String>();
        paths.add("flow");
        paths.add("process-groups");
        paths.add(processorGroupId);
        paths.add("status");

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");

        // Map<String, Object> params = new HashMap<>();
        // params.put("includeAncestorGroups", false);
        // params.put("includeDescendantGroups", true);

        ResponseEntity<String> result = dataCoreRestSVC.get(
            niFiClientEntity.getProperties().getNifiUrl() + niFiClientEntity.getBASE_URL(),
            paths,
            headers,
            null,
            null,
            niFiClientEntity.getAccessToken(),
            String.class
        );

        ProcessGroupStatusEntity resultEntity = nifiObjectMapper.readValue(
            result.getBody(),
            ProcessGroupStatusEntity.class
        );
        return resultEntity;
    }

    public Map<String, Integer> getNumberOfProcessorStatus(
        ProcessGroupStatusEntity processGroupStatus
    ) {
        Map<String, Integer> result = new HashMap<>();
        Integer runCnt = 0;
        Integer stopCnt = 0;
        Integer invaildCnt = 0;
        try {
            for (ProcessorStatusSnapshotEntity processorStatus : processGroupStatus
                .getProcessGroupStatus()
                .getAggregateSnapshot()
                .getProcessorStatusSnapshots()) {
                String status = processorStatus.getProcessorStatusSnapshot().getRunStatus();
                if (status.equals("Stopped")) {
                    stopCnt++;
                } else if (status.equals("Running")) {
                    runCnt++;
                } else {
                    invaildCnt++;
                }
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

    public TemplateEntity uploadTemplate() {
        try {
            ClassPathResource resource = new ClassPathResource("template/REST_Server.xml");
            File templateFile = resource.getFile();
            Resource template = new FileSystemResource(templateFile);

            List<String> paths = new ArrayList<String>();
            paths.add("process-groups");
            paths.add("root");
            paths.add("templates");
            paths.add("upload");

            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Content-Type", "multipart/form-data");

            LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("template", template);

            ResponseEntity<String> result = dataCoreRestSVC.post(
                niFiClientEntity.getProperties().getNifiUrl() + niFiClientEntity.getBASE_URL(),
                paths,
                headers,
                body,
                null,
                niFiClientEntity.getAccessToken(),
                String.class
            );
            TemplateEntity resultEntity = nifiObjectMapper.readValue(
                result.getBody(),
                TemplateEntity.class
            );
            return resultEntity;
        } catch (Exception e) {
            log.error("Not Found Template File", e);
            return new TemplateEntity();
        }
    }
}
