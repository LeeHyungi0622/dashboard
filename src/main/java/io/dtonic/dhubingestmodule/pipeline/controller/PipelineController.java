package io.dtonic.dhubingestmodule.pipeline.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.dtonic.dhubingestmodule.common.code.PipelineStatusCode;
import io.dtonic.dhubingestmodule.pipeline.service.PipelineCommonSVC;
import io.dtonic.dhubingestmodule.pipeline.service.PipelineSVC;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineListResponseVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineVO;
import io.dtonic.dhubingestmodule.security.service.IngestManagerSecuritySVC;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ForkJoinPool;

import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

@RestController
@Slf4j
public class PipelineController {

    @Autowired
    private PipelineSVC pipelineSVC;

    @Autowired
    private PipelineCommonSVC pipelineCommonSVC;

    @Autowired
    private IngestManagerSecuritySVC ingestManagerSVC;

    @GetMapping("/pipelines/completed") // PipeLine List 조회
    public ResponseEntity<List<PipelineListResponseVO>> getPipelineList(
    ) {
        return ResponseEntity.ok().body(pipelineSVC.getPipelineList());
    }

    /**
     * @param request    HttpServletRequest
     * @param id         to delete temporary Pipeline
     * @param pipelineVO create pipeline object
     * @return
     */
    @PostMapping("/pipelines/completed/{id}") // PipeLine 생성시 "등록완료"
    public ResponseEntity<Object> createPipeline(
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
     * @param accept   request accept header
     * @param id       retrieve Pipeline id
     * @return
     * @throws JsonProcessingException
     * @throws JsonMappingException
     */
    @PutMapping("/pipelines/completed/{id}") 
    public ResponseEntity<Object> updatePipeline(
        HttpServletRequest request,
        @RequestBody PipelineVO pipelineVO,
        @PathVariable Integer id
    ) throws JsonMappingException, JsonProcessingException {
        /* Get User Id */
        String userId = ingestManagerSVC.getUserId(request).getBody();
        /* Chech Exist Pipeline */
        if (Boolean.TRUE.equals(pipelineSVC.isExists(id))) {
            /* Update Pipeline */
            pipelineSVC.updatePipeline(id, pipelineVO, userId);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Pipeline is Not Exist");
        }
    }

    /**
     * [Retrieve Pipeline Detail by id]
     * if pipeline is not exist, return 400
     * else return pipeline detail
     * @param id Pipeline id
     * @return ResponseEntity<Object> pipeline detail
     * 
     * @since 2023. 8. 16
     * @version 1.2.0
     * @auther Justin
     */
    @GetMapping("/pipelines/completed/{id}")
    public ResponseEntity<Object> getPipelineById(
        @PathVariable Integer id
    ) {
        PipelineVO res = pipelineSVC.getPipelineVOById(id);
        if (res == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Pipeline is not Exist");
        } else {
            return ResponseEntity.ok().body(res);
        }
    }
    @GetMapping("/pipelines/collectors") // 데이터수집기 리스트 리턴
    public ResponseEntity<List<String>> getPipelinecollectors(
    ) {
        return ResponseEntity.ok().body(pipelineCommonSVC.getDataCollector());
    }

    @GetMapping("/pipelines/completed/properties") // 파이프라인 수정시 Collector,filter, DataSet 선택시 호출
    public ResponseEntity<Object> getPipelineProperties(
        @RequestParam(name = "page") String page,
        @RequestParam(name = "id") Integer id,
        @RequestParam(name = "adaptorName") String adaptorName,
        @RequestParam(name = "datasetid", required = false) String datasetid
    ) {
        PipelineVO res = pipelineSVC.getPipelineProperties(id, page, adaptorName, datasetid);
        if (res == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Fail to get Pipeline Properties");
        }
        return ResponseEntity.ok().body(res);
    }

    /**
     * Pipeline Status Update
     *
     * @param id       retrieve Pipeline id
     * @param status   PipelineStatusCode
     * @return ResponseEntity
     * 
     * @since 2023. 8. 21
     * @version 1.2.0
     * @auther Justin
     */
    @PutMapping("/pipelines/run-status/{id}")
    public ResponseEntity<Object> updatePipelineStatus(
        @PathVariable Integer id,
        @RequestParam(value = "status") String status
    ) {
        /* Validation Status Check */
        if (PipelineStatusCode.parseCode(status) == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Status name is invalid");
        }
        /* validation check */
        if (!Boolean.TRUE.equals(pipelineSVC.isExists(id))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Pipeline is not Exist");
        }
        /* Execute service */
        Boolean result = pipelineSVC.changePipelineStatus(id, status);
        if (result) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Fail To Update Pipeline Status");
        }
    }

    /**
     * Delete Pipeline
     *
     * @param request  HttpServletRequest
     * @param id       retrieve Pipeline id
     * @return Async ResponseEntity
     */
    @DeleteMapping("/pipelines/completed/{id}")
    public DeferredResult<ResponseEntity<Object>> deletePipeline(
        HttpServletRequest request,
        @PathVariable Integer id
    )  {
        DeferredResult<ResponseEntity<Object>> output = new DeferredResult<>();
        ForkJoinPool.commonPool().submit(() -> {
        try {
            /* Vaildation Pipeline Exist */
            if (Boolean.FALSE.equals(pipelineSVC.isExists(id))) {
                output.setResult(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Pipeline is not Exist"));
            } else {
                /* Execute Service */
                pipelineSVC.deletePipeline(id, ingestManagerSVC.getUserId(request).getBody());
                output.setResult(ResponseEntity.ok().build());
            }
        } catch (Exception e) {
            output.setErrorResult(e);
        }});

        return output;
    }

    

}
