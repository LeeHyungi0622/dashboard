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
        
        /* Check Token Expired */
        niFiClientEntity.manageToken();
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
            String collectorId = createAdaptor(pipelineVO.getCollector(), processGroupId);
            if(collectorId != null){
                niFiRestSVC.updateTask(taskId, TaskStatusCode.TASK_STATUS_FINISH.getCode());
            }

            /* Create Adatpors Filter */
            taskVO.setTaskName(MonitoringCode.CREATE_ADAPTOR_FILTER.getCode());
            taskId = niFiRestSVC.createTask(taskVO);
            String filterId = createAdaptor(pipelineVO.getFilter(), processGroupId);
            if(filterId != null){
                niFiRestSVC.updateTask(taskId, TaskStatusCode.TASK_STATUS_FINISH.getCode());
            }
            /* Create Adatpors Convertor */
            taskVO.setTaskName(MonitoringCode.CREATE_ADAPTOR_CONVERTOR.getCode());
            taskId = niFiRestSVC.createTask(taskVO);
            AdaptorVO formattedNiFiProps = convertPipelineVOToNiFiForm(pipelineVO);
            String convertorId = createAdaptor(formattedNiFiProps, processGroupId);
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

    private AdaptorVO convertPipelineVOToNiFiForm(PipelineVO convertor){
        // DataSet ID로 Data Model 정보 호출
        try{
            // ID 생성
            String id = idGenerater(convertor);
            // Convert NGSI LD
            NiFiComponentVO ngsiLdNiFi = setAttribute(convertor.getConverter());
            NiFiComponentVO ngsiLdFormater = convertNgsiLdData(
                convertor.getConverter(),
                convertor.getDataModel(),
                convertor.getDataSet(),
                id
            );
            NiFiComponentVO dateFormater = convertDateType(
                convertor.getConverter(),
                convertor.getFilter(),
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
        catch(Exception e){
            log.error(" convertPipelineVOToNiFiForm error " , e);
            return null;
        }
    }

    private NiFiComponentVO setAttribute(AdaptorVO convertor) {
        NiFiComponentVO dataSetPropComp = new NiFiComponentVO();
        dataSetPropComp.setName("DataSetProps");
        dataSetPropComp.setType("processor");
        List<PropertyVO> dataSetProps = new ArrayList<>();
        for (NiFiComponentVO nifi : convertor.getNifiComponents()) {
            if (nifi.getName().equals("DataSetProps")) {
                for (PropertyVO prop : nifi.getRequiredProps()) {
                    if (prop.getDetail().equals("Property") || prop.getDetail().equals("Relationship")) {
                        if(!prop.getType().contains("Array")){
                            PropertyVO newProp = new PropertyVO();
                            newProp.setName(prop.getName());
                            newProp.setDetail(prop.getDetail());
                            StringBuffer input = new StringBuffer();
                            String removeString = prop.getInputValue().replaceAll("\"", "");
                            for (String p : removeString.split("[.]")){
                                if (p.contains(" ") || p.contains("(")){
                                    input.append("['").append(p).append("']").append(".");
                                } else {
                                    input.append(p).append(".");
                                }
                            }
                            input.deleteCharAt(input.length() - 1);
                            newProp.setInputValue("$." + input.toString());
                            dataSetProps.add(newProp);
                        }
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
                            id =
                                id +
                                ":${" +
                                nifi
                                    .getRequiredProps()
                                    .get(i)
                                    .getInputValue()
                                    .replace("\"", "") +
                                "}";
                            
                        }
                    }
                    log.info("Create ID Gen : [{}]", id);
                    return id;
                }
            }
        }
        return null;
    }

    private NiFiComponentVO convertNgsiLdData(AdaptorVO adaptorVO, String dataModelId, String dataSetId, String id)
        throws JsonProcessingException {
        NiFiComponentVO ngsiLdFormmater = new NiFiComponentVO();
        ngsiLdFormmater.setName("NGSI-LD Fommater");
        ngsiLdFormmater.setType("processor");
        DataModelVO dataModelInfo = dataSetSVC.getDataModelProperties(dataModelId);
        log.debug("Data Model get Properties : [{}]", dataModelInfo);
        ConverterVO converterVO = new ConverterVO();
        List<Object> entities = new ArrayList<>();
        converterVO.setDatasetId(dataModelInfo.getDatasetId());
        Map<String, Object> entity = new HashMap<>();
        entity.put("id", id);
        entity.put("type", dataModelInfo.getType());
        entity.put("@context", dataModelInfo.getContext());
        List<String> geoPropNameList = new ArrayList<>();
        List<String> numberPropNameList = new ArrayList<>();
        for (Attribute e : dataModelInfo.getAttributes()) {
            Map<String, Object> subAttribute = new HashMap<>();
            subAttribute.put("type", e.getAttributeType());
            if (e.getAttributeType().equals("Property")) {
                subAttribute.put("value", "${" + e.getName() + "}");
                if(!e.getValueType().equals("Date") && !e.getValueType().equals("String")){
                    numberPropNameList.add(e.getName());
                } 
            } else if (e.getAttributeType().equals("Relationship")) {
                subAttribute.put("object", "${" + e.getName() + "}");
            } else if (e.getAttributeType().equals("GeoProperty")) {
                Map<String, Object> geoProp = new HashMap<>();
                for (NiFiComponentVO niFiComponentVO : adaptorVO.getNifiComponents()) {
                    if (niFiComponentVO.getName().equals("DataSetProps")) {
                        for (PropertyVO propertyVO : niFiComponentVO.getRequiredProps()) {
                            if (propertyVO.getDetail().equals("GeoType")) {
                                geoProp.put("type", propertyVO.getInputValue());
                                geoProp.put("coordinates", "${" + e.getName() + "}");
                                geoPropNameList.add(e.getName());
                            }
                        }
                    }
                }
                subAttribute.put("value", geoProp);
            }
            if (e.getHasObservedAt()) {
                subAttribute.put("observedAt", "${observedAt}");
            }
            if (e.getHasUnitCode()) {
                subAttribute.put("unitCode", "${observedAt}");
            }
            entity.put(e.getName(), subAttribute);
        }
        entities.add(entity);
        converterVO.setEntities(entities);
        converterVO.setDatasetId(dataSetId);
        String convertData = objectMapper.writeValueAsString(converterVO);
        for (String geoPropName : geoPropNameList) {
            convertData = convertData.replace("\"coordinates\":\"${" + geoPropName + "}\"", "\"coordinates\":${" + geoPropName + "}");
        }
        for (String numPropName : numberPropNameList) {
            convertData = convertData.replace("\"value\":\"${" + numPropName + "}\"", "\"value\":${" + numPropName + "}");
        }
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
        val.put("operation", "modify-default-beta");
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
            } else if (a.getValueType().equals("String")) {
                e.put("value", "=toString");
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
        prop.setInputValue(objectMapper.writeValueAsString(jolt));
        joltList.add(prop);
        convertValueTypeProc.setRequiredProps(joltList);
        return convertValueTypeProc;
    }

    private NiFiComponentVO convertDateType(AdaptorVO convertor,AdaptorVO filter, String dataModelId)
        throws JsonProcessingException {
        DataModelVO dataModelInfo = dataSetSVC.getDataModelProperties(dataModelId);
        List<PropertyVO> DataList = new ArrayList<>();
        NiFiComponentVO convertDateTypeProc = new NiFiComponentVO();
        convertDateTypeProc.setName("ConvertDateType");
        convertDateTypeProc.setType("Processor");
        for (Attribute a : dataModelInfo.getAttributes()) {
            if (a.getValueType().equals("GeoJson")){
                for (NiFiComponentVO nifi : convertor.getNifiComponents()) {
                    if (nifi.getName().equals("DataSetProps")) {
                        for (PropertyVO prop : nifi.getRequiredProps()) {
                            if (prop.getDetail().equals("GeoProperty") && prop.getName().equals(a.getName())) {
                                PropertyVO newProp = new PropertyVO();
                                StringBuffer input = new StringBuffer();
                                for (String p : prop.getInputValue().replaceAll("\"", "").split("[.]")){
                                    if (p.contains(" ") || p.contains("(")){
                                        input.append("['").append(p).append("']").append(".");
                                    } else {
                                        input.append(p).append(".");
                                    }
                                }
                                input.deleteCharAt(input.length() - 1);
                                newProp.setName(a.getName());
                                newProp.setInputValue(
                                    "${" +
                                    "raw_data" +
                                    ":jsonPath(\'$." + "${root_key}."+ // TEST 필요
                                    input.toString() +
                                    "\')}"
                                );
                                DataList.add(newProp);
                            }
                        }
                    }
                }
            }
            if (a.getValueType().contains("Array")){
                for (NiFiComponentVO nifi : convertor.getNifiComponents()) {
                    if (nifi.getName().equals("DataSetProps")) {
                        for (PropertyVO prop : nifi.getRequiredProps()) {
                            if (prop.getDetail().equals("Property") && prop.getName().equals(a.getName())) {
                                PropertyVO newProp = new PropertyVO();
                                newProp.setName(a.getName());
                                StringBuffer input = new StringBuffer();
                                for (String p : prop.getInputValue().replaceAll("\"", "").split("[.]")){
                                    if (p.contains(" ") || p.contains("(")){
                                        input.append("['").append(p).append("']").append(".");
                                    } else {
                                        input.append(p).append(".");
                                    }
                                }
                                input.deleteCharAt(input.length() - 1);
                                newProp.setInputValue(
                                    "${" +
                                    "raw_data" +
                                    ":jsonPath(\'$." + "${root_key}."+
                                    input.toString() +
                                    "\')}"
                                );
                                DataList.add(newProp);
                            }
                        }
                    }
                }
            }
            if (a.getValueType().equals("Date")) {
                for (NiFiComponentVO nifi : convertor.getNifiComponents()) {
                    if (nifi.getName().equals("DataSetProps")) {
                        for (PropertyVO prop : nifi.getRequiredProps()) {
                            if (prop.getDetail().equals("Date Format") && prop.getName().equals(a.getName())) {
                                PropertyVO newProp = new PropertyVO();
                                newProp.setName(a.getName());
                                newProp.setInputValue(
                                    "${" +
                                    a.getName() +
                                    ":toDate(\"" +
                                    prop.getInputValue() +
                                    "\", \"GMT\"):format(\"yyyy-MM-dd'T'HH:mm:ssXXX\", \"GMT\")}"
                                );
                                DataList.add(newProp);
                            }
                            
                        }
                        
                    }
                }
            }
        }
        convertDateTypeProc.setRequiredProps(DataList);
        return convertDateTypeProc;
    }

    /**
     * @param adaptorVO
     * @param rootProcessorGroupId Parent Process ID
     * @return ProcessGroup ID created Adaptor
     * @throws JsonProcessingException
     */
    private String createAdaptor(AdaptorVO adaptorVO, String rootProcessorGroupId) throws Exception{
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
            niFiClientEntity.manageToken();
            
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
        // Check Token Expired
        niFiClientEntity.manageToken();
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
        // Check Token Expired
        niFiClientEntity.manageToken();

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
            niFiClientEntity.manageToken();
            return niFiRestSVC.getStatusProcessGroup(processorGroup);
        } catch (Exception e) {
            log.error("Fail to Get Pipeline Status.", e);
            return null;
        }
    }
}
