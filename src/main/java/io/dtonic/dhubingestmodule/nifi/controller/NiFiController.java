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
    public String createPipeline(PipelineVO pipelineVO) {
        try {
            // Check Token Expired
            niFiClientEntity.manageToken(niFiClientEntity.getAccessToken());
            // Create Pipeline Processor Group
            String processGroupId = niFiSwaggerSVC.createProcessGroup(pipelineVO.getName(), "root");

            // Create Output Port
            String outputId = niFiSwaggerSVC.createOutputInPipeline(
                processGroupId,
                pipelineVO.getName()
            );

            /* Create Adatpors */
            // Create Collector Adaptor
            String collectorId = createAdaptor(pipelineVO.getCollector(), processGroupId);
            // Create Filter Adaptor
            String filterId = createAdaptor(pipelineVO.getFilter(), processGroupId);
            // Create Convertor Adaptor
            String convertorId = createAdaptor(pipelineVO.getConverter(), processGroupId);

            // 수정 필요.
            /* Create Connections */
            // Create Collector - Filter Connection
            niFiSwaggerSVC.createConnection(processGroupId, collectorId, filterId);
            // Create Filter - Convertor Connection
            niFiSwaggerSVC.createConnection(processGroupId, filterId, convertorId, false);
            // Create Convertor - Output Connection
            niFiSwaggerSVC.createConnection(processGroupId, convertorId, outputId, false);

            // Create Connection Output to Transmitter
            niFiSwaggerSVC.createConnection("root", convertorId, null, true);
            // Enable all Controllers
            return processGroupId;
        } catch (Exception e) {
            log.error("Fail to Create Pipeline in NiFi: PipelineVO = {}", pipelineVO);
            return null;
        }
    }

    /**
     * Delete Pipeline
     *
     * @param processGroupId
     * @return success/fail boolean
     */
    public boolean deletePipeline(String processGroupId) {
        try {
            // Check Token Expired
            niFiClientEntity.manageToken(niFiClientEntity.getAccessToken());

            /* Clear Queues in Connections */
            niFiRestSVC.clearQueuesInProcessGroup(processGroupId);
            /* Disable Controller */
            niFiRestSVC.disableControllers(processGroupId);
            /* Stop Pipeline */
            stopPipeline(processGroupId);

            /* Delete Connection */
            niFiSwaggerSVC.deleteConnectionToFunnel(processGroupId);
            /* Delete Processor Group */
            log.info("Success Delete Pipeline in NiFI : processGroupId = [{}]", processGroupId);
            return true;
        } catch (Exception e) {
            log.error("Fail to Delete Pipeline in NiFi : processGroupId = [{}]", processGroupId);
            return false;
        }
    }

    public void updatePipeline(PipelineVO pipelineVO) {
        // Check Token Expired
        niFiClientEntity.manageToken(niFiClientEntity.getAccessToken());
    }

    public boolean runPipeline(String processorGroupId) {
        // Check Token Expired
        niFiClientEntity.manageToken(niFiClientEntity.getAccessToken());
        if (niFiRestSVC.startProcessorGroup(processorGroupId)) {
            log.info("Success Run Pipeline : Processor Group ID = {}", processorGroupId);
            return true;
        } else {
            log.error("Fail to Run Pipeline : Processor Group ID = {}", processorGroupId);
            return false;
        }
    }

    /**
     * Stop Pipeline
     *
     * @param processGroupId
     * @return success/fail boolean
     */
    public boolean stopPipeline(String processorGroupId) {
        // Check Token Expired
        niFiClientEntity.manageToken(niFiClientEntity.getAccessToken());

        niFiClientEntity.manageToken(niFiClientEntity.getAccessToken());
        if (niFiRestSVC.stopProcessorGroup(processorGroupId)) {
            log.info("Success Stop Pipeline : Processor Group ID = {}", processorGroupId);
            return true;
        } else {
            log.error("Fail to Stop Pipeline : Processor Group ID = {}", processorGroupId);
            return false;
        }
    }

    // 다시짜자
    /**
     * @param adaptorVO
     * @param rootProcessorGroupId Parent Process ID
     * @return ProcessGroup ID created Adaptor
     */
    public String createAdaptor(AdaptorVO adaptorVO, String rootProcessorGroupId) {
        // String templateId = niFiSwaggerSVC.searchTempletebyName(adaptorVO.getType());

        // Create Dummy Template

        // Update Adaptor
        // updateAdaptor(templateProcessGroupID, adaptorVO.getNiFiComponent());

        return rootProcessorGroupId;
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
