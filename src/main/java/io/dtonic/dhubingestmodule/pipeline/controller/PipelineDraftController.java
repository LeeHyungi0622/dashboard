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
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PipelineDraftController {

    @Autowired
    private PipelineDraftSVC pipelineDraftSVC;

    @GetMapping("/pipeline/drafts/create") // 파이프라인 생성 첫 시작 API, (front에서 빈 Pipeline VO가 필요)
    public PipelineVO createPipelineDrafts(
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        PipelineVO pipelineVO = new PipelineVO();
        return pipelineVO;
    }

    /**
     * Retrieve Pipeline
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param accept request accept header
     * @param id retrieve Pipeline id
     * @return Pipeline object
     */
    @GetMapping("/pipeline/drafts/{id}") // 임시저장 상세 조회
    public PipelineVO getPipelineDrafts(
        HttpServletRequest request,
        HttpServletResponse response,
        @PathVariable Integer id
    ) {
        return pipelineDraftSVC.getPipelineDrafts(id);
    }

    @GetMapping("/pipeline/drafts/list") // 임시저장 목록 조회
    public List<PipelineDraftsListResponseVO> getPipelineDraftsList(
        HttpServletRequest request,
        HttpServletResponse response,
        PipelineListRetrieveVO pipelineListRetrieveVO
    ) {
        return pipelineDraftSVC.getPipelineDraftsList();
    }

    // 파이프라인 생성 중 "다음" 누를시 사용되는 API , 해당 임시파이프라인 upsert처리
    @PostMapping("/pipeline/drafts")
    public void upsertPipelineDrafts(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestBody String requestBody
    ) {
        JSONObject jsonObject = new JSONObject(requestBody);

        if (!jsonObject.isNull("id")) {
            if (pipelineDraftSVC.isExistsDrafts(jsonObject.getInt("id"))) {
                pipelineDraftSVC.updatePipelineDrafts(jsonObject);
                response.setStatus(HttpStatus.OK.value());
            }
        } else {
            if (!pipelineDraftSVC.isExistsNameDrafts(jsonObject.getString("name"))) {
                pipelineDraftSVC.createPipelineDrafts(
                    jsonObject.getString("name"),
                    jsonObject.getString("creator"),
                    jsonObject.getString("detail")
                );
            } else {
                throw new BadRequestException(
                    DataCoreUiCode.ErrorCode.ALREADY_EXISTS,
                    "PipelineName already Exists"
                );
            }
        }
    }

    @Transactional
    @GetMapping("/pipeline/drafts/properties/{id}") //<데이터수집, 정제, 변환> 다음버튼 누를시
    public PipelineVO getPipelineDraftsProperties(
        HttpServletRequest request,
        HttpServletResponse response,
        @PathVariable Integer id,
        @RequestParam(name = "page") Integer page, //collector, filter, converter
        @RequestParam(name = "adaptorName") String adaptorName,
        @RequestParam(name = "datasetid", required = false) String datasetid
    ) {
        PipelineVO pipelineVO = pipelineDraftSVC.getPipelineDraftsProperties(
            id,
            page,
            adaptorName,
            datasetid
        );
        return pipelineVO;
    }

    @Transactional
    @DeleteMapping("/pipeline/drafts/{id}") // 임시저장 삭제
    public void deletePipelineDrafts(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestHeader(HttpHeaders.ACCEPT) String accept,
        @PathVariable Integer id
    ) {
        pipelineDraftSVC.deletePipelineDrafts(id);
        response.setStatus(HttpStatus.OK.value());
    }
}
