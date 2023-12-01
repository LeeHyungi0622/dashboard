package io.dtonic.dhubingestmodule.nifi.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.dtonic.dhubingestmodule.dataset.service.DataSetSVC;
import io.dtonic.dhubingestmodule.dataset.vo.Attribute;
import io.dtonic.dhubingestmodule.dataset.vo.DataModelVO;
import io.dtonic.dhubingestmodule.nifi.vo.NiFiComponentVO;
import io.dtonic.dhubingestmodule.nifi.vo.PropertyVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineVO;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
public class NiFiConvertPropsSVC {
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    DataSetSVC dataSetSVC;

    public NiFiComponentVO extractDataSetPropsProcessor(PipelineVO convertor) {
        NiFiComponentVO dataSetPropComp = new NiFiComponentVO();
        dataSetPropComp.setName("ExtractDataSetProps");
        dataSetPropComp.setType("processor");
        List<PropertyVO> dataSetProps = new ArrayList<>();
        for (NiFiComponentVO nifi : convertor.getConverter().getNifiComponents()) {
            if (nifi.getName().equals("DataSetProps")) {
                for (PropertyVO prop : nifi.getRequiredProps()) {
                    if (prop.getDetail().equals("Property") || prop.getDetail().equals("Relationship")) {
                        if(!prop.getType().contains("Array") && !prop.getType().contains("Object")){
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

    public NiFiComponentVO convertDateTypeProcessor(PipelineVO pipeline)
        throws JsonProcessingException {
        DataModelVO dataModelInfo = dataSetSVC.getDataModelProperties(pipeline.getDataModel());
        List<PropertyVO> DataList = new ArrayList<>();
        NiFiComponentVO convertDateTypeProc = new NiFiComponentVO();
        convertDateTypeProc.setName("ConvertDateType");
        convertDateTypeProc.setType("Processor");
        for (Attribute a : dataModelInfo.getAttributes()) {
            if (a.getValueType().equals("Date")) {
                for (NiFiComponentVO nifi : pipeline.getConverter().getNifiComponents()) {
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
    
    public NiFiComponentVO convertArrayTypeProcessor(PipelineVO pipeline)
        throws JsonProcessingException {
        DataModelVO dataModelInfo = dataSetSVC.getDataModelProperties(pipeline.getDataModel());
        List<PropertyVO> DataList = new ArrayList<>();
        NiFiComponentVO convertDateTypeProc = new NiFiComponentVO();
        convertDateTypeProc.setName("ConvertArrayType");
        convertDateTypeProc.setType("Processor");
        for (Attribute a : dataModelInfo.getAttributes()) {
            
            if (a.getValueType().contains("Array")){
                for (NiFiComponentVO nifi : pipeline.getConverter().getNifiComponents()) {
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
                                    "${convert_data:jsonPath('$." +
                                    input.toString() +
                                    "')}"
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
    
    public NiFiComponentVO convertObjectTypeProcessor(PipelineVO pipeline)
        throws JsonProcessingException {
        DataModelVO dataModelInfo = dataSetSVC.getDataModelProperties(pipeline.getDataModel());
        List<PropertyVO> DataList = new ArrayList<>();
        NiFiComponentVO convertDateTypeProc = new NiFiComponentVO();
        convertDateTypeProc.setName("ConvertObjectType");
        convertDateTypeProc.setType("Processor");
        for (Attribute a : dataModelInfo.getAttributes()) {
            
            if (a.getValueType().contains("Object") && !a.getValueType().contains("Array")){
                for (NiFiComponentVO nifi : pipeline.getConverter().getNifiComponents()) {
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
                                    "${convert_data:jsonPath('$." +
                                    input.toString() +
                                    "')}"
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
    
    public NiFiComponentVO convertGeoJsonTypeProcessor(PipelineVO pipeline)
        throws JsonProcessingException {
        DataModelVO dataModelInfo = dataSetSVC.getDataModelProperties(pipeline.getDataModel());
        List<PropertyVO> DataList = new ArrayList<>();
        NiFiComponentVO convertDateTypeProc = new NiFiComponentVO();
        convertDateTypeProc.setName("ConvertGeoJsonType");
        convertDateTypeProc.setType("Processor");
        for (Attribute a : dataModelInfo.getAttributes()) {
            if (a.getValueType().equals("GeoJson")){
                for (NiFiComponentVO nifi : pipeline.getConverter().getNifiComponents()) {
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
                                    "${convert_data:jsonPath('$." +
                                    input.toString() +
                                    "')}"
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

    public NiFiComponentVO convertNgsiLdFormatProcessor(PipelineVO pipeline)
        throws JsonProcessingException {
        NiFiComponentVO processor = new NiFiComponentVO();
        processor.setName("ConvertNgsiLdFormat");
        processor.setType("processor");
        List<PropertyVO> props = new ArrayList<>();
        PropertyVO prop = new PropertyVO();
        prop.setName("Replacement Value");
        StringBuilder convertData = new StringBuilder();
        convertData.append("${start_entity:unescapeJson()");
        convertData.append(":append(${generatedEntityId:append(','):unescapeJson()})");
        convertData.append(":append(${generatedType:append(','):unescapeJson()})");
        convertData.append(":append(${contextString:unescapeJson()})");
        DataModelVO dataModelInfo = dataSetSVC.getDataModelProperties(pipeline.getDataModel());
        for (int idx = 0 ; idx < dataModelInfo.getAttributes().size(); idx ++){
            convertData.append(":append(${");
            convertData.append(dataModelInfo.getAttributes().get(idx).getName());
            convertData.append(":isEmpty():ifElse('',${");
            convertData.append(dataModelInfo.getAttributes().get(idx).getName()+"_string");
            convertData.append(":prepend(',')})})");
        }
        convertData.append(":append(${end_entity})");
        convertData.append("}");
        prop.setInputValue(convertData.toString());
        props.add(prop);
        processor.setRequiredProps(props);

        return processor;
    }

    public NiFiComponentVO mergeNgsiLdFormatProcessor()
        throws JsonProcessingException {
        NiFiComponentVO processor = new NiFiComponentVO();
        processor.setName("MergeNgsiLdFormat");
        processor.setType("processor");
        List<PropertyVO> props = new ArrayList<>();
        PropertyVO prop = new PropertyVO();
        prop.setName("Replacement Value");
        StringBuilder convertData = new StringBuilder();
        convertData.append("${init:unescapeJson():append(${merge_content}):append(${end_entity})}");

        prop.setInputValue(convertData.toString());
        props.add(prop);
        processor.setRequiredProps(props);

        return processor;
    }
    
    public NiFiComponentVO setNgsiLdStringProcessor(PipelineVO pipeline)
        throws JsonProcessingException {
        NiFiComponentVO processor = new NiFiComponentVO();
        processor.setName("SetNgsiLdString");
        processor.setType("processor");
        DataModelVO dataModelInfo = dataSetSVC.getDataModelProperties(pipeline.getDataModel());
        List<PropertyVO> props = new ArrayList<>();
        PropertyVO preProp = new PropertyVO();
        preProp.setName("init");
        preProp.setInputValue("{\"datasetId\":\"" + pipeline.getDataSet() + "\",\"entities\":[{");
        props.add(preProp);
        PropertyVO postProp = new PropertyVO();
        postProp.setName("end_entity");
        postProp.setInputValue("}");
        props.add(postProp);
        PropertyVO startProp = new PropertyVO();
        startProp.setName("start_entity");
        startProp.setInputValue("{}");
        props.add(startProp);
        PropertyVO generatedEntityId = new PropertyVO();
        generatedEntityId.setName("generatedEntityId");
        generatedEntityId.setInputValue("\"id\":\"" + idGenerater(pipeline) + "\"");
        props.add(generatedEntityId);
        PropertyVO generatedType = new PropertyVO();
        generatedType.setName("generatedType");
        generatedType.setInputValue("\"type\":\"" + pipeline.getDataModel() + "\"");
        props.add(generatedType);
        PropertyVO contextString = new PropertyVO();
        contextString.setName("contextString");
        StringBuilder context = new StringBuilder();
        context.append("\"@context\":[");
        for (String link : dataModelInfo.getContext()){
            context.append("\""+link+"\",");
        }
        context.deleteCharAt(context.length() - 1);
        context.append("]");
        contextString.setInputValue(context.toString());
        props.add(contextString);
        for (Attribute e : dataModelInfo.getAttributes()) {
            PropertyVO prop = new PropertyVO();
            prop.setName(e.getName()+"_string");
            StringBuilder input = new StringBuilder();
            input.append("\"" + e.getName() + "\":{");
            input.append("\"type\":\"" + e.getAttributeType() + "\",");
            if (e.getAttributeType().equals("Property")) {
                if(!e.getValueType().equals("Date") && !e.getValueType().equals("String")){
                    input.append("\"value\":${" + e.getName() + "}");
                } else {
                    input.append("\"value\":\"${" + e.getName() + ":replace('\\\"','\\\\\\\"')}\"");
                }
            } else if (e.getAttributeType().equals("Relationship")) {
                input.append("\"object\":\"${" + e.getName() + "}\"");
            } else if (e.getAttributeType().equals("GeoProperty")) {
                input.append("\"value\":{");
                for (NiFiComponentVO niFiComponentVO : pipeline.getConverter().getNifiComponents()) {
                    if (niFiComponentVO.getName().equals("DataSetProps")) {
                        for (PropertyVO propertyVO : niFiComponentVO.getRequiredProps()) {
                            if (propertyVO.getDetail().equals("GeoType")) {
                                input.append("\"type\":\"" + propertyVO.getInputValue() + "\",");
                                input.append("\"coordinates\":${" + e.getName() + "}");
                                break;
                            }
                        }
                    }
                }
                input.append("}");
            }
            if (e.getHasObservedAt()) {
                input.append(",\"observedAt\":\"${observedAt}\"");
            }
            if (e.getHasUnitCode()) {
                input.append(",\"unitCode\":\"${observedAt}\"");
            }
            input.append("}");
            prop.setInputValue(input.toString());
            props.add(prop);
        }
        
        processor.setRequiredProps(props);

        return processor;
    }
    
}
