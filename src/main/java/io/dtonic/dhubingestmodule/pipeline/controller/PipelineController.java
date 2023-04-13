package io.dtonic.dhubingestmodule.pipeline.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.dtonic.dhubingestmodule.common.code.DataCoreUiCode;
import io.dtonic.dhubingestmodule.common.code.PipelineStatusCode;
import io.dtonic.dhubingestmodule.common.exception.BadRequestException;
import io.dtonic.dhubingestmodule.pipeline.service.PipelineDraftSVC;
import io.dtonic.dhubingestmodule.pipeline.service.PipelineSVC;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineListResponseVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineVO;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
// @RequestMapping(path = "pipelines")
public class PipelineController<T> {

    @Autowired
    private PipelineSVC pipelineSVC;

    @Autowired
    private PipelineDraftSVC pipelineDraftSVC;

    @GetMapping("/pipelines/completed") // PipeLine List 조회
    public ResponseEntity<T> getPipelineList(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestHeader(HttpHeaders.ACCEPT) String accept
    ) {
        return pipelineSVC.getPipelineList();
    }

    /**
     * @param request    HttpServletRequest
     * @param response   HttpServletResponse
     * @param accept     request accept header
     * @param pipelineVO create pipeline object
     * @param id         to delete temporary Pipeline
     * @return
     */
    @Transactional
    @PostMapping("/pipelines/completed/{id}") // PipeLine 생성시 "등록완료"
    public ResponseEntity createPipeline(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestHeader(HttpHeaders.ACCEPT) String accept,
        @PathVariable Integer id, //삭제할 임시파이프라인 id
        @RequestBody PipelineVO pipelineVO
    ) {
        pipelineSVC.createPipeline(id, pipelineVO);
        return ResponseEntity.ok().build();
    }

    /**
     * Update Pipeline
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @param accept   request accept header
     * @param id       retrieve Pipeline id
     * @return
     */
    @PutMapping("/pipelines/completed/{id}") // 등록된 PipeLine에 대한 "수정 완료" 확정
    public ResponseEntity updatePipeline(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestBody PipelineVO pipelineVO,
        @PathVariable Integer id
    ) {
        // validation check
        if (Boolean.FALSE.equals(pipelineSVC.isExists(id))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Pipeline is not Exist ");
        } else {
            pipelineSVC.updatePipeline(id, pipelineVO);
            return ResponseEntity.ok().build();
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
    public ResponseEntity<T> getPipelineById(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestHeader(HttpHeaders.ACCEPT) String accept,
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
     */
    @PutMapping("/pipelines/run-status/{id}") // PipeLine status 업데이트
    public ResponseEntity UpdatePipelineStatus(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestHeader(HttpHeaders.ACCEPT) String accept,
        @PathVariable Integer id,
        @RequestParam(value = "status") String status
    ) {
        // validation check
        if (Boolean.FALSE.equals(pipelineSVC.isExists(id))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Pipeline is not Exist");
            // throw new BadRequestException(
            //     DataCoreUiCode.ErrorCode.NOT_EXIST_ID,
            //     "Pipeline is not Exist"
            // );
        } else {
            if (
                status.equals(PipelineStatusCode.PIPELINE_STATUS_STARTING.getCode()) ||
                status.equals(PipelineStatusCode.PIPELINE_STATUS_RUN.getCode()) ||
                status.equals(PipelineStatusCode.PIPELINE_STATUS_STOPPED.getCode()) ||
                status.equals(PipelineStatusCode.PIPELINE_STATUS_STOPPING.getCode())
            ) {
                pipelineSVC.changePipelineStatus(id, status);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Status name is invalid");
                // throw new BadRequestException(
                //     DataCoreUiCode.ErrorCode.BAD_REQUEST,
                //     "Status name is invalid"
                // );
            }

            return ResponseEntity.ok().build();
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
     */
    @DeleteMapping("/pipelines/completed/{id}") // PipeLine 삭제
    public ResponseEntity deletePipeline(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestHeader(HttpHeaders.ACCEPT) String accept,
        @PathVariable Integer id
    ) {
        if (Boolean.FALSE.equals(pipelineSVC.isExists(id))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Pipeline is not Exist");
            // throw new BadRequestException(
            //     DataCoreUiCode.ErrorCode.NOT_EXIST_ID,
            //     "Pipeline is not Exist"
            // );
        }
        // delete pipeline
        pipelineSVC.deletePipeline(id);
        return ResponseEntity.ok().build();
    }
}
