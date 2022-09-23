package io.dtonic.dhubingestmodule.nifi.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.nifi.web.api.entity.ControllerServicesEntity;
import org.apache.nifi.web.api.entity.ProcessGroupStatusEntity;
import org.apache.nifi.web.api.entity.TemplateEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@SpringBootTest
@ActiveProfiles("dtonic")
public class NiFiRestSVCTest {

    @Autowired
    private NiFiRestSVC niFiRestSVC;

    @Autowired
    private ObjectMapper nifiObjectMapper;

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
        String processorGroupId = "15382931-0274-3554-807c-190d8a1470f6";
        ControllerServicesEntity result = niFiRestSVC.searchControllersInProcessorGroup(
            processorGroupId
        );
        log.info(
            "TEST Search Controllers In Processor Group : [{}]",
            nifiObjectMapper.writeValueAsString(result.getControllerServices())
        );
        assertNotNull(result);
    }

    @Test
    public void uploadTemplateTest() throws JsonProcessingException {
        TemplateEntity result = niFiRestSVC.uploadTemplate();
        log.info(
            "TEST Search Controllers In Processor Group : [{}]",
            nifiObjectMapper.writeValueAsString(result)
        );
        assertNotNull(result);
    }

    @Test
    public void getStatusProcessGroupTest() throws JsonProcessingException {
        String processorGroupId = "95aa36a9-cda1-1e8b-06cd-b7dc03983c33";
        ProcessGroupStatusEntity result = niFiRestSVC.getStatusProcessGroup(processorGroupId);
        log.info(
            "TEST Get Status Of Processor Group : [{}]",
            nifiObjectMapper.writeValueAsString(result)
        );
        assertNotNull(result);
    }

    @Test
    public void getNumberOfProcessorStatusTest()
        throws JsonMappingException, JsonProcessingException {
        String processorGroupId = "95aa36a9-cda1-1e8b-06cd-b7dc03983c33";
        ProcessGroupStatusEntity processGroupStatus = niFiRestSVC.getStatusProcessGroup(
            processorGroupId
        );
        Map<String, Integer> result = niFiRestSVC.getNumberOfProcessorStatus(processGroupStatus);
        log.info(
            "TEST Get Status Number Of Processor Group : [{}]",
            nifiObjectMapper.writeValueAsString(result)
        );
        assertNotNull(result);
    }
}
