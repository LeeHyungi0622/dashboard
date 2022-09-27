package io.dtonic.dhubingestmodule.pipeline.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import graphql.com.google.common.collect.PeekingIterator;
import io.dtonic.dhubingestmodule.common.code.DataCoreUiCode;
import io.dtonic.dhubingestmodule.common.code.PipelineStatusCode;
import io.dtonic.dhubingestmodule.common.exception.BadRequestException;
import io.dtonic.dhubingestmodule.common.exception.ResourceNotFoundException;
import io.dtonic.dhubingestmodule.pipeline.service.PipelineSVC;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineCreateVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineListResponseVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineListRetrieveVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineVO;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.json.JSONObject;
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

    @GetMapping("/pipeline/complete/list") // PipeLine List 조회
    public List<PipelineListResponseVO> getPipelineList(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestHeader(HttpHeaders.ACCEPT) String accept,
        PipelineListRetrieveVO pipelineListRetrieveVO
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
    @PostMapping("/pipeline/complete") // PipeLine 등록
    public void createPipeline(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestHeader(HttpHeaders.ACCEPT) String accept,
        @RequestBody PipelineCreateVO pipelineVO
    ) {
        // 1. validation check
        pipelineSVC.createPipeline(
            pipelineVO.getCreator(),
            pipelineVO.getName(),
            pipelineVO.getDetail(),
            pipelineVO.getStatus(),
            pipelineVO.getData_set(),
            pipelineVO.getCollector(),
            pipelineVO.getFilter(),
            pipelineVO.getConverter(),
            pipelineVO.getCreatedAt(),
            pipelineVO.getModifiedAt()
        );
        //        pipelineSVC.deletePipeline(pipelineVO.getId());
        response.setStatus(HttpStatus.CREATED.value());
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
    )
        throws JsonMappingException, JsonProcessingException {
        // JSONObject json = pipelineSVC.getPipelineVOById(id);
        // response.set
        // return json;
        return pipelineSVC.getPipelineVOById(id);
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
    public void stopPipeline(
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
        }
        if (
            status.equals(PipelineStatusCode.PIPELINE_STATUS_STARTING.getCode()) ||
            status.equals(PipelineStatusCode.PIPELINE_STATUS_RUN.getCode()) ||
            status.equals(PipelineStatusCode.PIPELINE_STATUS_STOPPED.getCode()) ||
            status.equals(PipelineStatusCode.PIPELINE_STATUS_STOPPING.getCode())
        ) pipelineSVC.changePipelineStatus(id, status); // stop pipeline

        response.setStatus(HttpStatus.OK.value());
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

    /**
     * Update Pipeline
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @param accept   request accept header
     * @param id       retrieve Pipeline id
     * @return
     */

    @PutMapping("/pipeline/complete/{id}") // PipeLine 수정
    public PipelineVO updatePipeline(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestBody String requestBody,
        @PathVariable Integer id
    )
        throws JsonMappingException, JsonProcessingException {
        JSONObject jsonObject = new JSONObject(requestBody);
        pipelineSVC.updatePipeline(jsonObject);
        // response.setStatus(HttpStatus.OK.value());

        return pipelineSVC.getPipelineVOById(id);
    }
}
