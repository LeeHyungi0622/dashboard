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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
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

    @GetMapping("/pipeline/complete/list") // PipeLine List 조회
    public List<PipelineListResponseVO> getPipelineList(
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
    @PostMapping("/pipeline/complete/{id}") // PipeLine 생성시 "등록완료"
    public void createPipeline(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestHeader(HttpHeaders.ACCEPT) String accept,
        @PathVariable Integer id, //삭제할 임시파이프라인 id
        @RequestBody PipelineVO pipelineVO
    ) {
        pipelineSVC.createPipeline(id, pipelineVO);
        response.setStatus(HttpStatus.CREATED.value());
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

    @PutMapping("/pipeline/complete/{id}") // 등록된 PipeLine에 대한 "수정 완료" 확정
    public void updatePipeline(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestBody PipelineVO pipelineVO,
        @PathVariable Integer id
    )
        throws JsonMappingException, JsonProcessingException {
        // validation check
        if (Boolean.FALSE.equals(pipelineSVC.isExists(id))) {
            throw new BadRequestException(
                DataCoreUiCode.ErrorCode.NOT_EXIST_ID,
                "Pipeline is not Exist"
            );
        } else {
            pipelineSVC.updatePipeline(id, pipelineVO);
            response.setStatus(HttpStatus.OK.value());
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
    @GetMapping("/pipeline/complete/{id}") // PipeLine 상세 조회
    public PipelineVO getPipelineById(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestHeader(HttpHeaders.ACCEPT) String accept,
        @PathVariable Integer id
    ) {
        return pipelineSVC.getPipelineVOById(id);
    }

    @GetMapping("/pipeline/collectors") // 데이터수집기 리스트 리턴
    public List<String> getPipelinecollectors(
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        return pipelineDraftSVC.getDataCollector();
    }

    @GetMapping("/pipeline/complete/properties") // 파이프라인 수정시 Collector,filter, DataSet 선택시 호출
    public PipelineVO getPipelineProperties(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestParam(name = "page") String page,
        @RequestParam(name = "pipelineid") Integer pipelineid,
        @RequestParam(name = "adaptorName") String adaptorName,
        @RequestParam(name = "datasetid", required = false) String datasetid
    ) {
        PipelineVO pipelineVO = pipelineSVC.getPipelineProperties(
            pipelineid,
            page,
            adaptorName,
            datasetid
        );
        return pipelineVO;
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
    @PutMapping("/pipeline/run-status/{id}") // PipeLine status 업데이트
    public void UpdatePipelineStatus(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestHeader(HttpHeaders.ACCEPT) String accept,
        @PathVariable Integer id,
        @RequestParam(value = "status") String status
    ) {
        // validation check
        if (!pipelineSVC.isExists(id)) {
            throw new BadRequestException(
                DataCoreUiCode.ErrorCode.NOT_EXIST_ID,
                "Pipeline is not Exist"
            );
        } else {
            if (
                status.equals(PipelineStatusCode.PIPELINE_STATUS_STARTING.getCode()) ||
                status.equals(PipelineStatusCode.PIPELINE_STATUS_RUN.getCode()) ||
                status.equals(PipelineStatusCode.PIPELINE_STATUS_STOPPED.getCode()) ||
                status.equals(PipelineStatusCode.PIPELINE_STATUS_STOPPING.getCode())
            ) {
                pipelineSVC.changePipelineStatus(id, status);
            } else {
                throw new BadRequestException(
                    DataCoreUiCode.ErrorCode.BAD_REQUEST,
                    "Status name is invalid"
                );
            }

            response.setStatus(HttpStatus.OK.value());
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
    @DeleteMapping("/pipeline/complete/{id}") // PipeLine 삭제
    public void deletePipeline(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestHeader(HttpHeaders.ACCEPT) String accept,
        @PathVariable Integer id
    ) {
        if (!pipelineSVC.isExists(id)) {
            throw new BadRequestException(
                DataCoreUiCode.ErrorCode.NOT_EXIST_ID,
                "Pipeline is not Exist"
            );
        }
        // delete pipeline
        pipelineSVC.deletePipeline(id);
        response.setStatus(HttpStatus.OK.value());
    }
}
