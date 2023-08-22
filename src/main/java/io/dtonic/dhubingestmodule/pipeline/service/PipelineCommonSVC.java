package io.dtonic.dhubingestmodule.pipeline.service;


import java.util.List;


import org.springframework.beans.factory.annotation.Autowired;

import io.dtonic.dhubingestmodule.pipeline.mapper.PipelineMapper;

public class PipelineCommonSVC {

    @Autowired
    private PipelineMapper pipelineMapper;

    public List<String> getDataCollector() {
        return pipelineMapper.getDataCollector();
    }

}
