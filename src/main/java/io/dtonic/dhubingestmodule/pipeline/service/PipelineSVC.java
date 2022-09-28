package io.dtonic.dhubingestmodule.pipeline.service;

import io.dtonic.dhubingestmodule.common.code.DataCoreUiCode;
import io.dtonic.dhubingestmodule.common.code.PipelineStatusCode;
import io.dtonic.dhubingestmodule.common.exception.BadRequestException;
import io.dtonic.dhubingestmodule.pipeline.mapper.PipelineMapper;
import io.dtonic.dhubingestmodule.pipeline.vo.DataCollectorVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineListResponseVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineVO;
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
public class PipelineSVC {

    @Autowired
    private PipelineMapper pipelineMapper;

    @Autowired
    private PipelineDraftSVC pipelineDraftSVC;

    public void createPipeline(
        Integer id,
        String creator,
        String name,
        String detail,
        String status,
        String dataSet,
        String collector,
        String filter,
        String converter
    ) {
        // NIFI API response data_set
        int result = pipelineMapper.createPipeline(
            creator,
            name,
            detail,
            status,
            dataSet,
            collector,
            filter,
            converter
        );
        //임시 파이프라인 삭제
        if (result == 1) {
            pipelineDraftSVC.deletePipelineDrafts(id);
        } else {
            throw new BadRequestException(
                DataCoreUiCode.ErrorCode.CREATE_ENTITY_TABLE_ERROR,
                "Create Pipeline Error"
            );
        }
    }

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

    public PipelineVO getPipelineVOById(Integer id) {
        PipelineVO result = pipelineMapper.getPipeline(id);
        if (result == null) {
            throw new BadRequestException(
                DataCoreUiCode.ErrorCode.NOT_EXIST_ENTITY,
                "Pipeline Not Exist"
            );
        }
        return result;
    }

    @Transactional
    public void changePipelineStatus(Integer id, String status) {
        int result = pipelineMapper.changePipelineStatus(id, status);
        if (result != 1) {
            throw new BadRequestException(
                DataCoreUiCode.ErrorCode.BAD_REQUEST,
                "Change Pipeline Status Error"
            );
        }
    }

    @Transactional
    public void deletePipeline(Integer id) {
        // check nifi run or stop. if running then stop to nifi
        int result = pipelineMapper.deletePipeline(id);
        if (result != 1) {
            throw new BadRequestException(
                DataCoreUiCode.ErrorCode.BAD_REQUEST,
                "Delete Pipeline Error"
            );
        }
    }

    public Boolean isExists(Integer id) {
        return pipelineMapper.isExists(id);
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
