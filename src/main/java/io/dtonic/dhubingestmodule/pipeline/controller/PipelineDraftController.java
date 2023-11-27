package io.dtonic.dhubingestmodule.pipeline.controller;

import io.dtonic.dhubingestmodule.pipeline.service.PipelineDraftSVC;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineDraftsListResponseVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineListRetrieveVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineVO;

import java.util.List;


import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PipelineDraftController<T> {

    @Autowired
    private PipelineDraftSVC pipelineDraftSVC;
    
    @GetMapping("/pipelines/drafts/create") // 파이프라인 생성 첫 시작 API, (front에서 빈 Pipeline VO가 필요)
    public PipelineVO createPipelineDrafts(
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
    public ResponseEntity<Object> getPipelineDrafts(
        @PathVariable Integer id
    ) {
        PipelineVO result = pipelineDraftSVC.getPipelineDrafts(id);
        if (result == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Pipeline Not Found");
        } else return ResponseEntity.ok().body(result);
    }

    @GetMapping("/pipelines/drafts/list") // 임시저장 목록 조회
    public ResponseEntity<Object> getPipelineDraftsList(
        PipelineListRetrieveVO pipelineListRetrieveVO
    ) {
        List<PipelineDraftsListResponseVO> result = pipelineDraftSVC.getPipelineDraftsList();
        if (result == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Pipeline Not Found");
        } else return ResponseEntity.ok().body(result);
    }

    // 파이프라인 생성 중 "다음" 누를시 사용되는 API , 해당 임시파이프라인 upsert처리
    @PostMapping("/pipelines/drafts")
    public ResponseEntity<PipelineVO> upsertPipelineDrafts(
        @RequestBody String requestBody
    ) {
        JSONObject jsonObject = new JSONObject(requestBody);
        PipelineVO pipelineVO = new PipelineVO();
        if (!jsonObject.isNull("id")) {
            if (pipelineDraftSVC.isExistsDrafts(jsonObject.getInt("id"))) {
                PipelineVO result = pipelineDraftSVC.updatePipelineDrafts(jsonObject);
                return ResponseEntity.ok().body(result);
            }
        } else {
            if (
                !pipelineDraftSVC.isExistsNameDrafts(jsonObject.getString("name")) &&
                !jsonObject.isNull("name")
            ) {
                PipelineVO res = pipelineDraftSVC.createPipelineDrafts(
                    jsonObject.getString("name"),
                    jsonObject.getString("creator"),
                    jsonObject.getString("detail")
                );
                return ResponseEntity.ok().body(res);
            } else return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(pipelineVO);
        }
        return ResponseEntity.ok().body(pipelineVO);
    }

    @GetMapping("/pipelines/drafts/properties") //<데이터수집, 정제, 변환> 다음버튼 누를시
    public ResponseEntity<Object> getPipelineDraftsProperties(
        @RequestParam(name = "pipelineid", required = false) Integer pipelineid,
        @RequestParam(name = "page") String page, //collector, filter, converter
        @RequestParam(name = "adaptorName", required = false) String adaptorName,
        @RequestParam(name = "datasetid", required = false) String datasetid
    ) {
        PipelineVO  result = pipelineDraftSVC.getPipelineDraftsProperties(
            pipelineid,
            page,
            adaptorName,
            datasetid
        );
        if (result == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Pipeline Not Found");
        } else return ResponseEntity.ok().body(result);
    }
    
    @DeleteMapping("/pipelines/drafts/{id}") // 임시저장 삭제
    public ResponseEntity<Object> deletePipelineDrafts(
        @PathVariable Integer id
    ) {
        Boolean result = pipelineDraftSVC.deletePipelineDrafts(id);
        if (!result) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Pipeline Not Found");
        } else return ResponseEntity.ok().body(result);
    }
}
