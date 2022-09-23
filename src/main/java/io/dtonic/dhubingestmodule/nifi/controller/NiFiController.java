package io.dtonic.dhubingestmodule.nifi.controller;

import io.dtonic.dhubingestmodule.nifi.client.NiFiClientEntity;
import io.dtonic.dhubingestmodule.nifi.service.NiFiRestSVC;
import io.dtonic.dhubingestmodule.nifi.service.NiFiSwaggerSVC;
import io.dtonic.dhubingestmodule.nifi.vo.AdaptorVO;
import io.dtonic.dhubingestmodule.nifi.vo.NiFiComponentVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineVO;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
public class NiFiController {

    @Autowired
    private NiFiClientEntity niFiClientEntity;

    @Autowired
    private NiFiSwaggerSVC niFiSwaggerSVC;

    @Autowired
    private NiFiRestSVC niFiRestSVC;

    /**
     * Create Pipeline
     *
     * @param pipelineVO
     * @return templateID
     */
    public void createPipeline(PipelineVO pipelineVO) {
        // Check Token Expired
        niFiClientEntity.manageToken();
        // Create Pipeline Processor Group
        String processGroupId = niFiSwaggerSVC.createProcessGroup(pipelineVO.getName(), "root");

        // Create Output Port
        niFiSwaggerSVC.createOutputInPipeline(processGroupId, pipelineVO.getName());

        /* Create Adatpors */
        // Create Collector Adaptor
        String collectorId = createAdaptor(pipelineVO.getCollector(), processGroupId);
        // Create Filter Adaptor
        String filterId = createAdaptor(pipelineVO.getFilter(), processGroupId);
        // Create Convertor Adaptor
        String convertorId = createAdaptor(pipelineVO.getConverter(), processGroupId);

        /* Create Connections */
        // Create Collector - Filter Connection
        niFiSwaggerSVC.createConnection(processGroupId, collectorId, filterId, false);
        // Create Filter - Convertor Connection
        niFiSwaggerSVC.createConnection(processGroupId, filterId, convertorId, false);
        // Create Connection Pipeline to Transmitter
        niFiSwaggerSVC.createConnection("root", convertorId, null, true);
    }

    public void deletePipeline(String processGroupId, String templateId) {
        // Check Token Expired
        niFiClientEntity.manageToken();
    }

    public void updatePipeline() {
        // Check Token Expired
        niFiClientEntity.manageToken();
    }

    public void runPipeline() {
        // Check Token Expired
        niFiClientEntity.manageToken();
    }

    public void stopPipeline() {
        // Check Token Expired
        niFiClientEntity.manageToken();
    }

    /**
     * Create Adaptor
     *
     * @param adaptorVO
     * @param rootProcessorGroupId Parent Process ID
     * @return ProcessGroup ID created Adaptor
     */
    public String createAdaptor(AdaptorVO adaptorVO, String rootProcessorGroupId) {
        // Search TemplateId by name
        String templateId = niFiSwaggerSVC.searchTempletebyName(adaptorVO.getType());

        // Create Dummy Template
        String templateProcessGroupID = niFiRestSVC.createDummyTemplate(
            rootProcessorGroupId,
            templateId
        );

        // Update Adaptor
        updateAdaptor(templateProcessGroupID, adaptorVO.getNiFiComponent());

        return templateProcessGroupID;
    }

    public void updateAdaptor(String processorGroupId, List<NiFiComponentVO> NiFiComponents) {
        if (NiFiComponents.size() < 1) {
            log.error("Empty NiFi Components In Request Pipeline");
        }
        for (NiFiComponentVO component : NiFiComponents) {
            if (component.getType().equals("processor")) {
                // Update Processors Properties in Adaptor Processor Group
                niFiSwaggerSVC.updateProcessorsInAdaptor(processorGroupId, component);
            } else if (component.getType().equals("controller")) {
                // Update Controllers Properties in Adaptor
                niFiRestSVC.updateControllersInAdaptor(processorGroupId, component);
            } else {
                log.error("Not Found NiFi Component Type");
            }
        }
    }
}
