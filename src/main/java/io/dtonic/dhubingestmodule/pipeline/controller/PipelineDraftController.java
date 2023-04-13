package io.dtonic.dhubingestmodule.pipeline.controller;

import io.dtonic.dhubingestmodule.pipeline.service.PipelineDraftSVC;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineListRetrieveVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineVO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
// @RequestMapping(path = "pipelines")
public class PipelineDraftController<T> {

    @Autowired
    private PipelineDraftSVC pipelineDraftSVC;
    
    @GetMapping("/pipelines/drafts/create") // 파이프라인 생성 첫 시작 API, (front에서 빈 Pipeline VO가 필요)
    public PipelineVO createPipelineDrafts(
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        return new PipelineVO();
    }

    /**
     * Retrieve Pipeline
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param accept request accept header
     * @param id retrieve Pipeline id
     * @return Pipeline object
     */
    @GetMapping("/pipelines/drafts/{id}") // 임시저장 상세 조회
    public ResponseEntity getPipelineDrafts(
        HttpServletRequest request,
        HttpServletResponse response,
        @PathVariable Integer id
    ) {
        ResponseEntity result = pipelineDraftSVC.getPipelineDrafts(id);
        return result;
    }

    @GetMapping("/pipelines/drafts/list") // 임시저장 목록 조회
    public ResponseEntity getPipelineDraftsList(
        HttpServletRequest request,
        HttpServletResponse response,
        PipelineListRetrieveVO pipelineListRetrieveVO
    ) {
        ResponseEntity result = pipelineDraftSVC.getPipelineDraftsList();
        return result;
    }

    // 파이프라인 생성 중 "다음" 누를시 사용되는 API , 해당 임시파이프라인 upsert처리
    @PostMapping("/pipelines/drafts")
    public ResponseEntity<PipelineVO> upsertPipelineDrafts(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestBody String requestBody
    ) {
        JSONObject jsonObject = new JSONObject(requestBody);
        PipelineVO pipelineVO = new PipelineVO();
        if (!jsonObject.isNull("id")) {
            if (Boolean.TRUE.equals(pipelineDraftSVC.isExistsDrafts(jsonObject.getInt("id")))) {
                PipelineVO result = pipelineDraftSVC.updatePipelineDrafts(jsonObject);
                return ResponseEntity.ok().body(result);
            }
        } else {
            if (
                !pipelineDraftSVC.isExistsNameDrafts(jsonObject.getString("name")) &&
                !jsonObject.isNull("name")
            ) {
                return pipelineDraftSVC.createPipelineDrafts(
                    jsonObject.getString("name"),
                    jsonObject.getString("creator"),
                    jsonObject.getString("detail")
                );
            } else return new ResponseEntity<>(pipelineVO, HttpStatus.BAD_REQUEST);
            // return (ResponseEntity<T>) ResponseEntity
            //     .status(HttpStatus.BAD_REQUEST)
            //     .body("PipelineName already Exists");
        }
        return ResponseEntity.ok().body(pipelineVO);
    }

    @Transactional
    @GetMapping("/pipelines/drafts/properties") //<데이터수집, 정제, 변환> 다음버튼 누를시
    public ResponseEntity getPipelineDraftsProperties(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestParam(name = "pipelineid", required = false) Integer pipelineid,
        @RequestParam(name = "page") String page, //collector, filter, converter
        @RequestParam(name = "adaptorName", required = false) String adaptorName,
        @RequestParam(name = "datasetid", required = false) String datasetid
    ) {
        ResponseEntity result = pipelineDraftSVC.getPipelineDraftsProperties(
            pipelineid,
            page,
            adaptorName,
            datasetid
        );
        return result;
    }
    
    @Transactional
    @DeleteMapping("/pipelines/drafts/{id}") // 임시저장 삭제
    public ResponseEntity deletePipelineDrafts(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestHeader(HttpHeaders.ACCEPT) String accept,
        @PathVariable Integer id
    ) {
        ResponseEntity result = pipelineDraftSVC.deletePipelineDrafts(id);
        return result;
    }
}
