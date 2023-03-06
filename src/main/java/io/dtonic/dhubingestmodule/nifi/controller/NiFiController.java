package io.dtonic.dhubingestmodule.nifi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.dtonic.dhubingestmodule.common.code.MonitoringCode;
import io.dtonic.dhubingestmodule.common.code.TaskStatusCode;
import io.dtonic.dhubingestmodule.common.code.DataCoreUiCode.ErrorCode;
import io.dtonic.dhubingestmodule.common.exception.BadRequestException;
import io.dtonic.dhubingestmodule.dataset.service.DataSetSVC;
import io.dtonic.dhubingestmodule.dataset.vo.Attribute;
import io.dtonic.dhubingestmodule.dataset.vo.DataModelVO;
import io.dtonic.dhubingestmodule.nifi.NiFiApplicationRunner;
import io.dtonic.dhubingestmodule.nifi.client.NiFiClientEntity;
import io.dtonic.dhubingestmodule.nifi.service.NiFiRestSVC;
import io.dtonic.dhubingestmodule.nifi.service.NiFiSwaggerSVC;
import io.dtonic.dhubingestmodule.nifi.vo.AdaptorVO;
import io.dtonic.dhubingestmodule.nifi.vo.ConverterVO;
import io.dtonic.dhubingestmodule.nifi.vo.NiFiComponentVO;
import io.dtonic.dhubingestmodule.nifi.vo.PropertyVO;
import io.dtonic.dhubingestmodule.pipeline.service.PipelineSVC;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineVO;
import io.dtonic.dhubingestmodule.pipeline.vo.TaskVO;

import java.time.LocalDate;
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
        taskVO.setInputParameter(null);
        taskVO.setStatus(TaskStatusCode.TASK_STATUS_WORKING.getCode());

        try {
            /* Check Token Expired */
            niFiClientEntity.manageToken(niFiClientEntity.getAccessToken());
            
            /* Create Pipeline Processor Group */
            taskVO.setTaskName(MonitoringCode.CREATE_PROCESSGROUP.getCode());
            Integer taskId = niFiRestSVC.createTask(taskVO);

            String processGroupId = niFiSwaggerSVC.createProcessGroup(
                pipelineVO.getName(),
                niFiApplicationRunner.getIngestProcessGroupId()
            );
            niFiRestSVC.updateTask(taskId, TaskStatusCode.TASK_STATUS_FINISH.getCode());

            /* Create Output Port */
            taskVO.setTaskName(MonitoringCode.CREATE_OUTPUT_INPIPELINE.getCode());
            taskId = niFiRestSVC.createTask(taskVO);

            String outputId = niFiRestSVC.createOutputInPipeline(
                processGroupId,
                pipelineVO.getName()
            );
            niFiRestSVC.updateTask(taskId, TaskStatusCode.TASK_STATUS_FINISH.getCode());

            //TODO
            /* Create Adatpors Collector */
            taskVO.setTaskName(MonitoringCode.CREATE_ADAPTOR.getCode());
            taskId = niFiRestSVC.createTask(taskVO);
            String collectorId = createAdaptor(pipelineVO.getCollector(), processGroupId);
            /* Create Adatpors Filter */
            String filterId = createAdaptor(pipelineVO.getFilter(), processGroupId);
            /* Create Adatpors Convertor */
            AdaptorVO formattedNiFiProps = convertPipelineVOToNiFiForm(pipelineVO);
            String convertorId = createAdaptor(formattedNiFiProps, processGroupId);
            niFiRestSVC.updateTask(taskId, TaskStatusCode.TASK_STATUS_FINISH.getCode());

            /* Create Connections Collector - Filter Connection */
            taskVO.setTaskName(MonitoringCode.CREATE_CONNECTION_BETWEEN_PROCESSGROUP.getCode());
            taskId = niFiRestSVC.createTask(taskVO);
            niFiSwaggerSVC.createConnectionBetweenProcessGroup(
                processGroupId,
                collectorId,
                filterId
            );
            /* Create Connections Filter - Convertor Connection */
            niFiSwaggerSVC.createConnectionBetweenProcessGroup(
                processGroupId,
                filterId,
                convertorId
            );
            /* Create Connections Convertor - Output Port Connection */
            niFiSwaggerSVC.createConnectionFromProcessGroupToOutput(
                processGroupId,
                convertorId,
                processGroupId
            );
            /* Create Connections Output Port - Funnel Connection */
            niFiSwaggerSVC.createConnectionFromOutputToFunnel(
                niFiApplicationRunner.getIngestProcessGroupId(),
                processGroupId,
                niFiApplicationRunner.getIngestProcessGroupId()
            );
            niFiRestSVC.updateTask(taskId, TaskStatusCode.TASK_STATUS_FINISH.getCode());

            /* Enable all Controllers */
            taskVO.setTaskName(MonitoringCode.ENABLE_CONTROLLERS.getCode());
            taskId = niFiRestSVC.createTask(taskVO);
            niFiRestSVC.enableControllers(processGroupId);
            niFiRestSVC.updateTask(taskId, TaskStatusCode.TASK_STATUS_FINISH.getCode());


            log.info("Success Create Pipeline in NiFi : PipelineVO = {}", pipelineVO);
            return processGroupId;
        } catch (Exception e) {
            log.error("Fail to Create Pipeline in NiFi : PipelineVO = {}", pipelineVO, e);
            //TODO : niFiRestSVC.updateTask(taskId, TaskStatusCode.TASK_STATUS_FAILED.getCode());
            return null;
        }
    }

    private AdaptorVO convertPipelineVOToNiFiForm(PipelineVO convertor)
        throws JsonProcessingException {
        // DataSet ID로 Data Model 정보 호출

        // ID 생성
        String id = idGenerater(convertor);
        // Convert NGSI LD
        NiFiComponentVO ngsiLdNiFi = setAttribute(convertor.getConverter());
        NiFiComponentVO ngsiLdFormater = convertNgsiLdData(
            convertor.getDataModel(),
            convertor.getDataSet(),
            id
        );
        NiFiComponentVO dateFormater = convertDateType(
            convertor.getConverter(),
            convertor.getDataModel()
        );
        NiFiComponentVO convertType = convertValueType(convertor.getDataModel());
        // NiFi Components Setting
        AdaptorVO result = new AdaptorVO();
        List<NiFiComponentVO> resNifi = new ArrayList<>();
        resNifi.add(ngsiLdNiFi);
        resNifi.add(ngsiLdFormater);
        resNifi.add(convertType);
        if (dateFormater != null) {
            resNifi.add(dateFormater);
        }
        result.setNifiComponents(resNifi);
        result.setName(convertor.getConverter().getName());

        return result;
    }

    private NiFiComponentVO setAttribute(AdaptorVO convertor) {
        NiFiComponentVO dataSetPropComp = new NiFiComponentVO();
        dataSetPropComp.setName("DataSetProps");
        dataSetPropComp.setType("processor");
        List<PropertyVO> dataSetProps = new ArrayList<>();
        for (NiFiComponentVO nifi : convertor.getNifiComponents()) {
            if (nifi.getName().equals("DataSetProps")) {
                for (PropertyVO prop : nifi.getRequiredProps()) {
                    if (prop.getDetail() != "Date Format" || prop.getDetail() != "unitCode") {
                        PropertyVO newProp = new PropertyVO();
                        newProp.setName(prop.getName());
                        String convertInput = prop.getInputValue().replace("\"", "");
                        newProp.setInputValue("$." + convertInput);
                        dataSetProps.add(newProp);
                    }
                }
            }
        }
        dataSetPropComp.setRequiredProps(dataSetProps);
        return dataSetPropComp;
    }

    private String idGenerater(PipelineVO convertor) {
        String id = "urn:datahub:" + convertor.getDataModel();
        for (NiFiComponentVO nifi : convertor.getConverter().getNifiComponents()) {
            if (nifi.getName().equals("IDGenerater")) {
                if (nifi.getRequiredProps().size() == 0) {
                    log.error("Not Found ID Generate Levels");
                    return null;
                } else {
                    for (int i = 0; i < nifi.getRequiredProps().size(); i++) {
                        if (nifi.getRequiredProps().get(i).getInputValue() != null) {
                            if (i < nifi.getRequiredProps().size() - 1) {
                                id =
                                    id +
                                    ":${" +
                                    nifi
                                        .getRequiredProps()
                                        .get(i)
                                        .getInputValue()
                                        .replace("\"", "") +
                                    "}";
                            } else {
                                id =
                                    id +
                                    "${" +
                                    nifi
                                        .getRequiredProps()
                                        .get(i)
                                        .getInputValue()
                                        .replace("\"", "") +
                                    "}\"";
                            }
                        }
                    }
                    log.info("Create ID Gen : [{}]", id);
                    return id;
                }
            }
        }
        return null;
    }

    private NiFiComponentVO convertNgsiLdData(String dataModelId, String dataSetId, String id)
        throws JsonProcessingException {
        NiFiComponentVO ngsiLdFormmater = new NiFiComponentVO();
        ngsiLdFormmater.setName("NGSI-LD Fommater");
        ngsiLdFormmater.setType("processor");
        DataModelVO dataModelInfo = dataSetSVC.getDataModelProperties(dataModelId);
        log.info("Data Model get Properties : [{}]", dataModelInfo);
        ConverterVO converterVO = new ConverterVO();
        List<Object> entities = new ArrayList<>();
        converterVO.setDatasetId(dataModelInfo.getDatasetId());
        Map<String, Object> entity = new HashMap<>();
        entity.put("id", id);
        entity.put("type", dataModelId);
        entity.put("@context", dataModelInfo.getContext());
        for (Attribute e : dataModelInfo.getAttributes()) {
            Map<String, Object> subAttribute = new HashMap<>();
            subAttribute.put("type", e.getAttributeType());
            if (e.getAttributeType().equals("Property")) {
                subAttribute.put("value", "${" + e.getName() + "}");
            } else if (e.getAttributeType().equals("Relationship")) {
                subAttribute.put("object", "${" + e.getName() + "}");
            } else if (e.getAttributeType().equals("GeoProperty")) {
                Map<String, String> geoProp = new HashMap<>();
                geoProp.put("type", "Point");
                geoProp.put("coordinates", "${" + e.getName() + "}");
                subAttribute.put("value", geoProp);
            }
            if (e.getHasObservedAt()) {
                subAttribute.put("observedAt", "${observedAt}");
            }
            //TODO
            if (e.getHasUnitCode()) {
                subAttribute.put("unitCode", "${observedAt}");
            }
            entity.put(e.getName(), subAttribute);
        }
        entities.add(entity);
        converterVO.setEntities(entities);
        converterVO.setDatasetId(dataSetId);
        String convertData = objectMapper.writeValueAsString(converterVO);
        log.info("Convert Data = {}", convertData);
        List<PropertyVO> props = new ArrayList<>();
        PropertyVO ngsiProp = new PropertyVO();
        ngsiProp.setName("Replacement Value");
        ngsiProp.setInputValue(convertData);
        props.add(ngsiProp);
        ngsiLdFormmater.setRequiredProps(props);

        return ngsiLdFormmater;
    }

    private NiFiComponentVO convertValueType(String dataModelId) throws JsonProcessingException {
        NiFiComponentVO convertValueTypeProc = new NiFiComponentVO();
        convertValueTypeProc.setName("ConvertValueType");
        convertValueTypeProc.setType("Processor");
        DataModelVO dataModelInfo = dataSetSVC.getDataModelProperties(dataModelId);

        List<Object> jolt = new ArrayList<>();
        Map<String, Object> val = new HashMap<>();
        val.put("operation", "modify-overwrite-beta");
        Map<String, Object> spec = new HashMap<>();
        Map<String, Object> en = new HashMap<>();
        Map<String, Object> ae = new HashMap<>();
        for (Attribute a : dataModelInfo.getAttributes()) {
            Map<String, String> e = new HashMap<>();
            if (a.getValueType().equals("Integer")) {
                e.put("value", "=toInteger");
                ae.put(a.getName(), e);
            } else if (a.getValueType().equals("Double")) {
                e.put("value", "=toDouble");
                ae.put(a.getName(), e);
            } else if (a.getValueType().equals("Boolean")) {
                e.put("value", "=toBoolean");
                ae.put(a.getName(), e);
            } else if (a.getValueType().equals("BigDecimal")) {
                e.put("value", "=toDouble");
                ae.put(a.getName(), e);
            }
        }
        en.put("*", ae);
        spec.put("entities", en);
        val.put("spec", spec);
        jolt.add(val);
        List<PropertyVO> joltList = new ArrayList<>();
        PropertyVO prop = new PropertyVO();
        prop.setName("jolt-spec");
        log.info("Convert Data = {}", objectMapper.writeValueAsString(jolt));
        prop.setInputValue(objectMapper.writeValueAsString(jolt));
        joltList.add(prop);
        convertValueTypeProc.setRequiredProps(joltList);
        return convertValueTypeProc;
    }

    private NiFiComponentVO convertDateType(AdaptorVO convertor, String dataModelId)
        throws JsonProcessingException {
        DataModelVO dataModelInfo = dataSetSVC.getDataModelProperties(dataModelId);
        List<PropertyVO> DateList = new ArrayList<>();
        NiFiComponentVO convertDateTypeProc = new NiFiComponentVO();
        convertDateTypeProc.setName("ConvertDateType");
        convertDateTypeProc.setType("Processor");
        for (Attribute a : dataModelInfo.getAttributes()) {
            if (a.getValueType().equals("Date")) {
                for (NiFiComponentVO nifi : convertor.getNifiComponents()) {
                    if (nifi.getName().equals("DataSetProps")) {
                        for (PropertyVO prop : nifi.getRequiredProps()) {
                            if (prop.getDetail().equals("Date Format")) {
                                prop.setName(a.getName());
                                log.info("prop Data = {}", prop);
                                prop.setInputValue(
                                    "${" +
                                    a.getName() +
                                    ":toDate(\"" +
                                    prop.getInputValue() +
                                    "\", \"GMT\"):format(\"yyyy-MM-dd'T'HH:mm:ssXXX\", \"GMT\")}"
                                );
                                DateList.add(prop);
                            }
                        }
                        convertDateTypeProc.setRequiredProps(DateList);
                        return convertDateTypeProc;
                    }
                }
            }
        }
        return null;
    }

    /**
     * @param adaptorVO
     * @param rootProcessorGroupId Parent Process ID
     * @return ProcessGroup ID created Adaptor
     * @throws JsonProcessingException
     */
    private String createAdaptor(AdaptorVO adaptorVO, String rootProcessorGroupId)
        throws JsonProcessingException {
        String templateId = niFiSwaggerSVC.searchTempletebyName(adaptorVO.getName());

        // Create Dummy Template
        String adaptorId = niFiRestSVC.createDummyTemplate(rootProcessorGroupId, templateId);
        // Update Adaptor
        updateAdaptor(adaptorId, adaptorVO.getNifiComponents());

        return adaptorId;
    }

    private void updateAdaptor(String processorGroupId, List<NiFiComponentVO> NiFiComponents)
        throws JsonProcessingException {
        if (NiFiComponents.size() < 1) {
            log.error("Empty NiFi Components In Request Pipeline");
        }
        log.info("{}", NiFiComponents);
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
            // Check Token Expired
            niFiClientEntity.manageToken(niFiClientEntity.getAccessToken());
            
            TaskVO taskVO = new TaskVO();
            taskVO.setCommandId(commandId);
            taskVO.setInputParameter(null);
            taskVO.setStatus(TaskStatusCode.TASK_STATUS_WORKING.getCode());

            for (MonitoringCode action : actions){

                taskVO.setTaskName(action.toString());
                Integer taskId = niFiRestSVC.createTask(taskVO);

                if(!niFiRestSVC.monitoring(action, processGroupId)){
                    log.error("Fail to" + action + "Pipeline in NiFi : processGroupId = [{}]", processGroupId);
                    niFiRestSVC.updateTask(taskId, TaskStatusCode.TASK_STATUS_FAILED.getCode());
                    return false;
                }else{
                    niFiRestSVC.updateTask(taskId, TaskStatusCode.TASK_STATUS_FINISH.getCode());
                }
            }
            log.info("Success Delete Pipeline in NiFI : processGroupId = [{}]", processGroupId);
            return true;
        } catch (Exception e) {
            log.error("Fail to Delete Pipeline in NiFi : processGroupId = [{" + processGroupId + "}] error : " +  e);
            //TODO : niFiRestSVC.updateTask(taskId, TaskStatusCode.TASK_STATUS_FAILED.getCode());
            return false;
        }
    }

    public String updatePipeline(PipelineVO pipelineVO, Integer commandId) {
        try {
            niFiClientEntity.manageToken(niFiClientEntity.getAccessToken());
            String processGroupId = createPipeline(pipelineVO, commandId);
            if (deletePipeline(pipelineVO.getProcessorGroupId(), commandId)) {
                return processGroupId;
            } else {
                log.error(
                    "Fail to delete Pipeline in NiFi : processGroupId = [{}]",
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
     */
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

        if (niFiRestSVC.stopProcessorGroup(processorGroupId)) {
            log.info("Success Stop Pipeline : Processor Group ID = {}", processorGroupId);
            return true;
        } else {
            log.error("Fail to Stop Pipeline : Processor Group ID = {}", processorGroupId);
            return false;
        }
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
