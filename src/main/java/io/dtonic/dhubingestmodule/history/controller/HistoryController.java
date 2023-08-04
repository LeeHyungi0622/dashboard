package io.dtonic.dhubingestmodule.history.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import io.dtonic.dhubingestmodule.history.service.HistorySVC;
import io.dtonic.dhubingestmodule.pipeline.vo.CommandVO;
import io.dtonic.dhubingestmodule.pipeline.vo.TaskVO;

@Controller
public class HistoryController {

    @Autowired
    private HistorySVC historySVC;
    
    @GetMapping("/pipelines/hist/cmd/{pipelineId}") 
    public ResponseEntity<List<CommandVO>> getPipelineCmdHistory(
        HttpServletRequest request,
        HttpServletResponse response,
        @PathVariable Integer pipelineId
    ) {
        return ResponseEntity.ok().body(historySVC.getPipelineCmdHistory(pipelineId));
    }

    @GetMapping("/pipelines/hist/task/{commandId}") 
    public ResponseEntity<List<TaskVO>> getPipelineTaskHistory(
        HttpServletRequest request,
        HttpServletResponse response,
        @PathVariable Integer commandId
    ) {
        return ResponseEntity.ok().body(historySVC.getPipelineTaskHistory(commandId));
    }
}
