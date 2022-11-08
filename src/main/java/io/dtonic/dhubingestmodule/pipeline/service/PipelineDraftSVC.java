package io.dtonic.dhubingestmodule.pipeline.service;

import io.dtonic.dhubingestmodule.common.code.AdaptorName;
import io.dtonic.dhubingestmodule.dataset.service.DataSetSVC;
import io.dtonic.dhubingestmodule.dataset.vo.DataModelVO;
import io.dtonic.dhubingestmodule.nifi.vo.AdaptorVO;
import io.dtonic.dhubingestmodule.nifi.vo.NiFiComponentVO;
import io.dtonic.dhubingestmodule.nifi.vo.PropertyVO;
import io.dtonic.dhubingestmodule.pipeline.mapper.PipelineDraftMapper;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineDraftsListResponseVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineVO;
import io.dtonic.dhubingestmodule.util.ValidateUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PipelineDraftSVC {

    @Autowired
    private PipelineDraftMapper pipelineDraftMapper;

    @Autowired
    private DataSetSVC datasetsvc;

    public ResponseEntity createPipelineDrafts(String name, String creator, String detail) {
        int result = pipelineDraftMapper.createPipelineDrafts(name, creator, detail);
        if (result != 1) {
            return ResponseEntity
                .status(HttpStatus.SC_BAD_GATEWAY)
                .body("Create Draft Pipeline error in DB");
            // throw new BadRequestException(
            //     DataCoreUiCode.ErrorCode.BAD_REQUEST,
            //     "Create Draft Pipeline error in DB"
            // );
        }
        PipelineVO pipeline = (PipelineVO) getPipelineDrafts(
            pipelineDraftMapper.getPipelineIDbyName(name)
        )
            .getBody();
        return ResponseEntity.ok().body(pipeline);
    }

    public List<String> getDataCollector() {
        return pipelineDraftMapper.getDataCollector();
    }

    public ResponseEntity getPipelineDraftsProperties(
        Integer pipelineid,
        String page,
        String adaptorName,
        String datasetid
    ) {
        PipelineVO pipelineVO = pipelineDraftMapper.getPipelineDrafts(pipelineid);
        AdaptorVO adaptorVO = getPipelineproperties(adaptorName);
        if (page.equals(AdaptorName.ADAPTOR_NAME_COLLECTOR.getCode())) {
            pipelineVO.setCollector(adaptorVO);
        } else if (page.equals(AdaptorName.ADAPTOR_NAME_FILTER.getCode())) {
            pipelineVO.setFilter(adaptorVO);
        } else if (page.equals(AdaptorName.ADAPTOR_NAME_CONVERTER.getCode())) {
            DataModelVO dataModelVO = datasetsvc.getDataModelId( //model ID 가져오기
                datasetid
            );
            pipelineVO.setDataModel(dataModelVO.getId());
            pipelineVO.setDataSet(datasetid);
            dataModelVO = datasetsvc.getDataModelProperties(dataModelVO.getId());
            NiFiComponentVO niFiComponentVO = new NiFiComponentVO();
            for (int i = 0; i < dataModelVO.getAttributes().size(); i++) {
                if (Boolean.TRUE.equals(dataModelVO.getAttributes().get(i).getHasUnitCode())) {
                    PropertyVO propertyVO = new PropertyVO();
                    propertyVO.setName(dataModelVO.getAttributes().get(i).getName());
                    propertyVO.setDetail("unitCode");
                    niFiComponentVO.getRequiredProps().add(propertyVO);
                }
                if (dataModelVO.getAttributes().get(i).getValueType().equals("Date")) {
                    PropertyVO propertyVO = new PropertyVO();
                    propertyVO.setName(dataModelVO.getAttributes().get(i).getName());
                    propertyVO.setDetail("Date Format");
                    niFiComponentVO.getRequiredProps().add(propertyVO);
                }
                PropertyVO propertyVO = new PropertyVO();
                propertyVO.setName(dataModelVO.getAttributes().get(i).getName());
                propertyVO.setDetail(dataModelVO.getAttributes().get(i).getAttributeType());
                niFiComponentVO.getRequiredProps().add(propertyVO);
                niFiComponentVO.setName("DataSetProps");
                niFiComponentVO.setType("Processor");
            }
            adaptorVO.getNifiComponents().add(niFiComponentVO);
            pipelineVO.setConverter(adaptorVO);
        } else {
            return ResponseEntity.status(HttpStatus.SC_BAD_REQUEST).body("invalid Page name");
        }

        return ResponseEntity.ok().body(pipelineVO);
    }

    //adaptor의 속성값 가져오기
    public AdaptorVO getPipelineproperties(String adaptorName) {
        NiFiComponentVO niFiComponentVO = new NiFiComponentVO();
        List<NiFiComponentVO> niFiComponentVOs = new ArrayList<>();
        List<PropertyVO> propertyVO = pipelineDraftMapper.getPipelineproperties(adaptorName);
        AdaptorVO adaptorVO = new AdaptorVO();
        Integer cur_adaptor_id = propertyVO.get(0).getAdaptorId();
        for (int i = 0; i < propertyVO.size(); i++) {
            if (!Objects.equals(propertyVO.get(i).getAdaptorId(), cur_adaptor_id)) {
                niFiComponentVO.setName(
                    pipelineDraftMapper.getAdaptorinfo(cur_adaptor_id).getName()
                );
                niFiComponentVO.setType(
                    pipelineDraftMapper.getAdaptorinfo(cur_adaptor_id).getType()
                );
                cur_adaptor_id = propertyVO.get(i).getAdaptorId();
                niFiComponentVOs.add(niFiComponentVO);
                niFiComponentVO = new NiFiComponentVO();
            }
            if (Boolean.TRUE.equals(propertyVO.get(i).getIsRequired())) {
                if (
                    !propertyVO.get(i).getDefaultValue().isEmpty() &&
                    ValidateUtil.isStringEmpty(propertyVO.get(i).getInputValue())
                ) {
                    propertyVO.get(i).setInputValue(propertyVO.get(i).getDefaultValue().get(0));
                }
                niFiComponentVO.getRequiredProps().add(propertyVO.get(i));
            } else {
                if (
                    !propertyVO.get(i).getDefaultValue().isEmpty() &&
                    ValidateUtil.isStringEmpty(propertyVO.get(i).getInputValue())
                ) {
                    propertyVO.get(i).setInputValue(propertyVO.get(i).getDefaultValue().get(0));
                }
                niFiComponentVO.getOptionalProps().add(propertyVO.get(i));
            }
            if (i == propertyVO.size() - 1) {
                niFiComponentVO.setName(
                    pipelineDraftMapper.getAdaptorinfo(cur_adaptor_id).getName()
                );
                niFiComponentVO.setType(
                    pipelineDraftMapper.getAdaptorinfo(cur_adaptor_id).getType()
                );
            }
        }
        niFiComponentVOs.add(niFiComponentVO);
        adaptorVO.setNifiComponents(niFiComponentVOs);
        adaptorVO.setName(adaptorName);
        return adaptorVO;
    }

    @Transactional
    public ResponseEntity deletePipelineDrafts(Integer id) {
        int result = pipelineDraftMapper.deletePipelineDrafts(id);
        if (result != 1) {
            return ResponseEntity
                .status(HttpStatus.SC_BAD_REQUEST)
                .body("Not Exist to Delete Pipeline ID");
            // throw new BadRequestException(
            //     DataCoreUiCode.ErrorCode.BAD_REQUEST,
            //     "Not Exist to Delete Pipeline ID"
            // );
        }
        return ResponseEntity.ok().build();
    }

    @Transactional
    public PipelineVO updatePipelineDrafts(JSONObject jsonObject) {
        parseJSON(jsonObject, AdaptorName.ADAPTOR_NAME_COLLECTOR.getCode());
        parseJSON(jsonObject, AdaptorName.ADAPTOR_NAME_FILTER.getCode());
        parseJSON(jsonObject, AdaptorName.ADAPTOR_NAME_CONVERTER.getCode());
        PipelineVO pipeline = (PipelineVO) getPipelineDrafts(jsonObject.getInt("id")).getBody();
        return pipeline;
    }

    @Transactional
    public void parseJSON(JSONObject jsonObject, String nifiFlowType) {
        // collector, filter, converter를 설정하지 않은 초기 단계 에서는 jsonString을 null로 설정
        if (jsonObject.isNull(nifiFlowType)) {
            pipelineDraftMapper.updatePipelineDrafts(
                jsonObject.getInt("id"),
                jsonObject.getString("name"),
                jsonObject.getString("detail"),
                null,
                null,
                null,
                nifiFlowType
            );
            // collector, filter, converter 내의 processor의 필수 properties 값이 모두 채워졌는지 확인
        } else {
            int idx = 0;
            int completeCnt = 0; // 프로세서들의 필수 properties들이 모두 채워져있으면 1 증가
            String flowJsonString = jsonObject.getJSONObject(nifiFlowType).toString();

            JSONObject jObject = new JSONObject(flowJsonString);
            int nifiComponentLength = jObject.getJSONArray("nifiComponents").length();

            for (int i = 0; i < nifiComponentLength; i++) {
                JSONObject jObj = new JSONObject(
                    jObject.getJSONArray("nifiComponents").get(i).toString()
                );
                if (jObj.getString("name") == "IDGenerater") {
                    JSONArray properties = jObj.getJSONArray("requiredProps");
                    for (idx = 0; idx < properties.length(); idx++) {
                        if (
                            properties.getJSONObject(idx).getString("name") == "level2" ||
                            properties.getJSONObject(idx).getString("name") == "level3"
                        ) {
                            break;
                        } else if (
                            properties.getJSONObject(idx).isNull("inputValue") ||
                            properties.getJSONObject(idx).getString("inputValue") == ""
                        ) {
                            continue;
                        }
                    }
                    if (idx == properties.length()) {
                        completeCnt++;
                    }
                } else {
                    JSONArray properties = jObj.getJSONArray("requiredProps");
                    for (idx = 0; idx < properties.length(); idx++) {
                        if (
                            properties.getJSONObject(idx).isNull("inputValue") ||
                            properties.getJSONObject(idx).getString("inputValue") == ""
                        ) {
                            break;
                        }
                    }
                    if (idx == properties.length()) {
                        completeCnt++;
                    }
                }
            }

            // processor에서 필수로 넣어야 하는 properties 값들이 모두 채워져 있으면, completed를 true로 바꾼다
            if (completeCnt == nifiComponentLength) {
                jsonObject.getJSONObject(nifiFlowType).remove("completed");
                jsonObject.getJSONObject(nifiFlowType).put("completed", true);
                flowJsonString = jsonObject.getJSONObject(nifiFlowType).toString();
            } else {
                jsonObject.getJSONObject(nifiFlowType).remove("completed");
                jsonObject.getJSONObject(nifiFlowType).put("completed", false);
                flowJsonString = jsonObject.getJSONObject(nifiFlowType).toString();
            }

            // converter 단계에서 dataSet을 값을 설정한다.
            // converter 단계가 아니면 dataSet 값은 null로 처리
            if (jsonObject.isNull("dataSet")) {
                pipelineDraftMapper.updatePipelineDrafts(
                    jsonObject.getInt("id"),
                    jsonObject.getString("name"),
                    jsonObject.getString("detail"),
                    null,
                    null,
                    flowJsonString,
                    nifiFlowType
                );
            } else {
                pipelineDraftMapper.updatePipelineDrafts(
                    jsonObject.getInt("id"),
                    jsonObject.getString("name"),
                    jsonObject.getString("detail"),
                    jsonObject.getString("dataSet"),
                    jsonObject.getString("dataModel"),
                    flowJsonString,
                    nifiFlowType
                );
            }
        }
    }

    public ResponseEntity getPipelineDraftsList() {
        List<PipelineVO> pipelineVO = pipelineDraftMapper.getPipelineDraftsList();

        List<PipelineDraftsListResponseVO> pipelineDraftsListResponseVO = new ArrayList<>();
        for (int i = 0; i < pipelineVO.size(); i++) {
            PipelineDraftsListResponseVO draftsResponse = new PipelineDraftsListResponseVO();
            draftsResponse.setId(pipelineVO.get(i).getId());
            draftsResponse.setName(pipelineVO.get(i).getName());
            draftsResponse.setModifiedAt(pipelineVO.get(i).getModifiedAt());

            if (pipelineVO.get(i).getCollector() == null) {
                draftsResponse.setIsCollector(false);
            } else {
                draftsResponse.setIsCollector(pipelineVO.get(i).getCollector().isCompleted());
            }

            if (pipelineVO.get(i).getFilter() == null) {
                draftsResponse.setIsFilter(false);
            } else {
                draftsResponse.setIsFilter(pipelineVO.get(i).getFilter().isCompleted());
            }

            if (pipelineVO.get(i).getConverter() == null) {
                draftsResponse.setIsConverter(false);
            } else {
                draftsResponse.setIsConverter(pipelineVO.get(i).getConverter().isCompleted());
            }

            pipelineDraftsListResponseVO.add(draftsResponse);
        }
        return ResponseEntity.ok().body(pipelineDraftsListResponseVO);
    }

    public ResponseEntity getPipelineDrafts(Integer id) {
        PipelineVO result = pipelineDraftMapper.getPipelineDrafts(id);
        if (ValidateUtil.isEmptyData(result)) {
            return ResponseEntity
                .status(HttpStatus.SC_BAD_REQUEST)
                .body("Pipeline Not Exist in DB");
            // throw new BadRequestException(
            //     DataCoreUiCode.ErrorCode.NOT_EXIST_ENTITY,
            //     "Pipeline Not Exist in DB"
            // );
        }
        return ResponseEntity.ok().body(result);
    }

    public Boolean isExistsDrafts(Integer id) {
        return pipelineDraftMapper.isExistsDrafts(id);
    }

    public Boolean isExistsNameDrafts(String name) {
        return pipelineDraftMapper.isExistsNameDrafts(name);
    }
}
