package io.dtonic.dhubingestmodule.pipeline.controller;

import io.dtonic.dhubingestmodule.common.code.DataCoreUiCode;
import io.dtonic.dhubingestmodule.common.exception.BadRequestException;
import io.dtonic.dhubingestmodule.pipeline.service.PipelineDraftSVC;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineDraftsListResponseVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineListRetrieveVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineVO;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class PipelineDraftController {

    @Autowired
    private PipelineDraftSVC pipelineSVC;

    /**
     * Retrieve Pipeline
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param accept request accept header
     * @param id retrieve Pipeline id
     * @return Pipeline object
     */

    @GetMapping("/pipeline/drafts/{id}")
    public PipelineVO getPipelineDrafts(
        HttpServletRequest request,
        HttpServletResponse response,
        @PathVariable Integer id
    ) {
        return pipelineSVC.getPipelineDrafts(id);
    }

    @GetMapping("/pipeline/drafts/list")
    public @ResponseBody List<PipelineDraftsListResponseVO> getPipelineDraftsList(
        HttpServletRequest request,
        HttpServletResponse response,
        PipelineListRetrieveVO pipelineListRetrieveVO
    ) {
        return pipelineSVC.getPipelineDraftsList(
            pipelineListRetrieveVO.getSearchObject(),
            pipelineListRetrieveVO.getSearchValue()
        );
    }

    @Transactional
    @PutMapping("/pipeline/drafts")
    public Integer upsertPipelineDrafts(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestBody String requestBody
    ) {
        JSONObject jsonObject = new JSONObject(requestBody);
        if (!jsonObject.has("id")) {
            pipelineSVC.createPipelineDrafts(jsonObject);
            response.setStatus(HttpStatus.OK.value());
            return pipelineSVC.getRecentPipelineDraftsId();
        } else {
            pipelineSVC.updatePipelineDrafts(jsonObject);
            response.setStatus(HttpStatus.OK.value());
            return jsonObject.getInt("id");
        }
    }

    // @Transactional
    // @PostMapping("/pipeline/drafts")
    // public void createPipelineDrafts(
    //     HttpServletRequest request,
    //     HttpServletResponse response,
    //     @RequestBody PipelineVO pipelineVO
    // ) {
    //     pipelineSVC.createPipelineDrafts(
    //         pipelineVO.getName(),
    //         pipelineVO.getCreator(),
    //         pipelineVO.getDetail()
    //     );
    //     response.setStatus(HttpStatus.OK.value());
    // }

    // @Transactional
    // @PutMapping("/pipeline/drafts")
    // public void updatePipelineDrafts(
    //     HttpServletRequest request,
    //     HttpServletResponse response,
    //     @RequestBody String requestBody
    // )
    //     throws Exception {
    //     pipelineSVC.updatePipelineDrafts(requestBody);
    //     response.setStatus(HttpStatus.OK.value());
    // }

    @Transactional
    @DeleteMapping("/pipeline/drafts/{id}")
    public void deletePipelineDrafts(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestHeader(HttpHeaders.ACCEPT) String accept,
        @PathVariable Integer id
    ) {
        //validation check
        if (!pipelineSVC.isExistsDrafts(id)) {
            throw new BadRequestException(
                DataCoreUiCode.ErrorCode.NOT_EXIST_ID,
                "PipelineDrafts is not Exist"
            );
        }

        //delete pipeline
        pipelineSVC.deletePipelineDrafts(id);
        response.setStatus(HttpStatus.OK.value());
    }
}
