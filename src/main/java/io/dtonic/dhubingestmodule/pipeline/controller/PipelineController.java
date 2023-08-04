package io.dtonic.dhubingestmodule.pipeline.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.dtonic.dhubingestmodule.common.code.CommandStatusCode;
import io.dtonic.dhubingestmodule.common.code.PipelineStatusCode;
import io.dtonic.dhubingestmodule.common.code.TaskStatusCode;
import io.dtonic.dhubingestmodule.history.vo.CommandVO;
import io.dtonic.dhubingestmodule.history.vo.TaskVO;
import io.dtonic.dhubingestmodule.nifi.service.NiFiRestSVC;
import io.dtonic.dhubingestmodule.pipeline.service.PipelineDraftSVC;
import io.dtonic.dhubingestmodule.pipeline.service.PipelineSVC;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineListResponseVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineVO;
import io.dtonic.dhubingestmodule.security.service.IngestManagerSecuritySVC;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class PipelineController {

    @Autowired
    private PipelineSVC pipelineSVC;

    @Autowired
    private PipelineDraftSVC pipelineDraftSVC;

    @Autowired
    private IngestManagerSecuritySVC ingestManagerSVC;

    @GetMapping("/pipelines/completed") // PipeLine List 조회
    public ResponseEntity<List<PipelineListResponseVO>> getPipelineList(
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        return ResponseEntity.ok().body(pipelineSVC.getPipelineList());
    }

    /**
     * @param request    HttpServletRequest
     * @param response   HttpServletResponse
     * @param accept     request accept header
     * @param pipelineVO create pipeline object
     * @param id         to delete temporary Pipeline
     * @return
     */
    @PostMapping("/pipelines/completed/{id}") // PipeLine 생성시 "등록완료"
    public ResponseEntity createPipeline(
        HttpServletRequest request,
        HttpServletResponse response,
        @PathVariable Integer id,
        @RequestBody PipelineVO pipelineVO
    ) {
        try {
            pipelineSVC.createPipeline(id, pipelineVO);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Create Pipeline error {}", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Create Pipeline failed");
        } 
        
    }

    /**
     * Update Pipeline
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @param accept   request accept header
     * @param id       retrieve Pipeline id
     * @return
     * @throws JsonProcessingException
     * @throws JsonMappingException
     */
    @PutMapping("/pipelines/completed/{id}") // 등록된 PipeLine에 대한 "수정 완료" 확정
    public ResponseEntity updatePipeline(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestBody PipelineVO pipelineVO,
        @PathVariable Integer id
    ) throws JsonMappingException, JsonProcessingException {
        // validation check
        ResponseEntity principal = ingestManagerSVC.getUserId(request);
        String userId = principal.getBody().toString();

        if (Boolean.TRUE.equals(pipelineSVC.isExists(id))) {
            pipelineSVC.updatePipeline(id, pipelineVO, userId);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Pipeline is not Exist ");
        }
    }

    /**
     * Retrieve Pipeline
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @param accept   request accept header
     * @param id       retrieve Pipeline id
     * @return Pipeline object
     * @throws JsonProcessingException
     * @throws JsonMappingException
     */
    @GetMapping("/pipelines/completed/{id}") // PipeLine 상세 조회
    public ResponseEntity getPipelineById(
        HttpServletRequest request,
        HttpServletResponse response,
        @PathVariable Integer id
    ) {
        return pipelineSVC.getPipelineVOById(id);
    }
    @GetMapping("/pipelines/collectors") // 데이터수집기 리스트 리턴
    public List<String> getPipelinecollectors(
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        return pipelineDraftSVC.getDataCollector();
    }
    @GetMapping("/pipelines/completed/properties") // 파이프라인 수정시 Collector,filter, DataSet 선택시 호출
    public ResponseEntity<T> getPipelineProperties(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestParam(name = "page") String page,
        @RequestParam(name = "id") Integer id,
        @RequestParam(name = "adaptorName") String adaptorName,
        @RequestParam(name = "datasetid", required = false) String datasetid
    ) {
        return pipelineSVC.getPipelineProperties(id, page, adaptorName, datasetid);
    }

    /**
     * Stop Pipeline
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @param accept   request accept header
     * @param id       retrieve Pipeline id
     * @return
     * @throws JsonProcessingException
     * @throws JsonMappingException
     */
    @PutMapping("/pipelines/run-status/{id}") // PipeLine status 업데이트
    public ResponseEntity UpdatePipelineStatus(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestHeader(HttpHeaders.ACCEPT) String accept,
        @PathVariable Integer id,
        @RequestParam(value = "status") String status
    ) {
        ResponseEntity principal = ingestManagerSVC.getUserId(request);
        String userId = principal.getBody().toString();
        Integer commandId = null;
        Integer taskId = null;
        CommandVO commandVO = new CommandVO();
        TaskVO taskVO = new TaskVO();

        if(status.equals(PipelineStatusCode.PIPELINE_STATUS_STARTING.getCode())){
            commandVO.setCommand(PipelineStatusCode.PIPELINE_STATUS_RUN.getCode());
            commandVO.setStatus(CommandStatusCode.COMMAND_STATUS_RUNNING.getCode());
            taskVO.setTaskName(TaskStatusCode.TASK_TASK_NAME_RUN.getCode());
        }else if(status.equals(PipelineStatusCode.PIPELINE_STATUS_STOPPING.getCode())){
            commandVO.setCommand(PipelineStatusCode.PIPELINE_STATUS_STOPPED.getCode());
            commandVO.setStatus(CommandStatusCode.COMMAND_STATUS_STOPPING.getCode());
            taskVO.setTaskName(TaskStatusCode.TASK_TASK_NAME_STOP.getCode());
        }
        commandVO.setUserId(userId);
        commandVO.setPipelineId(id);
        taskVO.setStatus(TaskStatusCode.TASK_STATUS_WORKING.getCode());
        
        /* validation check */
        if (Boolean.TRUE.equals(pipelineSVC.isExists(id))) {
            if (
                status.equals(PipelineStatusCode.PIPELINE_STATUS_STARTING.getCode()) ||
                status.equals(PipelineStatusCode.PIPELINE_STATUS_RUN.getCode()) ||
                status.equals(PipelineStatusCode.PIPELINE_STATUS_STOPPED.getCode()) ||
                status.equals(PipelineStatusCode.PIPELINE_STATUS_STOPPING.getCode())
            ) {
                commandId = pipelineSVC.createCommand(commandVO);
                taskVO.setCommandId(commandId);
                taskId = niFiRestSVC.createTask(taskVO);
                /* service 수행 */
                ResponseEntity result = pipelineSVC.changePipelineStatus(id, status);
                if (result.getStatusCode() == HttpStatus.valueOf(200)){
                    niFiRestSVC.updateTask(taskId, TaskStatusCode.TASK_STATUS_FINISH.getCode());
                    pipelineSVC.updateCommand(commandId, CommandStatusCode.COMMAND_STATUS_SUCCEED.getCode());
                }else{
                    niFiRestSVC.updateTask(taskId, TaskStatusCode.TASK_STATUS_FAILED.getCode());
                    pipelineSVC.updateCommand(commandId, CommandStatusCode.COMMAND_STATUS_FAILED.getCode());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Run Failed ");
                }
            } else {   
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Status name is invalid");
            }
            return ResponseEntity.ok().build();
            
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Pipeline is not Exist");
        }
    }

    /**
     * Delete Pipeline
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @param accept   request accept header
     * @param id       retrieve Pipeline id
     * @return
     * @throws JsonProcessingException
     * @throws JsonMappingException
     */
    @DeleteMapping("/pipelines/completed/{id}") // PipeLine 삭제
    public ResponseEntity deletePipeline(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestHeader(HttpHeaders.ACCEPT) String accept,
        @PathVariable Integer id
    )  {
        if (Boolean.FALSE.equals(pipelineSVC.isExists(id))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Pipeline is not Exist");
        }
        // delete pipeline
        ResponseEntity principal = ingestManagerSVC.getUserId(request);
        String userId = null;
        try {
            userId = principal.getBody().toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        pipelineSVC.deletePipeline(id, userId);
        return ResponseEntity.ok().build();
    }

    
    @GetMapping("/redirectNiFiUrl")
    public String redirectNiFiUrl(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

                return NiFiURL;
        
    }
}
