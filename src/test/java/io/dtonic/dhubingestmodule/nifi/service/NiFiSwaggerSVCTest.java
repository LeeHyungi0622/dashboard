package io.dtonic.dhubingestmodule.nifi.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.hermannpencole.nifi.swagger.client.model.PortDTO;
import com.github.hermannpencole.nifi.swagger.client.model.ProcessorEntity;
import com.github.hermannpencole.nifi.swagger.client.model.ProcessorsEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@SpringBootTest
@ActiveProfiles("dtonic")
public class NiFiSwaggerSVCTest {

    @Autowired
    private NiFiSwaggerSVC niFiSwaggerSVC;

    @Autowired
    private ObjectMapper nifiObjectMapper;

    @Test
    public void searchProcessorsInProcessorGroupTest() throws JsonProcessingException {
        String processorGroupId = "15382931-0274-3554-807c-190d8a1470f6";
        ProcessorsEntity result = niFiSwaggerSVC.searchProcessorsInProcessorGroup(processorGroupId);
        log.debug("{}", nifiObjectMapper.writeValueAsString(result));
        assertNotNull(result);
    }

    @Test
    public void searchTempletebyNameTest() throws JsonProcessingException {
        String templateName = "Filter";
        String result = niFiSwaggerSVC.searchTempletebyName(templateName);
        log.info("{}", result);
        assertNotNull(result);
    }

    @Test
    public void searchInputInProcessorGroupTest() throws JsonProcessingException {
        String processorGroupId = "15382931-0274-3554-807c-190d8a1470f6";
        PortDTO result = niFiSwaggerSVC.searchInputInProcessorGroup(processorGroupId);
        log.debug("{}", nifiObjectMapper.writeValueAsString(result));
        assertNotNull(result);
    }

    @Test
    public void createTrasmitterTest() throws JsonProcessingException {
        ProcessorEntity result = niFiSwaggerSVC.createTrasmitter();
        log.debug("{}", nifiObjectMapper.writeValueAsString(result));
        assertNotNull(result);
    }
}
