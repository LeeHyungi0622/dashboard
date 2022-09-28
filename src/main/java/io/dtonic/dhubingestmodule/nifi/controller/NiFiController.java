package io.dtonic.dhubingestmodule.nifi.controller;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.crypto.modes.NISTCTSBlockCipher;
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
    DataSetSVC dataSetSVC;

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
            String processGroupId = niFiSwaggerSVC.createProcessGroup(
                pipelineVO.getName(),
                niFiApplicationRunner.getIngestProcessGroupId()
            );

            // Create Output Port
            String outputId = niFiSwaggerSVC.createOutputInPipeline(
                processGroupId,
                pipelineVO.getName()
            );
            //TODO
            /* Create Adatpors */
            // Create Collector Adaptor
            String collectorId = createAdaptor(pipelineVO.getCollector(), processGroupId);
            // Create Filter Adaptor
            String filterId = createAdaptor(pipelineVO.getFilter(), processGroupId);
            // Create Convertor Adaptor
            AdaptorVO formattedNiFiProps = convertPipelineVOToNiFiForm(pipelineVO);
            String convertorId = createAdaptor(formattedNiFiProps, processGroupId);

            /* Create Connections */
            // Create Collector - Filter Connection
            niFiSwaggerSVC.createConnectionBetweenProcessGroup(
                processGroupId,
                collectorId,
                filterId
            );
            // Create Filter - Convertor Connection
            niFiSwaggerSVC.createConnectionBetweenProcessGroup(
                processGroupId,
                filterId,
                convertorId
            );
            // Create Convertor - Output Port Connection
            niFiSwaggerSVC.createConnectionFromProcessGroupToOutput(
                processGroupId,
                convertorId,
                processGroupId
            );
            // Create Output Port - Funnel Connection
            niFiSwaggerSVC.createConnectionFromOutputToFunnel(
                niFiApplicationRunner.getIngestProcessGroupId(),
                processGroupId,
                niFiApplicationRunner.getIngestProcessGroupId()
            );
            // Enable all Controllers
            niFiRestSVC.enableControllers(processGroupId);

            log.info("Success Create Pipeline in NiFi : PipelineVO = {}", pipelineVO);
            return processGroupId;
        } catch (Exception e) {
            log.error("Fail to Create Pipeline in NiFi : PipelineVO = {}", pipelineVO, e);
            return null;
        }
    }

    //TODO
    private AdaptorVO convertPipelineVOToNiFiForm(PipelineVO convertor) {
        // DataSet ID로 Data Model 정보 호출

        // ID 생성
        String id = idGenerater(convertor);
        // Convert NGSI LD
        PipelineVO ngsiLdNiFi = setAttribute(convertor);
        NiFiComponentVO ngsiLdFormater = convertNgsiLdData(convertor.getDataModel(), id);
        NiFiComponentVO convertType = convertValueType(convertor.getDataModel());

        // NiFi Components Setting

        ngsiLdNiFi.getConverter().getNifiComponents().add(ngsiLdFormater);
        ngsiLdNiFi.getConverter().getNifiComponents().add(convertType);

        return ngsiLdNiFi.getConverter();
    }

    private PipelineVO setAttribute(PipelineVO convertor) {
        for (NiFiComponentVO nifi : convertor.getConverter().getNifiComponents()) {
            if (nifi.getName().equals("DataSetProps")) {
                for (PropertyVO prop : nifi.getRequiredProps()) {
                    String convertInput = prop.getInputValue().replace("\\\"", "");
                    prop.setInputValue("$." + convertInput);
                }
            }
        }
        return convertor;
    }

    private String idGenerater(PipelineVO convertor) {
        String id = "\"urn:datahub:" + convertor.getDataModel() + ":";
        for (NiFiComponentVO nifi : convertor.getConverter().getNifiComponents()) {
            if (nifi.getName().equals("IDGenerater")) {
                for (int i = 0; i < nifi.getRequiredProps().size(); i++) {
                    if (i < nifi.getRequiredProps().size() - 1) {
                        id = id + "${" + nifi.getRequiredProps().get(i).getInputValue() + "}:";
                    } else {
                        id = id + "${" + nifi.getRequiredProps().get(i).getInputValue() + "}\"";
                    }
                }
            }
        }
        return id;
    }

    private NiFiComponentVO convertNgsiLdData(String dataModelId, String id) {
        NiFiComponentVO ngsiLdFormmater = new NiFiComponentVO();
        ngsiLdFormmater.setName("NGSI-LD Fommater");
        ngsiLdFormmater.setType("Processor");
        DataModelVO dataModelInfo = dataSetSVC.getDataModelProperties(dataModelId);

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
            }
            //TODO:IDK
            // else if(e.getAttributeType().equals("GeoProperty")){
            //     subAttribute.put("IDK","IDK")
            // }
            if (e.getHasObservedAt()) {
                subAttribute.put("observedAt", "time");
            }
            // TODO:IDK
            // if(e.getHasUnitCode()){
            //     subAttribute.put("observedAt","time");
            // }
            entity.put(e.getName(), subAttribute);
        }
        entities.add(entity);
        converterVO.setEntities(entities);
        String convertData = converterVO.toString();
        List<PropertyVO> props = new ArrayList<>();
        PropertyVO ngsiProp = new PropertyVO();
        ngsiProp.setName("Replacement Value");
        ngsiProp.setInputValue(convertData);
        props.add(ngsiProp);
        ngsiLdFormmater.setRequiredProps(props);
        return ngsiLdFormmater;
    }

    private NiFiComponentVO convertValueType(String dataModelId) {
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
            }
            //Date 추가.. //Geo는 어캄?
        }
        en.put("*", ae);
        spec.put("entities", en);
        val.put("spec", spec);
        jolt.add(val);
        List<PropertyVO> joltList = new ArrayList<>();
        PropertyVO prop = new PropertyVO();
        prop.setName("jolt-spec");
        prop.setInputValue(jolt.toString());
        joltList.add(prop);
        convertValueTypeProc.setRequiredProps(joltList);
        return convertValueTypeProc;
    }

    // 다시짜자
    /**
     * @param adaptorVO
     * @param rootProcessorGroupId Parent Process ID
     * @return ProcessGroup ID created Adaptor
     */
    public String createAdaptor(AdaptorVO adaptorVO, String rootProcessorGroupId) {
        String templateId = niFiSwaggerSVC.searchTempletebyName(adaptorVO.getName());

        // Create Dummy Template
        String adaptorId = niFiRestSVC.createDummyTemplate(templateId, rootProcessorGroupId);
        // Update Adaptor
        updateAdaptor(adaptorId, adaptorVO.getNifiComponents());

        return adaptorId;
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

        niFiClientEntity.manageToken(niFiClientEntity.getAccessToken());
        if (niFiRestSVC.stopProcessorGroup(processorGroupId)) {
            log.info("Success Stop Pipeline : Processor Group ID = {}", processorGroupId);
            return true;
        } else {
            log.error("Fail to Stop Pipeline : Processor Group ID = {}", processorGroupId);
            return false;
        }
    }
}
