package io.dtonic.dhubingestmodule.nifi.controller;


import io.dtonic.dhubingestmodule.adaptor.service.AdaptorSVC;
import io.dtonic.dhubingestmodule.adaptor.vo.AdaptorVO;
import io.dtonic.dhubingestmodule.common.code.NifiStatusCode;
import io.dtonic.dhubingestmodule.dataset.service.DataSetSVC;

import io.dtonic.dhubingestmodule.nifi.client.NiFiClientProperty;
import io.dtonic.dhubingestmodule.nifi.service.NiFiConnectionSVC;
import io.dtonic.dhubingestmodule.nifi.service.NiFiControllerSVC;
import io.dtonic.dhubingestmodule.nifi.service.NiFiConvertPropsSVC;
import io.dtonic.dhubingestmodule.nifi.service.NiFiProcessGroupSVC;
import io.dtonic.dhubingestmodule.nifi.vo.NiFiComponentVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineVO;

import java.util.ArrayList;
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
 * @Version 1.2.0
 * @Date 2023. 8. 21.
 * @Author Justin
 */
@Slf4j
@Controller
public class NiFiController {

    @Autowired
    private AdaptorSVC adaptorSVC;

    @Autowired
    private NiFiProcessGroupSVC niFiProcessGroupSVC;

    @Autowired
    private NiFiConnectionSVC niFiConnectionSVC;

    @Autowired
    private NiFiConvertPropsSVC niFiConvertSVC;

    @Autowired
    private NiFiControllerSVC niFiControllerSVC;

    @Autowired
    private NiFiClientProperty niFiClientProperty;

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
            niFiConnectionSVC.createOutputInPipeline(
                commandId,
                processGroupId,
                pipelineVO.getName()
            );

            /* Create Adatpors Collector */
            String collectorId = adaptorSVC.createCollector(commandId, pipelineVO.getCollector(), processGroupId);

            /* Create Adatpors Filter */
            String filterId = adaptorSVC.createFilter(commandId, pipelineVO.getFilter(), processGroupId);

            /* Create Adatpors Convertor */
            AdaptorVO formattedNiFiProps = convertPipelineVOToNiFiForm(pipelineVO);
            String convertorId = adaptorSVC.createConvertor(commandId, formattedNiFiProps, processGroupId);

            /* Create Connections Collector - Filter Connection */
            niFiConnectionSVC.createConnectionBetweenProcessGroup(commandId, processGroupId, collectorId, filterId);
            niFiConnectionSVC.createConnectionBetweenProcessGroup(commandId, processGroupId, filterId, convertorId);
            /* Create Connections Convertor - Output Port Connection */
            niFiConnectionSVC.createConnectionFromProcessGroupToOutput(commandId, processGroupId, convertorId, processGroupId);
            /* Create Connections Output Port - Funnel Connection */
            niFiConnectionSVC.createConnectionFromOutputToFunnel(commandId, niFiClientProperty.getIngestProcessGroupId(), processGroupId, niFiClientProperty.getIngestProcessGroupId());

            /* Enable all Controllers */
            niFiControllerSVC.enableControllers(commandId, processGroupId);

            /* success create Pipeline */
            log.info("Success Create Pipeline in NiFi : Processor Group ID [{}]", processGroupId);
            return processGroupId;
        } catch(Exception e){
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
            NiFiComponentVO convertObjectType = niFiConvertSVC.convertObjectTypeProcessor(pipeline);
            if (convertObjectType != null) {
                convertorComponents.add(convertObjectType);
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


    public String updatePipeline(Integer commandId, PipelineVO pipelineVO) {
        try {
            /* Create Pipeline */
            String processGroupId = createPipeline(pipelineVO, commandId);
            if(processGroupId != null){
                
                /* Delete Pipleine using thread */
                deletePipeline(commandId, pipelineVO.getProcessorGroupId());

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
     * Run Pipeline Process Group 
     *
     * @param commandId  using command history aop
     * @param processorGroupId   processor group id
     * @return Boolean
     * 
     * @since 2023. 8. 21
     * @version 1.2.0
     * @auther Justin
     */
    public boolean runPipeline(Integer commandId, String processorGroupId){
        niFiProcessGroupSVC.startProcessorGroup(commandId, processorGroupId);
        try {
            Integer retryCnt = 0;
            while (retryCnt < 3) {
                Map<String, Integer> nifiStatus = niFiProcessGroupSVC.getStatusProcessGroup(processorGroupId);
                log.info("nifiStatus : {}", nifiStatus);
                if (nifiStatus.get(NifiStatusCode.NIFI_STATUS_STOPPED.getCode()) == 0 && nifiStatus.get(NifiStatusCode.NIFI_STATUS_INVALID.getCode()) == 0){
                    log.info("Success Run Pipeline : Processor Group ID = {}", processorGroupId);
                    return true;
                } else {
                    log.warn("Nifi status exist Run Proccessor or Invalid Proccessor");
                    log.warn("Retry Check Pipeline Status : Processor Group ID = {}", processorGroupId);
                }
                /* Sleep 0.3s */
                Thread.sleep(300);
                retryCnt++;
            }
            return false;
        } catch (InterruptedException e) {
            log.error("Interrupt Exception", e);
            return false;
        }
    }

    /**
     * Stop Pipeline Process Group 
     *
     * @param commandId  using command history aop
     * @param processorGroupId   processor group id
     * @return Boolean
     * 
     * @since 2023. 8. 21
     * @version 1.2.0
     * @auther Justin
     */
    public boolean stopPipeline(Integer commandId, String processorGroupId) {
        niFiProcessGroupSVC.stopProcessorGroup(commandId, processorGroupId);
        try {
            Integer retryCnt = 0;
            while (retryCnt < 3) {
                Map<String, Integer> nifiStatus = niFiProcessGroupSVC.getStatusProcessGroup(processorGroupId);
                if (nifiStatus.get(NifiStatusCode.NIFI_STATUS_RUNNING.getCode()) == 0 && nifiStatus.get(NifiStatusCode.NIFI_STATUS_INVALID.getCode()) == 0){
                    log.info("Success Stop Pipeline : Processor Group ID = {}", processorGroupId);
                    return true;
                } else {
                    log.warn("Nifi status exist Run Proccessor or Invalid Proccessor", nifiStatus);
                    log.warn("Retry Check {} Pipeline Status : Processor Group ID = {}", retryCnt, processorGroupId);
                }
                /* Sleep 0.3s */
                Thread.sleep(300);
                retryCnt++;
            } 
            return false;
        } catch (InterruptedException e) {
            log.error("Interrupt Exception", e);
            return false;
        }
    }
    
    /**
     * Get Status Pipeline Process Group
     *
     * @param processorGroupId   processor group id
     * @return Boolean
     * 
     * @since 2023. 8. 21
     * @version 1.2.0
     * @auther Justin
     */
    public Map<String, Integer> getPipelineStatus(String processorGroup) {
        return niFiProcessGroupSVC.getStatusProcessGroup(processorGroup);
    }
    
    /**
     * Delete Pipeline
     *
     * @param processorGroupId   processor group id
     * @return Boolean
     * 
     * @since 2023. 8. 21
     * @version 1.2.0
     * @auther Justin
     */
    public synchronized Boolean deletePipeline(Integer commandId, String processGroupId) {
        Boolean resStop = stopPipeline(commandId, processGroupId); 
        Boolean resClear = clearQueuePipeline(commandId, processGroupId); 
        Boolean resDisable = disablePipeline(commandId, processGroupId); 
        Boolean resdelete = deletePipelineEntity(commandId, processGroupId); 
        if (resStop && resClear && resDisable && resdelete) {
            log.info("Success Delete Pipeline : Processor Group ID = {}", processGroupId);
            return true;
        } else {
            log.error("Fail to Delete Pipeline : Processor Group ID = {}", processGroupId);
            return null;
        }
    }

    /**
     * Clear Queue Pipeline Process Group and Funnel
     *
     * @param commandId  using command history aop
     * @param processorGroupId   processor group id
     * @return Boolean
     * 
     * @since 2023. 8. 21
     * @version 1.2.0
     * @auther Justin
     */
    private boolean clearQueuePipeline(Integer commandId, String processorGroupId){
        Boolean resProcessGroup = niFiConnectionSVC.clearQueuesInProcessGroup(commandId, processorGroupId);
        Boolean resConnection = niFiConnectionSVC.clearQueuesInConnectionToFunnel(commandId, processorGroupId);
        if(resProcessGroup && resConnection){
            return true;
        } else {
            return false;
        }
    }
    /**
     * Disable Pipeline Controller
     *
     * @param commandId  using command history aop
     * @param processorGroupId   processor group id
     * @return Boolean
     * 
     * @since 2023. 8. 21
     * @version 1.2.0
     * @auther Justin
     */
    private boolean disablePipeline(Integer commandId,  String processorGroupId){
        return niFiControllerSVC.disableControllers(commandId, processorGroupId); // while 적용됨
    }

    /**
     * Delete Pipeline Process Group Entity and Connection
     *
     * @param commandId  using command history aop
     * @param processorGroupId   processor group id
     * @return Boolean
     * 
     * @since 2023. 8. 21
     * @version 1.2.0
     * @auther Justin
     */
    private boolean deletePipelineEntity(Integer commandId, String processorGroupId){
        Boolean resConnection= niFiConnectionSVC.deleteConnectionToFunnel(commandId, processorGroupId);
        Boolean resProcessGroup = niFiProcessGroupSVC.deleteProcessGroup(commandId, processorGroupId);
        if(resProcessGroup && resConnection){
            return true;
        } else {
            return false;
        }
    }



}
