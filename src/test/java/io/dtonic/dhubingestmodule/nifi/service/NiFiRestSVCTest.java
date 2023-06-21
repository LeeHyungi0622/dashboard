package io.dtonic.dhubingestmodule.nifi.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.hermannpencole.nifi.swagger.client.model.ProcessGroupEntity;
import com.github.hermannpencole.nifi.swagger.client.model.ProcessGroupFlowEntity;

import io.dtonic.dhubingestmodule.common.code.NifiStatusCode;
import io.dtonic.dhubingestmodule.common.service.DataCoreRestSVC;
import io.dtonic.dhubingestmodule.nifi.client.NiFiClient;
import io.dtonic.dhubingestmodule.nifi.client.NiFiClientEntity;
import io.dtonic.dhubingestmodule.nifi.controller.NiFiController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.apache.nifi.web.api.entity.ControllerServiceEntity;
import org.apache.nifi.web.api.entity.ControllerServicesEntity;
import org.apache.nifi.web.api.entity.ProcessGroupStatusEntity;
import org.apache.nifi.web.api.entity.TemplateEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@SpringBootTest
@ActiveProfiles("dtonic")
public class NiFiRestSVCTest {

    @Autowired
    private NiFiController niFiController;
    
    @Autowired
    private NiFiRestSVC niFiRestSVC;

    @Autowired
    private ObjectMapper nifiObjectMapper;

    @Autowired
    private NiFiSwaggerSVC niFiSwaggerSVC;

    @Autowired
    private NiFiClient niFiClient;

    @Autowired
    private DataCoreRestSVC dataCoreRestSVC;

    @Autowired
    private NiFiClientEntity niFiClientEntity;


    @Test
    public void createDummyTemplateTest() throws JsonProcessingException {
        String rootProcessorGroupId = "root";
        String templateId = "fb12dcd3-fc45-4ab7-b757-eb18b2552e25";
        String result = niFiRestSVC.createDummyTemplate(rootProcessorGroupId, templateId);
        log.debug("{}", nifiObjectMapper.writeValueAsString(result));
        assertNotNull(result);
    }

    @Test
    public void searchControllersInProcessorGroupTest() throws JsonProcessingException {
        String processorGroupId = "9058aaf2-0186-1000-56f5-1b2a46437d07";
        ControllerServicesEntity result = niFiRestSVC.searchControllersInProcessorGroup(
            processorGroupId
        );
        Set<ControllerServiceEntity> controllers = new HashSet<ControllerServiceEntity>();

        controllers = result.getControllerServices();

        for (ControllerServiceEntity controller : controllers) {
            log.info("## controller ID : " + controller.getId());
            log.info("## controller Status : " + controller.getStatus().getRunStatus());
        }
        // log.info(
        //     "TEST Search Controllers In Processor Group : [{}]",
        //     nifiObjectMapper.writeValueAsString(result.getControllerServices())
        // );
        assertNotNull(result);
    }

    @Test
    public void getStatusProcessGroupTest() throws JsonProcessingException {
        String processorGroupId = "95aa36a9-cda1-1e8b-06cd-b7dc03983c33";
        Map<String, Integer> result = niFiRestSVC.getStatusProcessGroup(processorGroupId);
        log.info(
            "TEST Get Status Of Processor Group : [{}]",
            nifiObjectMapper.writeValueAsString(result)
        );
        assertNotNull(result);
    }

    @Test
    public void startProcessorGroupTest() throws JsonProcessingException {
        String processorGroupId = "9058aaf2-0186-1000-56f5-1b2a46437d071";
        boolean result = niFiRestSVC.startProcessorGroup(processorGroupId);
        assertTrue(result);
    }

    @Test
    public void stopProcessorGroupTest() throws JsonProcessingException {
        String processorGroupId = "9058aaf2-0186-1000-56f5-1b2a46437d071";
        boolean result = niFiRestSVC.stopProcessorGroup(processorGroupId);
        assertTrue(result);
    }

    @Test
    public void disableControllersTest() throws JsonProcessingException {
        String processorGroupId = "9058aaf2-0186-1000-56f5-1b2a46437d071";
        boolean result = niFiRestSVC.disableControllers(processorGroupId);
        assertTrue(result);
    }

    @Test
    public void enableControllersTest() throws JsonProcessingException {
        String processorGroupId = "9058aaf2-0186-1000-56f5-1b2a46437d071";
        //niFiRestSVC.enableControllers(processorGroupId);
    }

    @Test
    public void createFunnelTest() throws JsonProcessingException {
        String processorGroupId = "9058aaf2-0186-1000-56f5-1b2a46437d071";
        String result = niFiRestSVC.createFunnel(processorGroupId);
        log.debug("{}", nifiObjectMapper.writeValueAsString(result));
        assertNotNull(result);
    }

    @Test
    public void deleteConnectionToFunnel() throws JsonProcessingException {
        String processorGroupId = "9058aaf2-0186-1000-56f5-1b2a46437d071";
        Boolean result = niFiRestSVC.stopProcessorGroup(processorGroupId);

        log.info("result : " + result);
        
        /*Map<String, Integer> nifiStatus = 
            if(nifiStatus == null){
                log.info("null 입니다요~");
            }else{
            log.info("nifiStatus" +  nifiStatus);
            log.info("nifiStatus.get(NifiStatusCode.NIFI_STATUS_RUNNING.getCode())" + nifiStatus.get(NifiStatusCode.NIFI_STATUS_RUNNING.getCode()));
            }*/
            //log.info("nifiStatus : ", nifiStatus.get(NifiStatusCode.NIFI_STATUS_STOPPED.getCode()).toString());
        //     List<String> paths = new ArrayList<String>();
        //     paths.add("flow");
        //     paths.add("process-groups");
        //     paths.add(processorGroupId);
    
        //     Map<String, String> headers = new HashMap<String, String>();
        //     headers.put("Content-Type", "application/json");
    
        //     Map<String, Object> params = new HashMap<>();
        //     params.put("uiOnly", true);
    
        //     ResponseEntity<String> result = dataCoreRestSVC.get(
        //         niFiClientEntity.getProperties().getNifiUrl() + niFiClientEntity.getBASE_URL(),
        //         paths,
        //         headers,
        //         null,
        //         params,
        //         niFiClientEntity.getAccessToken(),
        //         String.class
        //     );
    
        //     ProcessGroupFlowEntity resultEntity = nifiObjectMapper.readValue(
        //         result.getBody(),
        //         ProcessGroupFlowEntity.class
        //     );
        //     log.info("resultEntity : ", resultEntity);


        //     Map<String, Integer> result2 = new HashMap<>();
        // Integer runCnt = 0;
        // Integer stopCnt = 0;
        // Integer invaildCnt = 0;
        // try {
        //     for (ProcessGroupEntity processorStatus : resultEntity
        //         .getProcessGroupFlow()
        //         .getFlow()
        //         .getProcessGroups()) {
        //         runCnt += processorStatus.getRunningCount();
        //         stopCnt += processorStatus.getStoppedCount();
        //         invaildCnt += processorStatus.getInvalidCount();
        //     }
        //     result2.put("Running", runCnt);
        //     result2.put("Stopped", stopCnt);
        //     result2.put("Invaild", invaildCnt);

        //     log.info("result2: " + result2);
            
        // } catch (Exception e) {
            
        // }


    }
}
