package io.dtonic.dhubingestmodule.pipeline.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dtonic.dhubingestmodule.nifi.vo.AdaptorVO;
import io.dtonic.dhubingestmodule.nifi.vo.NiFiComponentVO;
import io.dtonic.dhubingestmodule.nifi.vo.PropertyVO;
import io.dtonic.dhubingestmodule.pipeline.mapper.PipelineMapper;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineCreateVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineListResponseVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineResponseVO;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils.Null;
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
    public List<PipelineListResponseVO> getPipelineList(
        String searchObject,
        String searchValue,
        String status
    ) {
        List<PipelineListResponseVO> pipelineListVOs = pipelineMapper.getPipelineList(
            searchObject,
            searchValue,
            status
        );
        for (int i = 0; i < pipelineListVOs.size(); i++) {
            String temp_status = "Running"; // Nifi API Response stats 값 가져와서 (현재 임시값)
            String cur_status = pipelineListVOs.get(i).getStatus(); // 현재 DB status값
            if (cur_status.equals("Starting") || cur_status.equals("Stopping")) { // DB 상태 Starting or Stopping 경우 상태변화
                // 일어남
                if (cur_status.equals("Starting") && temp_status.equals("Running")) pipelineListVOs
                    .get(i)
                    .setStatus("Run"); else if ( // DB상태 Run // DB상태 Starting Nifi 상태 Running =>
                    cur_status.equals("Stopping") && temp_status.equals("Stop")
                ) pipelineListVOs.get(i).setStatus("Stopped"); // DB상태 Stopped // DB상태 Stopping Nifi 상태 Stop =>
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

    public PipelineResponseVO getPipelineVOById(Integer id) {
        return pipelineMapper.getPipeline(id);
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
