package io.dtonic.dhubingestmodule.pipeline.service;

import io.dtonic.dhubingestmodule.pipeline.mapper.PipelineDraftMapper;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineResponseVO;
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

    public void createPipelineDrafts(String name, String creator, String detail) {
        pipelineMapper.createPipelineDrafts(name, creator, detail);
    }

    public List<PipelineResponseVO> getPipelineDraftsList(String searchObject, String searchValue) {
        List<PipelineResponseVO> pipelineVO = pipelineMapper.getPipelineDraftsList(
            searchObject,
            searchValue
        );
        return pipelineVO;
    }

    public PipelineResponseVO getPipelineDrafts(Integer id) {
        PipelineResponseVO pipelineVO = pipelineMapper.getPipelineDrafts(id);
        return pipelineVO;
    }

    public Boolean isExistsDrafts(Integer id) {
        return pipelineMapper.isExistsDrafts(id);
    }

    @Transactional
    public void deletePipelineDrafts(Integer id) {
        pipelineMapper.deletePipelineDrafts(id);
    }

    @Transactional
    public void updatePipelineDrafts(String requestBody) {
        parseJSON(requestBody, "collector");
        parseJSON(requestBody, "filter");
        parseJSON(requestBody, "converter");
    }

    @Transactional
    public void parseJSON(String requestBody, String nifiFlowType) {
        JSONObject jsonObject = new JSONObject(requestBody);

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
            String flowJsonString = jsonObject.getJSONArray(nifiFlowType).get(0).toString();

            JSONObject jObject = new JSONObject(flowJsonString);
            int nifiComponentLength = jObject.getJSONArray("NifiComponents").length();

            for (int i = 0; i < nifiComponentLength; i++) {
                JSONObject jObj = new JSONObject(
                    jObject.getJSONArray("NifiComponents").get(i).toString()
                );
                JSONArray properties = jObj.getJSONArray("properties");
                for (idx = 0; idx < properties.length(); idx++) {
                    if (
                        properties.getJSONObject(idx).getBoolean("isRequired") &&
                        properties.getJSONObject(idx).isNull("inputValue")
                    ) {
                        break;
                    }
                }

                if (idx == properties.length()) {
                    completeCnt++;
                }
            }

            // processor에서 필수로 넣어야 하는 properties 값들이 모두 채워져 있으면, completed를 true로 바꾼다
            if (completeCnt == nifiComponentLength) {
                jsonObject.getJSONArray(nifiFlowType).getJSONObject(0).remove("isCompleted");
                jsonObject.getJSONArray(nifiFlowType).getJSONObject(0).put("isCompleted", true);
                flowJsonString = jsonObject.getJSONArray(nifiFlowType).toString();
            } else {
                jsonObject.getJSONArray(nifiFlowType).getJSONObject(0).remove("isCompleted");
                jsonObject.getJSONArray(nifiFlowType).getJSONObject(0).put("isCompleted", false);
                flowJsonString = jsonObject.getJSONArray(nifiFlowType).toString();
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
}
