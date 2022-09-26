package io.dtonic.dhubingestmodule.pipeline.controller;

import graphql.com.google.common.collect.PeekingIterator;
import io.dtonic.dhubingestmodule.common.code.DataCoreUiCode;
import io.dtonic.dhubingestmodule.common.exception.BadRequestException;
import io.dtonic.dhubingestmodule.common.exception.ResourceNotFoundException;
import io.dtonic.dhubingestmodule.nifi.vo.PropertyVO;
import io.dtonic.dhubingestmodule.pipeline.service.PipelineDraftSVC;
import io.dtonic.dhubingestmodule.pipeline.vo.DataCollectorVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineCreateVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineListRetrieveVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineVO;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
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
    @PostMapping("/pipeline/drafts") // <기본정보입력> 다음버튼 누를시
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

    @GetMapping("/pipeline/drafts/collectors") // <데이터수집> 데이터수집기 누를시
    public List<DataCollectorVO> getPipelinecollectors(
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        return pipelineSVC.getDataCollector();
    }

    @GetMapping("/pipeline/drafts/properties") // <데이터수집> 데이터수집 선택완료시
    public PipelineVO getPipelineproperties(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestParam(name = "adaptorid") Integer adaptorid,
        @RequestParam(name = "Pipelineid") Integer pipelineid
    ) {
        PipelineVO pipelineVO = pipelineSVC.getPipelineproperties(adaptorid, pipelineid);
        return pipelineVO;
    }

    // @Transactional
    // @PutMapping("/pipeline/drafts/{page}") //<데이터수집, 정제, 변환> 다음버튼 누를시
    // public @ResponseBody PipelineVO updatePipelineDrafts(
    //     HttpServletRequest request,
    //     HttpServletResponse response,
    //     @PathVariable Integer page,
    //     @RequestBody PipelineVO requestBody
    // ) {
    //     pipelineSVC.updatePipelineDrafts(requestBody);
    //     PipelineVO pipelineVO = new PipelineVO();
    //     switch (page) {
    //         case 1: //수집에서 다음 누를때
    //             pipelineVO.getFilter().getNifiComponents().get(0).setName("Base64 Decoder");
    //             pipelineVO
    //                 .getFilter()
    //                 .getNifiComponents()
    //                 .get(0)
    //                 .getRequiredProps()
    //                 .get(0)
    //                 .setInputValue("false");
    //             pipelineVO.getFilter().getNifiComponents().get(1).setName("Root Key Finder");
    //             pipelineVO
    //                 .getFilter()
    //                 .getNifiComponents()
    //                 .get(1)
    //                 .getRequiredProps()
    //                 .get(0)
    //                 .setName("Root Key 설정");
    //         case 2: //정제에서 다음 누를때
    //     }
    //     return pipelineVO;
    // }

    @Transactional
    @DeleteMapping("/pipeline/drafts/{id}")
    public @ResponseBody void deletePipelineDrafts(
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
