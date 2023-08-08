package io.dtonic.dhubingestmodule.history.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.dtonic.dhubingestmodule.common.code.MonitoringCode;
import io.dtonic.dhubingestmodule.common.code.NifiStatusCode;
import io.dtonic.dhubingestmodule.history.mapper.HistoryMapper;
import io.dtonic.dhubingestmodule.history.vo.CommandVO;
import io.dtonic.dhubingestmodule.history.vo.TaskVO;
import io.swagger.client.model.ControllerServiceEntity;
import io.swagger.client.model.ControllerServicesEntity;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
public class HistorySVC {
    
    @Autowired
    private HistoryMapper historyMapper;

    public boolean monitoring(MonitoringCode action, String processGroupId) throws JsonMappingException, JsonProcessingException, InterruptedException{
        for (int i =0 ; i < 3; i++){
            long startTime = System.currentTimeMillis();
            boolean result = false;
            switch(action){
                case STOP_PIPELINE: {
                    if(stopProcessorGroup(processGroupId)){
                        while(!result && (System.currentTimeMillis() - startTime < 10000) ){
                            Map<String, Integer> nifiStatus = getStatusProcessGroup(processGroupId);
                            if(nifiStatus.get(NifiStatusCode.NIFI_STATUS_RUNNING.getCode()) == 0){
                                result = true;
                            }
                            
                        }
                            return result;
                    }
                    break;
                }
                case CLEAR_QUEUE: {
                    ResponseEntity<String> response = clearQueuesInProcessGroup(processGroupId);
                    DropRequestEntity resultClearQueue = nifiObjectMapper.readValue(response.getBody(), DropRequestEntity.class);
                    String requestId = resultClearQueue.getDropRequest().getId();
                    if(requestId != null){
                        while(!result && (System.currentTimeMillis() - startTime < 10000)){
                            result = checkclearQueuesInProcessGroup(processGroupId , requestId);
                        }
                        return true;
                    }
                    break;
                }
                case DISABLE_CONTROLLER: {
                    if(disableControllers(processGroupId)){
                        List<Boolean> resList = new ArrayList<Boolean>();
                        while(!result && (System.currentTimeMillis() - startTime < 500)){
                            ControllerServicesEntity controllers = searchControllersInProcessorGroup(processGroupId);
                            for (ControllerServiceEntity controller : controllers.getControllerServices()) {
                                if (controller.getStatus().getRunStatus().equals("DISABLED")){
                                    resList.add(true);
                                }
                            }
                            if(resList.size() == controllers.getControllerServices().size()){
                                result = true;
                            }
                        }
                        return result;
                    }
                    break;
                }
                case DELETE_CONNECTION: {
                    if(niFiSwaggerSVC.deleteConnectionToFunnel(processGroupId)){
                        while(!result && (System.currentTimeMillis() - startTime < 10000)){
                            if(niFiSwaggerSVC.clearQueuesInConnectionToFunnel(processGroupId) == null){
                                result = true;
                            }
                        }
                        return result;
                    }
                    break;
                }
                case DELETE_PIPELINE: {
                    if(niFiSwaggerSVC.deleteProcessGroup(processGroupId)){
                        while(!result && (System.currentTimeMillis() - startTime < 10000)){
                            if(stopProcessorGroup(processGroupId) == false){
                                result = true;
                            }
                        }
                        return result;
                    }
                    break;
                }
                default: {
                    log.error("Invalid Action to Delete Pipeline Process in NiFi : processGroupId = [{}]" + processGroupId);
                    return result;
                }
            }
        }
        return false;
    }
    
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
        }else{
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
