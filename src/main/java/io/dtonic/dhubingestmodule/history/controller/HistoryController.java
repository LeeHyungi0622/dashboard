package io.dtonic.dhubingestmodule.history.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import io.dtonic.dhubingestmodule.history.service.HistorySVC;
import io.dtonic.dhubingestmodule.history.vo.CommandVO;
import io.dtonic.dhubingestmodule.history.vo.TaskVO;

@Controller
public class HistoryController {

    @Autowired
    private HistorySVC historySVC;
    
    @GetMapping("/pipelines/hist/cmd/{pipelineId}") 
    public ResponseEntity<List<CommandVO>> getPipelineCmdHistory(
        @PathVariable Integer pipelineId
    ) {
        return ResponseEntity.ok().body(historySVC.getPipelineCmdHistory(pipelineId));
    }

    @GetMapping("/pipelines/hist/task/{commandId}") 
    public ResponseEntity<List<TaskVO>> getPipelineTaskHistory(
        @PathVariable Integer commandId
    ) {
        return ResponseEntity.ok().body(historySVC.getPipelineTaskHistory(commandId));
    }
}
