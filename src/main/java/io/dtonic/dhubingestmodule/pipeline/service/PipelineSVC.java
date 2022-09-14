package io.dtonic.dhubingestmodule.pipeline.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dtonic.dhubingestmodule.pipeline.mapper.PipelineMapper;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PipelineSVC {

    @Autowired
    private PipelineMapper pipelineMapper;

    @Autowired
    private ObjectMapper objectMapper;

    public PipelineVO getPipelineVOById(String id) {
        PipelineVO pipelineVO = pipelineMapper.getPipeline(id);
        //objectMapper.writeValue(pipelineVO);
        return pipelineVO;
    }
}
