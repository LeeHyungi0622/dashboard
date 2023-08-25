package io.dtonic.dhubingestmodule.history.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.dtonic.dhubingestmodule.history.mapper.HistoryMapper;
import io.dtonic.dhubingestmodule.history.vo.CommandVO;
import io.dtonic.dhubingestmodule.history.vo.TaskVO;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
public class HistorySVC {
    
    @Autowired
    private HistoryMapper historyMapper;
    
    public Integer createTask(TaskVO taskVO) {
        try{
            historyMapper.createTask(taskVO);
            return taskVO.getId();
        }catch(Exception e){
            log.error("create task error {}" , e);
            return null;
        }
    }

    public Boolean updateTask(Integer id, String taskStatusCode) {
        if(historyMapper.updateTask(id, taskStatusCode) == 1){
            return true;
        }else{
            log.error("Update Task error");
            return false;
        }
    }

    
    public Integer createCommand(CommandVO commandVO) {
        try{
            historyMapper.createCommand(commandVO);
            return commandVO.getId();
        }
        catch(Exception e){
            log.error("Create command error {}", e);
            return null;
        }
    }

    public Boolean updateCommand(Integer id, String status) {
        if(historyMapper.updateCommand(id, status) == 1){
            return true;
        } else {
            log.error("Update Command error");
            return false;
        }
    }
    public Boolean updateCommandPipelineId(Integer id, Integer pipelineId) {
        if(historyMapper.updateCommandPipelineId(id, pipelineId) == 1){
            return true;
        } else {
            log.error("Update Command error");
            return false;
        }
    }

    public List<CommandVO> getPipelineCmdHistory(Integer pipelineId){
        return historyMapper.getPipelineCmdHistory(pipelineId);
    }
    
    public List<TaskVO> getPipelineTaskHistory(Integer commandId){
        List<TaskVO> taskVOs = historyMapper.getPipelineTaskHistory(commandId) ;
        return taskVOs;
    }
}
