package io.dtonic.dhubingestmodule.common.thread;

import org.springframework.context.annotation.ComponentScan;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.dtonic.dhubingestmodule.common.code.CommandStatusCode;
import io.dtonic.dhubingestmodule.common.code.PipelineStatusCode;
import io.dtonic.dhubingestmodule.nifi.controller.NiFiController;
import io.dtonic.dhubingestmodule.pipeline.mapper.PipelineMapper;
import io.dtonic.dhubingestmodule.pipeline.service.PipelineSVC;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineVO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ComponentScan(basePackages={"io.dtonic.dhubingestmodule.pipeline.vo.PipelineVO"})
public class MultiThread implements Runnable{
        
    private PipelineVO pipelineVO;
    private Integer commandId;

    private NiFiController niFiController;
    
    private PipelineMapper pipelineMapper;

    private PipelineSVC pipelineSVC;

    public MultiThread(PipelineVO pipelineVO, Integer commandId) {
        this.pipelineVO = pipelineVO;
        this.commandId = commandId;
    }
    
    @Override
    public void run() {
        Integer pipelineId = pipelineVO.getId();

        /* 의존성 주입 */
        niFiController = ApplicationContextProvider.getBean(NiFiController.class);
        pipelineSVC = ApplicationContextProvider.getBean(PipelineSVC.class);
        pipelineMapper = ApplicationContextProvider.getBean(PipelineMapper.class);

        if (niFiController.deletePipeline(pipelineVO.getProcessorGroupId(), commandId)) {
            int result = pipelineMapper.deletePipeline(pipelineId, PipelineStatusCode.PIPELINE_STATUS_DELETED.getCode());
            if (result == 1) {
                pipelineSVC.updateCommand(commandId,CommandStatusCode.COMMAND_STATUS_SUCCEED.getCode());
                try {
                    pipelineSVC.changePipelineStatus(pipelineId, PipelineStatusCode.PIPELINE_STATUS_DELETED.getCode());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                log.error("Delete Pipeline DB error");
                pipelineSVC.updateCommand(commandId,CommandStatusCode.COMMAND_STATUS_FAILED.getCode());
                try {
                    pipelineSVC.changePipelineStatus(pipelineId, PipelineStatusCode.PIPELINE_STATUS_FAILED.getCode());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            try {
                pipelineSVC.updateCommand(commandId,CommandStatusCode.COMMAND_STATUS_FAILED.getCode());
                pipelineSVC.changePipelineStatus(pipelineId, PipelineStatusCode.PIPELINE_STATUS_FAILED.getCode());
            } catch (Exception e) {
                e.printStackTrace();
            }
            log.error("Delete Pipeline Nifi error");
        }
    }
}