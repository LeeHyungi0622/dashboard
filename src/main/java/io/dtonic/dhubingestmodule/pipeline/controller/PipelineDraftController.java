package io.dtonic.dhubingestmodule.pipeline.controller;

import io.dtonic.dhubingestmodule.common.code.DataCoreUiCode;
import io.dtonic.dhubingestmodule.common.exception.BadRequestException;
import io.dtonic.dhubingestmodule.pipeline.service.PipelineDraftSVC;
import io.dtonic.dhubingestmodule.pipeline.vo.DataCollectorVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineListRetrieveVO;
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

    @GetMapping("/pipeline/drafts/{id}")
    public @ResponseBody PipelineVO getPipelineDrafts(
        HttpServletRequest request,
        HttpServletResponse response,
        @PathVariable Integer id
    ) {
        PipelineVO pipelineVO = pipelineSVC.getPipelineDrafts(id);
        return pipelineVO;
    }

    @GetMapping("/pipeline/drafts/list")
    public @ResponseBody List<PipelineVO> getPipelineDraftsList(
        HttpServletRequest request,
        HttpServletResponse response,
        PipelineListRetrieveVO pipelineListRetrieveVO
    ) {
        List<PipelineVO> pipelineVO = pipelineSVC.getPipelineDraftsList(
            pipelineListRetrieveVO.getSearchObject(),
            pipelineListRetrieveVO.getSearchValue()
        );
        return pipelineVO;
    }

    @Transactional
    @PostMapping("/pipeline/drafts") // <기본정보입력> 다음버튼 누를시 (파이프라인 create)
    public void createPipelineDrafts(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestBody PipelineVO PipelineVO
    ) {
        Integer id = PipelineVO.getId();

        if (pipelineSVC.isExistsDrafts(id)) {
            pipelineSVC.updatePipelineDrafts(PipelineVO);
        } else {
            pipelineSVC.createPipelineDrafts(
                PipelineVO.getName(),
                PipelineVO.getCreator(),
                PipelineVO.getDetail()
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
