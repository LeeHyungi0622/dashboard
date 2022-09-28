package io.dtonic.dhubingestmodule.pipeline.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.google.gson.JsonObject;
import io.dtonic.dhubingestmodule.common.code.PipelineStatusCode;
import io.dtonic.dhubingestmodule.common.code.ResponseCode;
import io.dtonic.dhubingestmodule.nifi.vo.AdaptorVO;
import io.dtonic.dhubingestmodule.nifi.vo.NiFiComponentVO;
import io.dtonic.dhubingestmodule.nifi.vo.PropertyVO;
import io.dtonic.dhubingestmodule.pipeline.mapper.PipelineMapper;
import io.dtonic.dhubingestmodule.pipeline.vo.DataCollectorVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineCreateVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineListResponseVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineVO;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.ObjIntConsumer;
import java.util.stream.Collector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import netscape.javascript.JSException;
import netscape.javascript.JSObject;
import org.apache.commons.lang3.ObjectUtils.Null;
import org.bouncycastle.cms.jcajce.JceKEKAuthenticatedRecipient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Slf4j
public class PipelineSVC {

    @Autowired
    private PipelineMapper pipelineMapper;

    @Autowired
    private ObjectMapper objectMapper;

    // private final NiFoControlSVC nificontrolSVC;
    @Transactional
    public List<PipelineListResponseVO> getPipelineList() {
        List<PipelineListResponseVO> pipelineListVOs = pipelineMapper.getPipelineList();
        for (int i = 0; i < pipelineListVOs.size(); i++) {
            String temp_status = "Starting"; // Nifi API Response stats 값 가져와서 (현재 임시값)
            String cur_status = pipelineListVOs.get(i).getStatus(); // 현재 DB status값
            if (
                cur_status.equals(PipelineStatusCode.PIPELINE_STATUS_STARTING.getCode()) ||
                cur_status.equals(PipelineStatusCode.PIPELINE_STATUS_STOPPING.getCode())
            ) { // DB 상태 Starting or Stopping 경우 상태변화
                // 일어남
                if (
                    cur_status.equals(PipelineStatusCode.PIPELINE_STATUS_STARTING.getCode()) &&
                    temp_status.equals(PipelineStatusCode.PIPELINE_NIFISTATUS_RUNNING.getCode())
                ) pipelineListVOs
                    .get(i)
                    .setStatus(PipelineStatusCode.PIPELINE_STATUS_RUN.getCode()); else if ( // DB상태 Run // DB상태 Starting Nifi 상태 Running =>
                    cur_status.equals(PipelineStatusCode.PIPELINE_STATUS_STOPPING.getCode()) &&
                    temp_status.equals(PipelineStatusCode.PIPELINE_NIFISTATUS_STOP.getCode())
                ) pipelineListVOs
                    .get(i)
                    .setStatus(PipelineStatusCode.PIPELINE_STATUS_STOPPED.getCode()); // DB상태 Stopped // DB상태 Stopping Nifi 상태 Stop =>
                changePipelineStatus(
                    pipelineListVOs.get(i).getId(),
                    pipelineListVOs.get(i).getStatus()
                );
            }
        }
        return pipelineListVOs;
    }

    public List<DataCollectorVO> getDataCollector() {
        return pipelineMapper.getDataCollector();
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

    public void createPipeline(
        String creator,
        String name,
        String detail,
        String status,
        String data_set,
        String collector,
        String filter,
        String converter
    ) {
        // NIFI API response data_set
        pipelineMapper.createPipeline(
            creator,
            name,
            detail,
            status,
            data_set,
            collector,
            filter,
            converter
        );
    }

    static boolean isStringEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public Boolean isExists(Integer id) {
        return pipelineMapper.isExists(id);
    }

    public PipelineVO getPipelineVOById(Integer id)
        throws JsonMappingException, JsonProcessingException {
        // PipelineVO result = pipelineMapper.getPipeline(id);

        // List<AdaptorVO> map = objectMapper.readValue(result.getCollector(), JSONObject.class);
        // return map;
        PipelineVO result = pipelineMapper.getPipeline(id);
        return result;
    }

    @Transactional
    public void changePipelineStatus(Integer id, String status) {
        pipelineMapper.changePipelineStatus(id, status);
    }

    @Transactional
    public void deletePipeline(Integer id) {
        // check nifi run or stop. if running then stop to nifi
        pipelineMapper.deletePipeline(id);
    }

    @Transactional
    public void updatePipeline(JSONObject jsonObject) {
        parseJSON(jsonObject, "collector");
        parseJSON(jsonObject, "filter");
        parseJSON(jsonObject, "converter");
    }

    @Transactional
    public void parseJSON(JSONObject jsonObject, String nifiFlowType) {
        // collector, filter, converter를 설정하지 않은 초기 단계 에서는 jsonString을 null로 설정
        if (jsonObject.isNull(nifiFlowType)) {
            pipelineMapper.updatePipeline(
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
                pipelineMapper.updatePipeline(
                    jsonObject.getInt("id"),
                    jsonObject.getString("name"),
                    jsonObject.getString("detail"),
                    null,
                    flowJsonString,
                    nifiFlowType
                );
            } else {
                pipelineMapper.updatePipeline(
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
