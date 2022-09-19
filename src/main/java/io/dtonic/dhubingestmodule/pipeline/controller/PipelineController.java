package io.dtonic.dhubingestmodule.pipeline.controller;

import io.dtonic.dhubingestmodule.common.code.DataCoreUiCode;
import io.dtonic.dhubingestmodule.pipeline.service.PipelineSVC;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineListRetrieveVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineVO;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.transaction.annotation.Transactional;
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
     * Retrieve Pipeline
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param accept request accept header
     * @param id retrieve Pipeline id
     * @return Pipeline object
     */

    @GetMapping("/pipeline/drafts/list")
    public @ResponseBody PipelineVO getPipelineDraftsList(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestHeader(HttpHeaders.ACCEPT) String accept,
        PipelineListRetrieveVO pipelineListRetrieveVO
    ) {
        PipelineVO pipelineVO = pipelineSVC.getPipelineDraftsList();
        return pipelineVO;
    }

    @GetMapping("/pipeline/drafts/{id}")
    public @ResponseBody PipelineVO getPipelineDraftsById(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestHeader(HttpHeaders.ACCEPT) String accept,
        @PathVariable String id
    ) {
        PipelineVO pipelineVO = pipelineSVC.getPipelineDraftsVOById(id);
        return pipelineVO;
    }

    @Transactional
    @PostMapping("/pipeline/drafts/create")
    public void createPipelineDrafts(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestBody List<String> selectedMetaSets
    )
        throws Exception {}

    @Transactional
    @PutMapping("/pipeline/drafts/update")
    public @ResponseBody void updatePipelineDrafts(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestBody List<String> metaSetUpsertVO
    )
        throws Exception {}
}
