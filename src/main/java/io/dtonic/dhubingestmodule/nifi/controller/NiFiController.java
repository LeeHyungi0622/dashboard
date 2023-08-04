package io.dtonic.dhubingestmodule.nifi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.dtonic.dhubingestmodule.common.code.MonitoringCode;
import io.dtonic.dhubingestmodule.common.code.NifiStatusCode;
import io.dtonic.dhubingestmodule.common.code.TaskStatusCode;
import io.dtonic.dhubingestmodule.common.thread.MultiThread;
import io.dtonic.dhubingestmodule.dataset.service.DataSetSVC;
import io.dtonic.dhubingestmodule.dataset.vo.Attribute;
import io.dtonic.dhubingestmodule.dataset.vo.DataModelVO;
import io.dtonic.dhubingestmodule.nifi.NiFiApplicationRunner;
import io.dtonic.dhubingestmodule.nifi.client.NiFiClientEntity;
import io.dtonic.dhubingestmodule.nifi.service.NiFiAdaptorSVC;
import io.dtonic.dhubingestmodule.nifi.service.NiFiConvertPropsSVC;
import io.dtonic.dhubingestmodule.nifi.service.NiFiRestSVC;
import io.dtonic.dhubingestmodule.nifi.service.NiFiSwaggerSVC;
import io.dtonic.dhubingestmodule.nifi.vo.AdaptorVO;
import io.dtonic.dhubingestmodule.nifi.vo.ConverterVO;
import io.dtonic.dhubingestmodule.nifi.vo.NiFiComponentVO;
import io.dtonic.dhubingestmodule.nifi.vo.PropertyVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineVO;
import io.dtonic.dhubingestmodule.pipeline.vo.TaskVO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

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
    private NiFiClientEntity niFiClientEntity;

    @Autowired
    private NiFiSwaggerSVC niFiSwaggerSVC;

    @Autowired
    private NiFiRestSVC niFiRestSVC;

    @Autowired
    private NiFiAdaptorSVC niFiAdaptorSVC;

    @Autowired
    private NiFiConvertPropsSVC niFiConvertSVC;

    @Autowired
    NiFiApplicationRunner niFiApplicationRunner;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    DataSetSVC dataSetSVC;

    // @Autowired
    // PipelineSVC pipelineSVC;

    /**
     * Create Pipeline
     *
     * @param pipelineVO
     * @return templateID
     */
    public String createPipeline(PipelineVO pipelineVO, Integer commandId) {
        TaskVO taskVO = new TaskVO();
        taskVO.setCommandId(commandId);
        taskVO.setStatus(TaskStatusCode.TASK_STATUS_WORKING.getCode());
        taskVO.setTaskName(MonitoringCode.CREATE_PROCESSGROUP.getCode());
        Integer taskId = niFiRestSVC.createTask(taskVO);
        try{
            /* Create Pipeline Processor Group */
            String processGroupId = niFiSwaggerSVC.createProcessGroup(
                pipelineVO.getName(),
                niFiApplicationRunner.getIngestProcessGroupId()
            );
            if(processGroupId != null){
                niFiRestSVC.updateTask(taskId, TaskStatusCode.TASK_STATUS_FINISH.getCode());
            }

            /* Create Output Port */
            taskVO.setTaskName(MonitoringCode.CREATE_OUTPUTPORT_PIPELINE.getCode());
            taskId = niFiRestSVC.createTask(taskVO);

            String outputId = niFiRestSVC.createOutputInPipeline(
                processGroupId,
                pipelineVO.getName()
            );
            if(outputId != null){
                niFiRestSVC.updateTask(taskId, TaskStatusCode.TASK_STATUS_FINISH.getCode());
            }

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
                    log.error("Fail to" + action + "Pipeline in NiFi : processGroupId = [{}]", processGroupId);
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
            niFiClientEntity.manageToken();
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
            log.error("Fail to update Pipeline in NiFi : processGroupId = [{}]", pipelineVO, e);
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
}
