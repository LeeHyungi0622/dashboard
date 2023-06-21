package io.dtonic.dhubingestmodule.common.thread;

import org.springframework.context.annotation.ComponentScan;
import io.dtonic.dhubingestmodule.common.code.CommandStatusCode;
import io.dtonic.dhubingestmodule.common.code.PipelineStatusCode;
import io.dtonic.dhubingestmodule.nifi.controller.NiFiController;
import io.dtonic.dhubingestmodule.pipeline.service.PipelineSVC;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineVO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ComponentScan(basePackages={"io.dtonic.dhubingestmodule.pipeline.vo.PipelineVO"})
public class MultiThread implements Runnable{
        
    private PipelineVO pipelineVO;
    private Integer commandId;

    public MultiThread(PipelineVO pipelineVO, Integer commandId) {
        this.pipelineVO = pipelineVO;
        this.commandId = commandId;
    }
    
    @Override
    public void run() {
        Integer pipelineId = pipelineVO.getId();

        /* 의존성 주입 */
        NiFiController niFiController = ApplicationContextProvider.getBean(NiFiController.class);
        PipelineSVC pipelineSVC = ApplicationContextProvider.getBean(PipelineSVC.class);

        if (niFiController.deletePipeline(pipelineVO.getProcessorGroupId(), commandId)) {
            pipelineSVC.updateCommand(commandId,CommandStatusCode.COMMAND_STATUS_SUCCEED.getCode());
            try {
                pipelineSVC.changePipelineStatus(pipelineId, PipelineStatusCode.PIPELINE_STATUS_DELETED.getCode());
            } catch (Exception e) {
                e.printStackTrace();
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