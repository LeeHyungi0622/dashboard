package io.dtonic.dhubingestmodule.adaptor.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.dtonic.dhubingestmodule.adaptor.vo.AdaptorVO;
import io.dtonic.dhubingestmodule.common.code.AdaptorName;
import io.dtonic.dhubingestmodule.common.code.MonitoringCode;
import io.dtonic.dhubingestmodule.history.aop.task.TaskHistory;
import io.dtonic.dhubingestmodule.nifi.service.NiFiControllerSVC;
import io.dtonic.dhubingestmodule.nifi.service.NiFiProcessorSVC;
import io.dtonic.dhubingestmodule.nifi.service.NiFiTemplateSVC;
import io.dtonic.dhubingestmodule.nifi.vo.NiFiComponentVO;
import io.swagger.client.model.ControllerServiceEntity;
import io.swagger.client.model.ControllerServicesEntity;
import io.swagger.client.model.ProcessorEntity;
import io.swagger.client.model.ProcessorsEntity;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AdaptorSVC {
    @Autowired
    private NiFiTemplateSVC niFiTemplateSVC;

    @Autowired
    private NiFiProcessorSVC niFiProcessorSVC;

    @Autowired
    private NiFiControllerSVC niFiControllerSVC;

    @TaskHistory(taskName = MonitoringCode.CREATE_ADAPTOR_COLLECTOR)
    public String createCollector(Integer commandId, AdaptorVO adaptorVO, String rootProcessorGroupId){
        return createAdaptor(adaptorVO, rootProcessorGroupId);
    }
    @TaskHistory(taskName = MonitoringCode.CREATE_ADAPTOR_FILTER)
    public String createFilter(Integer commandId, AdaptorVO adaptorVO, String rootProcessorGroupId){
        return createAdaptor(adaptorVO, rootProcessorGroupId);
    }
    @TaskHistory(taskName = MonitoringCode.CREATE_ADAPTOR_CONVERTOR)
    public String createConvertor(Integer commandId, AdaptorVO adaptorVO, String rootProcessorGroupId){
        return createAdaptor(adaptorVO, rootProcessorGroupId);
    }
    /**
     * @param adaptorVO
     * @param rootProcessorGroupId Parent Process ID
     * @return ProcessGroup ID created Adaptor
     * @throws JsonProcessingException
     */
    private String createAdaptor(AdaptorVO adaptorVO, String rootProcessorGroupId){
        // Create Dummy Template
        String adaptorId = niFiTemplateSVC.createDummyTemplate(AdaptorName.parseCode(adaptorVO.getName()), rootProcessorGroupId);
        // Update Adaptor
        updateAdaptor(adaptorId, adaptorVO.getNifiComponents());
        return adaptorId;
    }
    
    private void updateAdaptor(String processorGroupId, List<NiFiComponentVO> NiFiComponents)
        {
        if (NiFiComponents.size() < 1) {
            log.error("Empty NiFi Components In Request Pipeline");
        }
        for (NiFiComponentVO component : NiFiComponents) {
            if (
                component.getType().equals("processor") || component.getType().equals("Processor")
            ) {
                // Update Processors Properties in Adaptor Processor Group
                updateProcessorsInAdaptor(processorGroupId, component);
            } else if (
                component.getType().equals("controller") || component.getType().equals("Controller")
            ) {
                // Update Controllers Properties in Adaptor
                updateControllersInAdaptor(processorGroupId, component);
            } else {
                log.error("{} Not Found NiFi Component Type");
            }
        }
    }
    
    private void updateProcessorsInAdaptor(
        String processorGroupId,
        NiFiComponentVO niFiComponentVO
    ) {
        ProcessorsEntity processors = niFiProcessorSVC.searchProcessorsInProcessGroup(processorGroupId);
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
                            niFiProcessorSVC.updateProcessorProperties(
                                processor,
                                niFiComponentVO.getRequiredProps()
                            );
                    } else {
                        log.error("Empty Required Props Elements In {}", niFiComponentVO.getName());
                    }
                    if (niFiComponentVO.getOptionalProps().size() != 0) {
                        processor =
                            niFiProcessorSVC.updateProcessorProperties(
                                processor,
                                niFiComponentVO.getOptionalProps()
                            );
                    }
                    niFiProcessorSVC.updateProcessor(processor.getId(), processor);
                }
            }
        }
    }
    private void updateControllersInAdaptor(String processorGroupId, NiFiComponentVO properies)
        {
        ControllerServicesEntity controllers = niFiControllerSVC.searchControllersInProcessorGroup(processorGroupId);
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
                            niFiControllerSVC.updateControllerProperties(controller, properies.getRequiredProps());
                    } else {
                        log.error("Empty Required Props Elements In {}", properies.getName());
                    }
                    if (properies.getOptionalProps().size() != 0) {
                        controller =
                            niFiControllerSVC.updateControllerProperties(controller, properies.getOptionalProps());
                    }
                    try {
                        ControllerServiceEntity resultEntity = niFiControllerSVC.updateController(processorGroupId, controller);
                        log.info("Update Controllers In Adaptor : Controller = {}", resultEntity);
                    } catch (Exception e) {
                        log.error("Fail to Update Controllers in Adaptor.", e);
                    }
                }
            }
        }
    }

}
