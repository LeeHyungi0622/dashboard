package io.dtonic.dhubingestmodule.nifi.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.dtonic.dhubingestmodule.nifi.vo.AdaptorVO;
import io.dtonic.dhubingestmodule.nifi.vo.NiFiComponentVO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class NiFiAdaptorSVC {
    @Autowired
    private NiFiSwaggerSVC niFiSwaggerSVC;

    @Autowired
    private NiFiRestSVC niFiRestSVC;
    /**
     * @param adaptorVO
     * @param rootProcessorGroupId Parent Process ID
     * @return ProcessGroup ID created Adaptor
     * @throws JsonProcessingException
     */
    public String createAdaptor(AdaptorVO adaptorVO, String rootProcessorGroupId) throws Exception{
        String templateId = niFiSwaggerSVC.searchTempletebyName(adaptorVO.getName());
        // Create Dummy Template
        String adaptorId = niFiRestSVC.createDummyTemplate(adaptorVO.getName(), rootProcessorGroupId, templateId);
        // Update Adaptor
        updateAdaptor(adaptorId, adaptorVO.getNifiComponents());
        return adaptorId;
    }
    
    private void updateAdaptor(String processorGroupId, List<NiFiComponentVO> NiFiComponents)
        throws JsonProcessingException {
        if (NiFiComponents.size() < 1) {
            log.error("Empty NiFi Components In Request Pipeline");
        }
        for (NiFiComponentVO component : NiFiComponents) {
            if (
                component.getType().equals("processor") || component.getType().equals("Processor")
            ) {
                // Update Processors Properties in Adaptor Processor Group
                niFiSwaggerSVC.updateProcessorsInAdaptor(processorGroupId, component);
            } else if (
                component.getType().equals("controller") || component.getType().equals("Controller")
            ) {
                // Update Controllers Properties in Adaptor
                niFiRestSVC.updateControllersInAdaptor(processorGroupId, component);
            } else {
                log.error("{} Not Found NiFi Component Type");
            }
        }
    }
}
