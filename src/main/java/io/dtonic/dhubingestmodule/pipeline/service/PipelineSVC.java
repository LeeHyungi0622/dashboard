package io.dtonic.dhubingestmodule.pipeline.service;

import io.dtonic.dhubingestmodule.common.code.AdaptorName;
import io.dtonic.dhubingestmodule.common.code.CommandStatusCode;
import io.dtonic.dhubingestmodule.common.code.DataCoreUiCode;
import io.dtonic.dhubingestmodule.common.code.NifiStatusCode;
import io.dtonic.dhubingestmodule.common.code.PipelineStatusCode;
import io.dtonic.dhubingestmodule.common.code.TaskStatusCode;
import io.dtonic.dhubingestmodule.common.exception.BadRequestException;
import io.dtonic.dhubingestmodule.common.thread.MultiThread;
import io.dtonic.dhubingestmodule.dataset.service.DataSetSVC;
import io.dtonic.dhubingestmodule.dataset.vo.DataModelVO;
import io.dtonic.dhubingestmodule.nifi.controller.NiFiController;
import io.dtonic.dhubingestmodule.nifi.vo.AdaptorVO;
import io.dtonic.dhubingestmodule.nifi.vo.NiFiComponentVO;
import io.dtonic.dhubingestmodule.nifi.vo.PropertyVO;
import io.dtonic.dhubingestmodule.pipeline.mapper.PipelineMapper;
import io.dtonic.dhubingestmodule.pipeline.vo.CommandVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineListResponseVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineVOtoDB;
import io.dtonic.dhubingestmodule.pipeline.vo.TaskVO;

import io.dtonic.dhubingestmodule.util.ValidateUtil;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

@Slf4j
@RequiredArgsConstructor
@Service
@EnableScheduling
public class PipelineSVC {

    @Autowired
    private PipelineMapper pipelineMapper;

    @Autowired
    private PipelineDraftSVC pipelineDraftSVC;

    @Autowired
    private DataSetSVC dataSetSVC;

    @Autowired
    private NiFiController niFiController;

    @Transactional
    public ResponseEntity createPipeline(Integer id, PipelineVO pipelineVO) {

        CommandVO commandVO = new CommandVO();
        commandVO.setCommand(CommandStatusCode.COMMAND_CREATE.getCode());
        commandVO.setStatus(CommandStatusCode.COMMAND_STATUS_CREATING.getCode());
        commandVO.setUserId(pipelineVO.getCreator());

        PipelineVOtoDB pipelineVOtoDB = new PipelineVOtoDB();
        Integer pipelineId = null;
        
        JSONObject jsonObject = new JSONObject(pipelineVO);
        pipelineVOtoDB.setName(pipelineVO.getName());
        pipelineVOtoDB.setDataModel(pipelineVO.getDataModel());
        pipelineVOtoDB.setDataSet(pipelineVO.getDataSet());
        pipelineVOtoDB.setDetail(pipelineVO.getDetail());
        pipelineVOtoDB.setCreator(pipelineVO.getCreator());
        pipelineVOtoDB.setStatus(PipelineStatusCode.PIPELINE_STATUS_STOPPED.getCode());
        pipelineVOtoDB.setProcessorGroupId(null);
        pipelineVOtoDB.setCollector(jsonObject.getJSONObject(AdaptorName.ADAPTOR_NAME_COLLECTOR.getCode()).toString());
        pipelineVOtoDB.setFilter(jsonObject.getJSONObject(AdaptorName.ADAPTOR_NAME_FILTER.getCode()).toString());
        pipelineVOtoDB.setConverter(jsonObject.getJSONObject(AdaptorName.ADAPTOR_NAME_CONVERTER.getCode()).toString());

        pipelineMapper.createPipeline(pipelineVOtoDB);

        pipelineId = pipelineVOtoDB.getId();
        commandVO.setPipelineId(pipelineId);

        Integer commandId = createCommand(commandVO);
        
        String processorGroupId = niFiController.createPipeline(pipelineVO, commandId);
        
        if (processorGroupId != null) {
            //임시 파이프라인 삭제
            pipelineDraftSVC.deletePipelineDrafts(id);

            pipelineMapper.updatePipelineProcessgroupId(pipelineId, processorGroupId);

            updateCommand(commandId,CommandStatusCode.COMMAND_STATUS_SUCCEED.getCode());

            return ResponseEntity.ok().build();
        } else {
            updateCommand(commandId,CommandStatusCode.COMMAND_STATUS_FAILED.getCode());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Nifi error");
        }
    }

    @Transactional
    @Scheduled(fixedDelay = 3000, initialDelay = 3000)
    public ResponseEntity getPipelineList() {
        List<PipelineListResponseVO> pipelineListVOs = pipelineMapper.getPipelineList();
        pipelineListVOs
            .parallelStream()
            .forEach(
                pipelineVO -> {
                    PipelineVO pipeline = (PipelineVO) getPipelineVOById(pipelineVO.getId())
                        .getBody();
                    Map<String, Integer> nifiStatus = niFiController.getPipelineStatus(
                        pipeline.getProcessorGroupId()
                    );
                    //log.info("{}", nifiStatus);
                    if (!ValidateUtil.isMapEmpty(nifiStatus)) {
                        String curStatus = pipelineVO.getStatus(); // 현재 DB status값
                        if (
                            curStatus.equals(
                                PipelineStatusCode.PIPELINE_STATUS_STARTING.getCode()
                            ) ||
                            curStatus.equals(
                                PipelineStatusCode.PIPELINE_STATUS_STOPPING.getCode()
                            ) ||
                            curStatus.equals(PipelineStatusCode.PIPELINE_STATUS_RUN.getCode())
                        ) {
                            if ( //case: DB=Starting, Nifi=Running
                                curStatus.equals(
                                    PipelineStatusCode.PIPELINE_STATUS_STARTING.getCode()
                                ) &&
                                nifiStatus.get(NifiStatusCode.NIFI_STATUS_STOPPED.getCode()) == 0 &&
                                nifiStatus.get(NifiStatusCode.NIFI_STATUS_INVALID.getCode()) == 0
                            ) {
                                log.info("{}", PipelineStatusCode.PIPELINE_STATUS_RUN.getCode());
                                pipeline.setStatus(
                                    PipelineStatusCode.PIPELINE_STATUS_RUN.getCode()
                                );
                            } else if ( //case: DB=Stopping, Nifi=Stopped
                                curStatus.equals(
                                    PipelineStatusCode.PIPELINE_STATUS_STOPPING.getCode()
                                ) &&
                                nifiStatus.get(NifiStatusCode.NIFI_STATUS_RUNNING.getCode()) == 0 &&
                                nifiStatus.get(NifiStatusCode.NIFI_STATUS_INVALID.getCode()) == 0
                            ) {
                                pipeline.setStatus(
                                    PipelineStatusCode.PIPELINE_STATUS_STOPPED.getCode()
                                );
                            } else if ( //case: DB=Run, Nifi=Running
                                curStatus.equals(
                                    PipelineStatusCode.PIPELINE_STATUS_RUN.getCode()
                                ) &&
                                nifiStatus.get("Stopped") != 0 ||
                                nifiStatus.get("Invaild") != 0
                            ) {
                                pipeline.setStatus(
                                    PipelineStatusCode.PIPELINE_STATUS_STOPPED.getCode()
                                );
                            }
                            try {
                                changePipelineStatus(pipeline.getId(), pipeline.getStatus());
                            } catch (JsonProcessingException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            );
        return ResponseEntity.ok().body(pipelineListVOs);
    }

    public ResponseEntity getPipelineStatus_websocket(){
        List<PipelineListResponseVO> pipelineListVOs = pipelineMapper.getPipelineList();
        return ResponseEntity.ok().body(pipelineListVOs);
    }

    public ResponseEntity getPipelineVOById(Integer id) {
        PipelineVO result = pipelineMapper.getPipeline(id);
        if (ValidateUtil.isEmptyData(result)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Pipeline Not Exist");
            // throw new BadRequestException(
            //     DataCoreUiCode.ErrorCode.NOT_EXIST_ENTITY,
            //     "Pipeline Not Exist"
            // );
        }
        return ResponseEntity.ok().body(result);
    }

    @Transactional
    public ResponseEntity changePipelineStatus(Integer id, String status) throws JsonMappingException, JsonProcessingException {
        PipelineVO pipelineVO = (PipelineVO) getPipelineVOById(id).getBody();
        Boolean Nifiresult = false;
        
        if (status.equals(PipelineStatusCode.PIPELINE_STATUS_STARTING.getCode())) {
            try {
                Nifiresult = niFiController.runPipeline(pipelineVO.getProcessorGroupId());
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        } else if (status.equals(PipelineStatusCode.PIPELINE_STATUS_STOPPING.getCode())) {
            Nifiresult = niFiController.stopPipeline(pipelineVO.getProcessorGroupId());
        } else {
            Nifiresult = true;
        }
        if (Nifiresult) {
            int result = pipelineMapper.changePipelineStatus(id, status);
            if (result != 1) {
                pipelineMapper.changePipelineStatus(id, PipelineStatusCode.PIPELINE_STATUS_FAILED.getCode());
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Change Pipeline Status Error in DB");
            } else {
                return ResponseEntity.ok().build();
            }
        } else {
            pipelineMapper.changePipelineStatus(id, PipelineStatusCode.PIPELINE_STATUS_FAILED.getCode());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Nifi error");
        }
    }

    @Transactional
    public ResponseEntity deletePipeline(Integer id, String userId){
        try{PipelineVO pipelineVO = (PipelineVO) getPipelineVOById(id).getBody();

        CommandVO commandVO = new CommandVO();
        commandVO.setPipelineId(id);
        commandVO.setCommand(CommandStatusCode.COMMAND_DELETE.getCode());
        commandVO.setStatus(CommandStatusCode.COMMAND_STATUS_DELETING.getCode());
        commandVO.setUserId(userId);
        Integer commandId = createCommand(commandVO);
        
        /* Delete Pipleine using thread */
        Runnable r = new MultiThread(pipelineVO, commandId);
        Thread thread = new Thread(r);
        thread.start();

        return changePipelineStatus(id, PipelineStatusCode.PIPELINE_STATUS_DELETING.getCode());}
        catch(Exception e){
            log.error(" Delete Pipeline error processGroup Id : {}", id, e);
            return null;
        }
    }

    @Transactional
    public ResponseEntity updatePipeline(Integer id, PipelineVO pipelineVO, String userId) throws JsonMappingException, JsonProcessingException {
        
        changePipelineStatus(id, PipelineStatusCode.PIPELINE_STATUS_UPDATING.getCode());

        CommandVO commandVO = new CommandVO();
        commandVO.setPipelineId(id);
        commandVO.setCommand(CommandStatusCode.COMMAND_UPDATE.getCode());
        commandVO.setStatus(CommandStatusCode.COMMAND_STATUS_UPDATING.getCode());
        commandVO.setUserId(userId);
        Integer commandId = createCommand(commandVO);

        String processorGroupId = niFiController.updatePipeline(pipelineVO, commandId);
        
        if (processorGroupId != null) {
            JSONObject jsonObject = new JSONObject(pipelineVO);
            int result = pipelineMapper.updatePipeline(
                jsonObject.getInt("id"),
                jsonObject.getString("name"),
                jsonObject.getString("detail"),
                jsonObject.getString("dataSet"),
                jsonObject.getString("dataModel"),
                processorGroupId,
                jsonObject.getJSONObject(AdaptorName.ADAPTOR_NAME_COLLECTOR.getCode()).toString(),
                jsonObject.getJSONObject(AdaptorName.ADAPTOR_NAME_FILTER.getCode()).toString(),
                jsonObject.getJSONObject(AdaptorName.ADAPTOR_NAME_CONVERTER.getCode()).toString()
            );
            log.debug("result : {}", result);
            if (result != 1) {
                updateCommand(commandId, CommandStatusCode.COMMAND_STATUS_FAILED.getCode());
                pipelineMapper.changePipelineStatus(id, PipelineStatusCode.PIPELINE_STATUS_FAILED.getCode());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Update Pipeline Error");
            } else {
                updateCommand(commandId, CommandStatusCode.COMMAND_STATUS_SUCCEED.getCode());
                pipelineMapper.changePipelineStatus(id, PipelineStatusCode.PIPELINE_STATUS_STOPPED.getCode());
                return ResponseEntity.ok().build();
            }
        } else {
            updateCommand(commandId, CommandStatusCode.COMMAND_STATUS_FAILED.getCode());
            pipelineMapper.changePipelineStatus(id, PipelineStatusCode.PIPELINE_STATUS_FAILED.getCode());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Nifi error");
        }
    }

    public ResponseEntity getPipelineProperties(
        Integer pipelineid,
        String page,
        String adaptorName,
        String datasetid
    ) {
        PipelineVO pipelineVO = (PipelineVO) getPipelineVOById(pipelineid).getBody();
        AdaptorVO adaptorVO = pipelineDraftSVC.getPipelineproperties(adaptorName);
        if (page.equals(AdaptorName.ADAPTOR_NAME_COLLECTOR.getCode())) {
            pipelineVO.setCollector(adaptorVO);
        } else if (page.equals(AdaptorName.ADAPTOR_NAME_FILTER.getCode())) {
            pipelineVO.setFilter(adaptorVO);
        } else if (page.equals(AdaptorName.ADAPTOR_NAME_CONVERTER.getCode())) {
            DataModelVO dataModelVO = dataSetSVC.getDataModelId( //model ID 가져오기
                datasetid
            );
            pipelineVO.setDataSet(datasetid);
            pipelineVO.setDataModel(dataModelVO.getId());
            dataModelVO = dataSetSVC.getDataModelProperties(dataModelVO.getId());
            NiFiComponentVO niFiComponentVO = new NiFiComponentVO();
            for (int i = 0; i < dataModelVO.getAttributes().size(); i++) {
                PropertyVO propertyVO = new PropertyVO();
                propertyVO.setName(dataModelVO.getAttributes().get(i).getName());
                propertyVO.setDetail(dataModelVO.getAttributes().get(i).getAttributeType());
                niFiComponentVO.getRequiredProps().add(propertyVO);
                niFiComponentVO.setName("DataSetProps");
                niFiComponentVO.setType("Processor");
            }
            adaptorVO.getNifiComponents().add(niFiComponentVO);
            pipelineVO.setConverter(adaptorVO);
        }
        return ResponseEntity.ok().body(pipelineVO);
    }

    public Boolean isExists(Integer id) {
        return pipelineMapper.isExists(id);
    }

    public Integer createCommand(CommandVO commandVO) {
        try{
            pipelineMapper.createCommand(commandVO);
            return commandVO.getId();
        }
        catch(Exception e){
            log.error("Create command error {}", e);
            return -1;
        }
    }

    public Boolean updateCommand(Integer id, String status) {
        if(pipelineMapper.updateCommand(id, status) == 1){
            return true;
        }else{
            log.error("Update Command error");
            return false;
        }
    }

    public ResponseEntity getPipelineCmdHistory(Integer pipelineId){
        List <CommandVO> commandVOs = pipelineMapper.getPipelineCmdHistory(pipelineId) ;
        return ResponseEntity.ok().body(commandVOs);
    }
    public ResponseEntity getPipelineTaskHistory(Integer commandId){
        List <TaskVO> taskVOs = pipelineMapper.getPipelineTaskHistory(commandId) ;
        return ResponseEntity.ok().body(taskVOs);
    }

}
