package io.dtonic.dhubingestmodule.pipeline.controller;

import graphql.com.google.common.collect.PeekingIterator;
import io.dtonic.dhubingestmodule.common.code.DataCoreUiCode;
import io.dtonic.dhubingestmodule.common.exception.BadRequestException;
import io.dtonic.dhubingestmodule.common.exception.ResourceNotFoundException;
import io.dtonic.dhubingestmodule.pipeline.service.PipelineSVC;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineCreateVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineResponseVO;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class PipelineController {

    @Autowired
    private PipelineSVC pipelineSVC;

    /**
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param accept request accept header
     * @param pipelineVO create pipeline object
     * @return
     */
    // @Transactional
    // @PostMapping("/pipeline/{id}")
    // public @ResponseBody void createPipeline(
    //     HttpServletRequest request,
    //     HttpServletResponse response,
    //     @RequestHeader(HttpHeaders.ACCEPT) String accept,
    //     @RequestBody PipelineCreateVO pipelineVO
    // ) {
    //     //1. validation check
    //     pipelineSVC.createPipeline(
    //         pipelineVO.getCreator(),
    //         pipelineVO.getName(),
    //         pipelineVO.getDetail(),
    //         pipelineVO.getStatus(),
    //         pipelineVO.getData_set(),
    //         pipelineVO.getCollector(),
    //         pipelineVO.getCollector(),
    //         pipelineVO.getConverter(),
    //         pipelineVO.getCreatedAt(),
    //         pipelineVO.getModifiedAt()
    //     );
    //     response.setStatus(HttpStatus.CREATED.value());
    // }

    /**
     * Retrieve Pipeline
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param accept request accept header
     * @param id retrieve Pipeline id
     * @return Pipeline object
     */
    @GetMapping("/pipeline/{id}")
    public @ResponseBody PipelineResponseVO getPipelineById(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestHeader(HttpHeaders.ACCEPT) String accept,
        @PathVariable Integer id
    ) {
        //validation check
        PipelineResponseVO pipelineVO = pipelineSVC.getPipelineVOById(id);
        return pipelineVO;
    }

    /**
     * Start Pipeline
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param accept request accept header
     * @param id retrieve Pipeline id
     * @return
     */
    @PutMapping("/pipeline/{id}/start")
    public @ResponseBody void startPipeline(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestHeader(HttpHeaders.ACCEPT) String accept,
        @PathVariable Integer id
    ) {
        //validation check
        PipelineResponseVO pipelineVO = pipelineSVC.getPipelineVOById(id);

        if (!pipelineSVC.isExists(id)) {
            throw new BadRequestException(
                DataCoreUiCode.ErrorCode.NOT_EXIST_ID,
                "Pipeline is not Exist"
            );
        }

        //start pipeline
        pipelineSVC.startPipeline(id);

        response.setStatus(HttpStatus.OK.value());
    }

    /**
     * Stop Pipeline
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param accept request accept header
     * @param id retrieve Pipeline id
     * @return
     */
    @PutMapping("/pipeline/{id}/stop")
    public @ResponseBody void stopPipeline(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestHeader(HttpHeaders.ACCEPT) String accept,
        @PathVariable Integer id
    ) {
        //validation check
        PipelineResponseVO pipelineVO = pipelineSVC.getPipelineVOById(id);

        if (!pipelineSVC.isExists(id)) {
            throw new BadRequestException(
                DataCoreUiCode.ErrorCode.NOT_EXIST_ID,
                "Pipeline is not Exist"
            );
        }
        //stop pipeline
        pipelineSVC.stopPipeline(id);
        response.setStatus(HttpStatus.OK.value());
    }

    /**
     * Delete Pipeline
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param accept request accept header
     * @param id retrieve Pipeline id
     * @return
     */
    @DeleteMapping("/pipeline/{id}")
    public @ResponseBody void deletePipeline(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestHeader(HttpHeaders.ACCEPT) String accept,
        @PathVariable Integer id
    ) {
        //validation check
        PipelineResponseVO pipelineVO = pipelineSVC.getPipelineVOById(id);

        if (!pipelineSVC.isExists(id)) {
            throw new BadRequestException(
                DataCoreUiCode.ErrorCode.NOT_EXIST_ID,
                "Pipeline is not Exist"
            );
        }
        //delete pipeline
        pipelineSVC.deletePipeline(id);
        response.setStatus(HttpStatus.OK.value());
    }
}
