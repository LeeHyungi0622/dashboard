package io.dtonic.dhubingestmodule.pipeline.service;

import io.dtonic.dhubingestmodule.dataset.service.DataSetSVC;
import io.dtonic.dhubingestmodule.dataset.vo.DataSetPropertiesResponseVO;
import io.dtonic.dhubingestmodule.nifi.vo.AdaptorVO;
import io.dtonic.dhubingestmodule.nifi.vo.NiFiComponentVO;
import io.dtonic.dhubingestmodule.nifi.vo.PropertyVO;
import io.dtonic.dhubingestmodule.pipeline.mapper.PipelineDraftMapper;
import io.dtonic.dhubingestmodule.pipeline.vo.DataCollectorVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineDraftsListResponseVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineVO;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Slf4j
public class PipelineDraftSVC {

    @Autowired
    private PipelineDraftMapper pipelineMapper;

    @Autowired
    private DataSetSVC datasetsvc;

    public void createPipelineDrafts(String name, String creator, String detail) {
        pipelineMapper.createPipelineDrafts(name, creator, detail);
    }

    public List<DataCollectorVO> getDataCollector() {
        return pipelineMapper.getDataCollector();
    }

    public PipelineVO getPipelineDraftsPage(
        Integer pipelineid,
        Integer page,
        String adaptorName,
        String datasetid
    ) {
        PipelineVO pipelineVO = pipelineMapper.getPipelineDrafts(pipelineid);
        AdaptorVO adaptorVO = getPipelineproperties(adaptorName, pipelineid);
        switch (page) {
            case 1: //수집기 선택시 (수집 pipelineVO 속성 리턴)
                pipelineVO.setCollector(adaptorVO);
                break;
            case 2: //수집에서 다음 누를때(정제 pipelineVO 속성 리턴)
                pipelineVO.setFilter(adaptorVO);
                break;
            case 3: //데이터셋 선택시 (변환 pipelineVO 속성 리턴)
                DataSetPropertiesResponseVO dataSetPropertiesResponseVO = datasetsvc.getDataModelId( //model ID 가져오기
                    datasetid
                );
                dataSetPropertiesResponseVO =
                    datasetsvc.getDataModelProperties(dataSetPropertiesResponseVO);
                NiFiComponentVO niFiComponentVO = new NiFiComponentVO();
                for (int i = 0; i < dataSetPropertiesResponseVO.getAttribute().size(); i++) {
                    PropertyVO propertyVO = new PropertyVO();
                    propertyVO.setName(dataSetPropertiesResponseVO.getAttribute().get(i).getName());
                    propertyVO.setDetail(
                        dataSetPropertiesResponseVO.getAttribute().get(i).getDescription()
                    );
                    niFiComponentVO.getRequiredProps().add(propertyVO);
                    niFiComponentVO.setName("DataSetProps");
                    niFiComponentVO.setType("Processor");
                }
                adaptorVO.getNifiComponents().add(niFiComponentVO);
                pipelineVO.setConverter(adaptorVO);
                break;
        }

        return pipelineVO;
    }

    public AdaptorVO getPipelineproperties(String adaptorName, Integer pipelineid) { //adaptor의 속성값 가져오기
        NiFiComponentVO niFiComponentVO = new NiFiComponentVO();
        List<NiFiComponentVO> niFiComponentVOs = new ArrayList<NiFiComponentVO>();
        List<PropertyVO> propertyVO = pipelineMapper.getPipelineproperties(adaptorName);
        AdaptorVO adaptorVO = new AdaptorVO();
        Integer cur_adaptor_id = propertyVO.get(0).getAdaptorId();
        for (int i = 0; i < propertyVO.size(); i++) {
            if (propertyVO.get(i).getAdaptorId() != cur_adaptor_id) {
                cur_adaptor_id = propertyVO.get(i).getAdaptorId();
                niFiComponentVO.setName(propertyVO.get(i - 1).getNifiName());
                niFiComponentVO.setType(propertyVO.get(i - 1).getNifiType());
                niFiComponentVOs.add(niFiComponentVO);
                niFiComponentVO = new NiFiComponentVO();
            }
            if (propertyVO.get(i).getIsRequired()) {
                if (
                    propertyVO.get(i).getDefaultValue().size() > 0 &&
                    isStringEmpty(propertyVO.get(i).getInputValue())
                ) {
                    propertyVO.get(i).setInputValue(propertyVO.get(i).getDefaultValue().get(0));
                }
                niFiComponentVO.getRequiredProps().add(propertyVO.get(i));
            } else {
                niFiComponentVO.getOptionalProps().add(propertyVO.get(i));
            }
            if (i == propertyVO.size() - 1) {
                niFiComponentVO.setName(propertyVO.get(i).getNifiName());
                niFiComponentVO.setType(propertyVO.get(i).getNifiType());
            }
        }
        niFiComponentVOs.add(niFiComponentVO);
        adaptorVO.setNifiComponents(niFiComponentVOs);
        adaptorVO.setName(adaptorName);
        return adaptorVO;
    }

    public Boolean isExistsDrafts(Integer id) {
        return pipelineMapper.isExistsDrafts(id);
    }

    @Transactional
    public void deletePipelineDrafts(Integer id) {
        pipelineMapper.deletePipelineDrafts(id);
    }

    @Transactional
    public void updatePipelineDrafts(JSONObject jsonObject) {
        parseJSON(jsonObject, "collector");
        parseJSON(jsonObject, "filter");
        parseJSON(jsonObject, "converter");
    }

    @Transactional
    public void parseJSON(JSONObject jsonObject, String nifiFlowType) {
        // collector, filter, converter를 설정하지 않은 초기 단계 에서는 jsonString을 null로 설정
        if (jsonObject.isNull(nifiFlowType)) {
            pipelineMapper.updatePipelineDrafts(
                jsonObject.getInt("id"),
                jsonObject.getString("name"),
                jsonObject.getString("detail"),
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
            int nifiComponentLength = jObject.getJSONArray("NifiComponents").length();

            for (int i = 0; i < nifiComponentLength; i++) {
                JSONObject jObj = new JSONObject(
                    jObject.getJSONArray("NifiComponents").get(i).toString()
                );
                JSONArray properties = jObj.getJSONArray("requiredProps");
                for (idx = 0; idx < properties.length(); idx++) {
                    if (properties.getJSONObject(idx).isNull("inputValue")) {
                        break;
                    }
                }

                if (idx == properties.length()) {
                    completeCnt++;
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
                pipelineMapper.updatePipelineDrafts(
                    jsonObject.getInt("id"),
                    jsonObject.getString("name"),
                    jsonObject.getString("detail"),
                    null,
                    flowJsonString,
                    nifiFlowType
                );
            } else {
                pipelineMapper.updatePipelineDrafts(
                    jsonObject.getInt("id"),
                    jsonObject.getString("name"),
                    jsonObject.getString("detail"),
                    jsonObject.getString("dataSet"),
                    flowJsonString,
                    nifiFlowType
                );
            }
        }
    }

    static boolean isStringEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public List<PipelineDraftsListResponseVO> getPipelineDraftsList(
        String searchObject,
        String searchValue
    ) {
        List<PipelineVO> pipelineVO = pipelineMapper.getPipelineDraftsList(
            searchObject,
            searchValue
        );

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
        return pipelineDraftsListResponseVO;
    }

    public PipelineVO getPipelineDrafts(Integer id) {
        PipelineVO pipelineVO = pipelineMapper.getPipelineDrafts(id);
        return pipelineVO;
    }
}
