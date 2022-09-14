package io.dtonic.dhubingestmodule.pipeline.controller;

import io.dtonic.dhubingestmodule.common.code.DataCoreUiCode;
import io.dtonic.dhubingestmodule.common.exception.ResourceNotFoundException;
import io.dtonic.dhubingestmodule.pipeline.service.PipelineSVC;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineVO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class PipelineController {

    @Autowired
    private PipelineSVC pipelineSVC;

    /**
     * Retrieve Pipeline
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param accept request accept header
     * @param id retrieve Pipeline id
     * @return Pipeline object
     */
    @GetMapping("/pipeline/{id}")
    public @ResponseBody PipelineVO getPipelineById(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestHeader(HttpHeaders.ACCEPT) String accept,
        @PathVariable String id
    ) {
        //validation check
        PipelineVO pipelineVO = pipelineSVC.getPipelineVOById(id);

        if (pipelineVO == null) {
            throw new ResourceNotFoundException(
                DataCoreUiCode.ErrorCode.NOT_EXIST_ID,
                "There are no pipelines with that ID."
            );
        }
        return pipelineVO;
    }
}
