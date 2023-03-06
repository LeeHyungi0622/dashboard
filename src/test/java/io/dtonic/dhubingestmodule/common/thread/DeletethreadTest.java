package io.dtonic.dhubingestmodule.common.thread;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import io.dtonic.dhubingestmodule.common.code.PipelineStatusCode;
import io.dtonic.dhubingestmodule.nifi.controller.NiFiController;
import io.dtonic.dhubingestmodule.pipeline.mapper.PipelineMapper;
import io.dtonic.dhubingestmodule.pipeline.service.PipelineSVC;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineVO;
import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Test;

@Slf4j
@SpringBootTest
@ActiveProfiles("dtonic")
public class DeletethreadTest {
    /*private PipelineVO pipelineVO;
    
    private Integer id;

    @Autowired
    private NiFiController niFiController;

    @Autowired
    private PipelineMapper pipelineMapper;

    @Autowired
    private PipelineSVC pipelineSVC;

    public DeletethreadTest(PipelineVO pipelineVO, Integer id) {
        this.pipelineVO = pipelineVO;
        this.id = id;
        
    }

    @Override
    @Test
    public void run() {
        log.info("== Thread ==");
        if (niFiController.deletePipeline(pipelineVO.getProcessorGroupId())) {
            log.info("== Thread Finish Delete Pipeline 1 (NIFI) ==");
            int result = pipelineMapper.deletePipeline(id);
            log.info("== Thread Finish Delete Pipeline 2 (DB) ==");
            if (result != 1) {
                 pipelineSVC.changePipelineStatus(id, PipelineStatusCode.PIPELINE_STATUS_FAILED.getCode());
                 log.info("== Thread Finish Delete Pipeline 3 (DB) Update \"Deleted\" ==");
            }
        } else {
             pipelineSVC.changePipelineStatus(id, PipelineStatusCode.PIPELINE_STATUS_FAILED.getCode());
             log.info("== Thread Fail Delete Pipeline 0 (DB) ==");
        }
    } */
}
