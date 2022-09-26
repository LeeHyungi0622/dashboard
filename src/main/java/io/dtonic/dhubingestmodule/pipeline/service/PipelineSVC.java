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

    public void createPipeline(
        String creator,
        String name,
        String detail,
        String status,
        String data_set,
        String collector,
        String filter,
        String converter,
        Date createdAt,
        Date modifiedAt
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
            converter,
            createdAt,
            modifiedAt
        );
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
}
