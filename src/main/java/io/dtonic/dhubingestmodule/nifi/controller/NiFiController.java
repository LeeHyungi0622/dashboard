package io.dtonic.dhubingestmodule.nifi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.dtonic.dhubingestmodule.adaptor.vo.AdaptorVO;
import io.dtonic.dhubingestmodule.common.code.MonitoringCode;
import io.dtonic.dhubingestmodule.common.code.NifiStatusCode;
import io.dtonic.dhubingestmodule.common.code.TaskStatusCode;
import io.dtonic.dhubingestmodule.common.component.Properties;
import io.dtonic.dhubingestmodule.common.thread.MultiThread;
import io.dtonic.dhubingestmodule.dataset.service.DataSetSVC;
import io.dtonic.dhubingestmodule.dataset.vo.Attribute;
import io.dtonic.dhubingestmodule.dataset.vo.DataModelVO;
import io.dtonic.dhubingestmodule.history.aop.task.TaskHistory;
import io.dtonic.dhubingestmodule.history.vo.TaskVO;
import io.dtonic.dhubingestmodule.nifi.NiFiApplicationRunner;
import io.dtonic.dhubingestmodule.nifi.client.NiFiClientProperty;
import io.dtonic.dhubingestmodule.nifi.service.NiFiConnectionSVC;
import io.dtonic.dhubingestmodule.nifi.service.NiFiConvertPropsSVC;
import io.dtonic.dhubingestmodule.nifi.service.NiFiProcessGroupSVC;
import io.dtonic.dhubingestmodule.nifi.vo.NiFiComponentVO;
import io.dtonic.dhubingestmodule.nifi.vo.PropertyVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineVO;
import io.swagger.client.model.ControllerServiceEntity;
import io.swagger.client.model.ControllerServicesEntity;
import io.swagger.client.model.DropRequestEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Served Pipeline Service
 * @FileName NiFiController.java
 * @Project D.hub Ingest Manager
 * @Brief
 * @Version 1.0
 * @Date 2022. 9. 27.
 * @Author Justin
 */
@Slf4j
@Controller
public class NiFiController {
    @Autowired
    private NiFiProcessGroupSVC niFiProcessGroupSVC;

    @Autowired
    private NiFiConnectionSVC niFiConnectionSVC;

    @Autowired
    private NiFiConvertPropsSVC niFiConvertSVC;

    @Autowired
    private NiFiClientProperty niFiClientProperty;

    @Autowired
    private Properties properties;

    @Autowired
    DataSetSVC dataSetSVC;


    /**
     * Create Pipeline In NiFi
     *
     * @param pipelineVO
     * @return templateID
     */
    public String createPipeline(PipelineVO pipelineVO, Integer commandId) {

        try{
            /* Create Pipeline Processor Group */
            String processGroupId = niFiProcessGroupSVC.createProcessGroup(
                commandId,
                pipelineVO.getName(),
                niFiClientProperty.getIngestProcessGroupId()
            ).getId();
            /* Create Output Port */
            String outputId = niFiConnectionSVC.createOutputInPipeline(
                commandId,
                processGroupId,
                pipelineVO.getName()
            );

            /* Create Adatpors Collector */
            taskVO.setTaskName(MonitoringCode.CREATE_ADAPTOR_COLLECTOR.getCode());
            taskId = niFiRestSVC.createTask(taskVO);
            String collectorId = niFiAdaptorSVC.createAdaptor(pipelineVO.getCollector(), processGroupId);
            if(collectorId != null){
                niFiRestSVC.updateTask(taskId, TaskStatusCode.TASK_STATUS_FINISH.getCode());
            }

            /* Create Adatpors Filter */
            taskVO.setTaskName(MonitoringCode.CREATE_ADAPTOR_FILTER.getCode());
            taskId = niFiRestSVC.createTask(taskVO);
            String filterId = niFiAdaptorSVC.createAdaptor(pipelineVO.getFilter(), processGroupId);
            if(filterId != null){
                niFiRestSVC.updateTask(taskId, TaskStatusCode.TASK_STATUS_FINISH.getCode());
            }
            /* Create Adatpors Convertor */
            taskVO.setTaskName(MonitoringCode.CREATE_ADAPTOR_CONVERTOR.getCode());
            taskId = niFiRestSVC.createTask(taskVO);
            AdaptorVO formattedNiFiProps = convertPipelineVOToNiFiForm(pipelineVO);
            String convertorId = niFiAdaptorSVC.createAdaptor(formattedNiFiProps, processGroupId);
            if(formattedNiFiProps != null){
                niFiRestSVC.updateTask(taskId, TaskStatusCode.TASK_STATUS_FINISH.getCode());
            }

            /* Create Connections Collector - Filter Connection */
            taskVO.setTaskName(MonitoringCode.CREATE_CONNECTION_BETWEEN_PROCESSGROUP.getCode());
            taskId = niFiRestSVC.createTask(taskVO);
            if(niFiSwaggerSVC.createConnectionBetweenProcessGroup(
                processGroupId,
                collectorId,
                filterId
            ) &&
            /* Create Connections Filter - Convertor Connection */
            niFiSwaggerSVC.createConnectionBetweenProcessGroup(
                processGroupId,
                filterId,
                convertorId
            ) &&
            /* Create Connections Convertor - Output Port Connection */
            niFiSwaggerSVC.createConnectionFromProcessGroupToOutput(
                processGroupId,
                convertorId,
                processGroupId
            ) &&
            /* Create Connections Output Port - Funnel Connection */
            niFiSwaggerSVC.createConnectionFromOutputToFunnel(
                niFiApplicationRunner.getIngestProcessGroupId(),
                processGroupId,
                niFiApplicationRunner.getIngestProcessGroupId()
            )){
                niFiRestSVC.updateTask(taskId, TaskStatusCode.TASK_STATUS_FINISH.getCode());
            }

            /* Enable all Controllers */
            taskVO.setTaskName(MonitoringCode.ENABLE_CONTROLLERS.getCode());
            taskId = niFiRestSVC.createTask(taskVO);
            if(niFiRestSVC.enableControllers(processGroupId)){
                niFiRestSVC.updateTask(taskId, TaskStatusCode.TASK_STATUS_FINISH.getCode());
            }
            /* success create Pipeline */
            log.info("Success Create Pipeline in NiFi : PipelineVO = {}", pipelineVO);
            return processGroupId;
        } catch(Exception e){
            log.error("Fail to " + taskVO.getTaskName() + " Pipeline in NiFi : PipelineVO = " + pipelineVO, e);
            niFiRestSVC.updateTask(taskId, TaskStatusCode.TASK_STATUS_FAILED.getCode());
            return null;
        }
    }

    private AdaptorVO convertPipelineVOToNiFiForm(PipelineVO pipeline){

        try{
            List<NiFiComponentVO> convertorComponents = new ArrayList<>();

            NiFiComponentVO extractDataSetProps = niFiConvertSVC.extractDataSetPropsProcessor(pipeline);
            if (extractDataSetProps != null) {
                convertorComponents.add(extractDataSetProps);
            }
            NiFiComponentVO convertNgsiLdFormat = niFiConvertSVC.convertNgsiLdFormatProcessor(pipeline);
            if (convertNgsiLdFormat != null) {
                convertorComponents.add(convertNgsiLdFormat);
            }
            NiFiComponentVO setNgsiLdString = niFiConvertSVC.setNgsiLdStringProcessor(pipeline);
            if (setNgsiLdString != null) {
                convertorComponents.add(setNgsiLdString);
            }
            NiFiComponentVO convertDateType = niFiConvertSVC.convertDateTypeProcessor(pipeline);
            if (convertDateType != null) {
                convertorComponents.add(convertDateType);
            }
            NiFiComponentVO convertArrayType = niFiConvertSVC.convertArrayTypeProcessor(pipeline);
            if (convertArrayType != null) {
                convertorComponents.add(convertArrayType);
            }
            NiFiComponentVO convertGeoJsonType = niFiConvertSVC.convertGeoJsonTypeProcessor(pipeline);
            if (convertGeoJsonType != null) {
                convertorComponents.add(convertGeoJsonType);
            }
            AdaptorVO convertor = new AdaptorVO();
            
            convertor.setNifiComponents(convertorComponents);
            convertor.setName(pipeline.getConverter().getName());
            return convertor;
        }
        catch(Exception e){
            log.error(" convertPipelineVOToNiFiForm error " , e);
            return null;
        }
    }




    /**
     * Delete Pipeline
     *
     * @param processGroupId
     * @return success/fail boolean
     */
    public synchronized boolean deletePipeline(String processGroupId, Integer commandId) {
        ArrayList<MonitoringCode> actions = new ArrayList<MonitoringCode>();
        
        /* Stop Pipeline */
        actions.add(MonitoringCode.STOP_PIPELINE);
        /* Clear Queues in Connections */
        actions.add(MonitoringCode.CLEAR_QUEUE);
        /* Disable Controller */
        actions.add(MonitoringCode.DISABLE_CONTROLLER);
        /* Delete Connection */
        actions.add(MonitoringCode.DELETE_CONNECTION);
        /* Delete Processor Group */
        actions.add(MonitoringCode.DELETE_PIPELINE);
        
        try {
            TaskVO taskVO = new TaskVO();
            taskVO.setCommandId(commandId);
            taskVO.setStatus(TaskStatusCode.TASK_STATUS_WORKING.getCode());

            for (MonitoringCode action : actions){

                taskVO.setTaskName(action.getCode());
                Integer taskId = niFiRestSVC.createTask(taskVO);

                /* task 수행 */
                if(niFiRestSVC.monitoring(action, processGroupId)){
                    niFiRestSVC.updateTask(taskId, TaskStatusCode.TASK_STATUS_FINISH.getCode());
                }else{
                    log.error("Fail to " + action + " Pipeline in NiFi : processGroupId = [{}]", processGroupId);
                    niFiRestSVC.updateTask(taskId, TaskStatusCode.TASK_STATUS_FAILED.getCode());
                    return false;
                }
            }
            log.info("Success Delete Pipeline in NiFI : processGroupId = [{}]", processGroupId);
            return true;
        } catch (Exception e) {
            log.error("Fail to Delete Pipeline in NiFi : processGroupId = [{" + processGroupId + "}] error : " +  e);
            return false;
        }
    }

    public String updatePipeline(PipelineVO pipelineVO, Integer commandId) {
        try {
            /* Create Pipeline */
            String processGroupId = createPipeline(pipelineVO, commandId);
            if(processGroupId != null){
                
                /* Delete Pipleine using thread */
                Runnable deletePipeline = new MultiThread(pipelineVO, commandId);
                Thread thread = new Thread(deletePipeline);
                thread.start();

                return processGroupId;
            }else{
                log.error(
                    "Fail to Create Pipeline in NiFi : processGroupId = [{}]",
                    pipelineVO.getProcessorGroupId()
                );
                return null;
            }
        } catch (Exception e) {
            log.error("Fail to Update Pipeline in NiFi : processGroupId = [{}]", pipelineVO, e);
            return null;
        }
    }

    /**
     * Run Pipeline
     *
     * @param processGroupId
     * @return success/fail boolean
     * @throws JsonProcessingException
     * @throws JsonMappingException
     */
    public boolean runPipeline(String processorGroupId) throws JsonMappingException, JsonProcessingException {
        if (niFiRestSVC.startProcessorGroup(processorGroupId)) {
            Map<String, Integer> nifiStatus = niFiRestSVC.getStatusProcessGroup(processorGroupId);
            for(int i =0; i <3 ; i++){
                if (nifiStatus.get(NifiStatusCode.NIFI_STATUS_STOPPED.getCode()) == 0 && nifiStatus.get(NifiStatusCode.NIFI_STATUS_INVALID.getCode()) == 0){
                    log.info("Success Run Pipeline : Processor Group ID = {}", processorGroupId);
                    return true;
                }else{
                    log.error("Nifi status exist Run Proccessor or Invalid Proccessor " + i + " times");
                }
            }
        } else {
            log.error("Fail to Run Pipeline : Processor Group ID = {}", processorGroupId);
            return false;
        }
        return false;
    }

    /**
     * Stop Pipeline
     *
     * @param processGroupId
     * @return success/fail boolean
     * @throws JsonProcessingException
     * @throws JsonMappingException
     */
    public boolean stopPipeline(String processorGroupId) throws JsonMappingException, JsonProcessingException {
        if (niFiRestSVC.stopProcessorGroup(processorGroupId)) {
            Map<String, Integer> nifiStatus = niFiRestSVC.getStatusProcessGroup(processorGroupId);
            for(int i =0; i <3 ; i++){
                if (nifiStatus.get(NifiStatusCode.NIFI_STATUS_RUNNING.getCode()) == 0 && nifiStatus.get(NifiStatusCode.NIFI_STATUS_INVALID.getCode()) == 0){
                    log.info("Success Stop Pipeline : Processor Group ID = {}", processorGroupId);
                    return true;
                }else{
                    log.error("Nifi status exist Stopped Proccessor or Invalid Proccessor " + i + " times");
                }
            }
        } else {
            log.error("Fail to Stop Pipeline : Processor Group ID = {}", processorGroupId);
            return false;
        }
        return false;
    }

    public Map<String, Integer> getPipelineStatus(String processorGroup) {
        try {
            return niFiRestSVC.getStatusProcessGroup(processorGroup);
        } catch (Exception e) {
            log.error("Fail to Get Pipeline Status.", e);
            return null;
        }
    }

    /**
     * NiFi Redirect URL Return
     *
     * @return nifi url String
     */
    @GetMapping("/redirectNiFiUrl")
    public String redirectNiFiUrl(HttpServletRequest request, HttpServletResponse response) {
        return properties.getNifiUrl();
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
}
