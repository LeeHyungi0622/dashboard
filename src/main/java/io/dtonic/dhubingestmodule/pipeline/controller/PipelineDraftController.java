package io.dtonic.dhubingestmodule.pipeline.controller;

import io.dtonic.dhubingestmodule.common.code.DataCoreUiCode;
import io.dtonic.dhubingestmodule.common.exception.BadRequestException;
import io.dtonic.dhubingestmodule.common.exception.ResourceNotFoundException;
import io.dtonic.dhubingestmodule.nifi.vo.AdaptorVO;
import io.dtonic.dhubingestmodule.nifi.vo.PropertyVO;
import io.dtonic.dhubingestmodule.pipeline.service.PipelineDraftSVC;
import io.dtonic.dhubingestmodule.pipeline.service.PipelineDraftSVC;
import io.dtonic.dhubingestmodule.pipeline.vo.DataCollectorVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineCreateVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineDraftsListResponseVO;
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

    @GetMapping("/pipeline/drafts/{id}") // 임시저장 상세 조회
    public PipelineVO getPipelineDrafts(
        HttpServletRequest request,
        HttpServletResponse response,
        @PathVariable Integer id
    ) {
        return pipelineSVC.getPipelineDrafts(id);
    }

    @GetMapping("/pipeline/drafts/list") // 임시저장 목록 조회
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

    // @Transactional
    // @PutMapping("/pipeline/drafts") // 임시저장 등록/수정
    // public Integer upsertPipelineDrafts(
    //     HttpServletRequest request,
    //     HttpServletResponse response,
    //     @RequestBody String requestBody
    // ) {
    //     JSONObject jsonObject = new JSONObject(requestBody);
    //     if (!jsonObject.has("id")) {
    //         pipelineSVC.createPipelineDrafts(jsonObject);
    //         response.setStatus(HttpStatus.OK.value());
    //         return pipelineSVC.getRecentPipelineDraftsId();
    //     } else {
    //         pipelineSVC.updatePipelineDrafts(jsonObject);
    //         response.setStatus(HttpStatus.OK.value());
    //         return jsonObject.getInt("id");
    //     }
    // }

    @PostMapping("/pipeline/drafts") // <기본정보입력> 다음버튼 누를시 (파이프라인 create)
    public void createPipelineDrafts(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestBody PipelineVO pipelineCreateVO
    ) {
        Integer id = pipelineCreateVO.getId();

        if (pipelineSVC.isExistsDrafts(id)) {
            pipelineSVC.updatePipelineDrafts(pipelineCreateVO);
        } else {
            pipelineSVC.createPipelineDrafts(
                pipelineCreateVO.getName(),
                pipelineCreateVO.getCreator(),
                pipelineCreateVO.getDetail()
            );
        }
        response.setStatus(HttpStatus.CREATED.value());
    }

    @GetMapping("/pipeline/drafts/collectors") // 데이터수집기 리스트 리턴
    public List<DataCollectorVO> getPipelinecollectors(
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        return pipelineSVC.getDataCollector();
    }

    @GetMapping("/pipeline/drafts/properties") // <데이터수집> 데이터수집 선택완료시
    public AdaptorVO getPipelineproperties(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestParam(name = "adaptorName") String adaptorName,
        @RequestParam(name = "Pipelineid") Integer pipelineid
    ) {
        AdaptorVO adaptorVO = pipelineSVC.getPipelineproperties(adaptorName, pipelineid);
        return adaptorVO;
    }

    @Transactional
    @GetMapping("/pipeline/drafts") //<데이터수집, 정제, 변환> 다음버튼 누를시
    public @ResponseBody PipelineVO updatePipelineDrafts(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestParam(name = "page") Integer page,
        @RequestParam(name = "Pipelineid") Integer pipelineid,
        @RequestParam(name = "adaptorName") String adaptorName,
        @RequestParam(name = "datasetid", required = false) String datasetid
    ) {
        PipelineVO pipelineVO = pipelineSVC.getPipelineDraftsPage(
            pipelineid,
            page,
            adaptorName,
            datasetid
        );
        return pipelineVO;
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
    @DeleteMapping("/pipeline/drafts/{id}") // 임시저장 삭제
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
