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
    public void updatePipelineDrafts(String requestBody) {
        parseJSON(requestBody, "collector");
        parseJSON(requestBody, "filter");
        parseJSON(requestBody, "converter");
    }

    public void parseJSON(String requestBody, String nifiFlowType) {
        int idx = 0;
        int completeCnt = 0;

        JSONObject jsonObject = new JSONObject(requestBody);
        if (jsonObject.isNull(nifiFlowType)) {
            pipelineMapper.updatePipelineDrafts(
                jsonObject.getInt("id"),
                jsonObject.getString("name"),
                jsonObject.getString("detail"),
                null,
                nifiFlowType
            );
        } else {
            String flowJsonString = jsonObject.getJSONObject(nifiFlowType).toString();

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

            log.info("Cnt : " + completeCnt);
            if (completeCnt == nifiComponentLength) {
                jsonObject.getJSONObject(nifiFlowType).remove("completed");
                jsonObject.getJSONObject(nifiFlowType).put("completed", true);
                flowJsonString = jsonObject.getJSONObject(nifiFlowType).toString();
            }

            pipelineMapper.updatePipelineDrafts(
                jsonObject.getInt("id"),
                jsonObject.getString("name"),
                jsonObject.getString("detail"),
                flowJsonString,
                nifiFlowType
            );
        }
    }

    @Transactional
    public void deletePipelineDrafts(Integer id) {
        pipelineMapper.deletePipelineDrafts(id);
    }
}
