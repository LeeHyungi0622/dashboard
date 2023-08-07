package io.dtonic.dhubingestmodule.adaptor.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.dtonic.dhubingestmodule.adaptor.vo.AdaptorVO;
import io.dtonic.dhubingestmodule.nifi.vo.NiFiComponentVO;
import io.swagger.client.model.ProcessorEntity;
import io.swagger.client.model.ProcessorsEntity;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AdaptorSVC {
    @Autowired
    
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
                    if (niFiComponentVO.getRequiredProps().size() != 0) {
                        processor =
                            updateProcessorProperties(
                                processor,
                                niFiComponentVO.getRequiredProps()
                            );
                    } else {
                        log.error("Empty Required Props Elements In {}", niFiComponentVO.getName());
                    }
                    if (niFiComponentVO.getOptionalProps().size() != 0) {
                        processor =
                            updateProcessorProperties(
                                processor,
                                niFiComponentVO.getOptionalProps()
                            );
                    }
                    niFiClient
                        .getProcessorsApiSwagger()
                        .updateProcessor(processor.getId(), processor);
                }
            }
        }
    }


}
