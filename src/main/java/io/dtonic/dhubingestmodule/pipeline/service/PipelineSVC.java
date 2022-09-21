package io.dtonic.dhubingestmodule.pipeline.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import io.dtonic.dhubingestmodule.nifi.vo.AdaptorVO;
import io.dtonic.dhubingestmodule.nifi.vo.NiFiComponentVO;
import io.dtonic.dhubingestmodule.nifi.vo.PropertyVO;
import io.dtonic.dhubingestmodule.pipeline.mapper.PipelineMapper;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineCreateVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineResponseVO;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
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

    //private final NiFoControlSVC nificontrolSVC;

    // public void createPipeline(
    //     String creator,
    //     String name,
    //     String detail,
    //     String status,
    //     String data_set,
    //     String collector,
    //     String filter,
    //     String converter,
    //     Date createdAt,
    //     Date modifiedAt
    // ) {
    //     pipelineMapper.createPipeline(
    //         creator,
    //         name,
    //         detail,
    //         status,
    //         data_set,
    //         collector,
    //         filter,
    //         converter,
    //         createdAt,
    //         modifiedAt
    //     );
    // }

    public Boolean isExists(Integer id) {
        return pipelineMapper.isExists(id);
    }

    public PipelineResponseVO getPipelineVOById(Integer id) {
        PipelineResponseVO pipelineVO = pipelineMapper.getPipeline(id);
        return pipelineVO;
    }

    @Transactional
    public void startPipeline(Integer id) {
        //start nifi process group
        //nificontrolSVC

        //change DB
        pipelineMapper.startPipeline(id);
    }

    @Transactional
    public void stopPipeline(Integer id) {
        //change DB
        pipelineMapper.stopPipeline(id);
    }

    @Transactional
    public void deletePipeline(Integer id) {
        //check nifi run or stop. if running then stop to nifi
        //nificontrolSVC

        //delete nifi
        //nificontrolSVC

        //change DB
        pipelineMapper.deletePipeline(id);
    }
}
