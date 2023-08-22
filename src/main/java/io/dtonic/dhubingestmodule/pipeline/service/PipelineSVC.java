package io.dtonic.dhubingestmodule.pipeline.service;

import io.dtonic.dhubingestmodule.adaptor.vo.AdaptorVO;
import io.dtonic.dhubingestmodule.common.code.AdaptorName;
import io.dtonic.dhubingestmodule.common.code.CommandStatusCode;
import io.dtonic.dhubingestmodule.common.code.NifiStatusCode;
import io.dtonic.dhubingestmodule.common.code.PipelineStatusCode;
import io.dtonic.dhubingestmodule.dataset.service.DataSetSVC;
import io.dtonic.dhubingestmodule.dataset.vo.DataModelVO;
import io.dtonic.dhubingestmodule.history.aop.command.CommandHistory;
import io.dtonic.dhubingestmodule.nifi.controller.NiFiController;
import io.dtonic.dhubingestmodule.nifi.vo.NiFiComponentVO;
import io.dtonic.dhubingestmodule.nifi.vo.PropertyVO;
import io.dtonic.dhubingestmodule.pipeline.mapper.PipelineMapper;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineListResponseVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineVO;
import io.dtonic.dhubingestmodule.util.ValidateUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    
    public Boolean createPipeline(Integer temperalId, PipelineVO pipelineVO) {
        /* Insert Row Pipeline Info */
        pipelineMapper.createPipeline(pipelineVO);
        /* Excute NiFi Create Command */
        String processorGroupId = createNiFiPipeline(pipelineVO.getId(), pipelineVO.getCreator(), null, pipelineVO);
        
        if (processorGroupId != null) {
            /* Delete Row Temperal Pipeline Info */
            Boolean tmpRes = pipelineDraftSVC.deletePipelineDrafts(temperalId);
            if (!tmpRes) {
                log.error("Fail to Delete Temperal Pipeline ID : [{}]", temperalId);
                return false;
            }
            pipelineMapper.updatePipelineProcessgroupId(pipelineVO.getId(), processorGroupId);
            return true;
        } else {
            changePipelineStatus(pipelineVO.getId(), PipelineStatusCode.PIPELINE_STATUS_FAILED.getCode());
            return false;
        }
    }
    /**
     * Create NiFi Pipeline
     * @param pipelineId using command history aop
     * @param userId using command history aop
     * @param commandId using command history aop
     * @param pipelineVO using command history aop
     * @return String
     * 
     * @since 2023. 8. 16
     * @version 1.2.0
     * @auther Justin
     */
    @CommandHistory(command = CommandStatusCode.COMMAND_CREATE)
    private String createNiFiPipeline(Integer pipelineId, String userId, Integer commandId, PipelineVO pipelineVO) {
        String processorGroupId = niFiController.createPipeline(pipelineVO, commandId);
        return processorGroupId;
    }   

    @Transactional
    public List<PipelineListResponseVO> getPipelineList() {
        List<PipelineListResponseVO> pipelineListVOs = pipelineMapper.getPipelineList();
        if(pipelineListVOs.size() > 0){
            pipelineListVOs
                .parallelStream()
                .forEach(
                    pipelineVO -> {
                        PipelineVO pipeline = getPipelineVOById(pipelineVO.getId());
                        Map<String, Integer> nifiStatus = niFiController.getPipelineStatus(
                            pipeline.getProcessorGroupId()
                        );
                        if (!ValidateUtil.isMapEmpty(nifiStatus)) {
                            String curStatus = pipelineVO.getStatus(); // 현재 DB status값
                            if ( //case: DB=Run, Nifi=stopped or invalid (error발생)
                                curStatus.equals(PipelineStatusCode.PIPELINE_STATUS_RUN.getCode()) &&
                                nifiStatus.get("Stopped") != 0 ||
                                nifiStatus.get("Invaild") != 0
                            ) {
                                if ( //case: DB=Starting, Nifi=Running
                                    curStatus.equals(
                                        PipelineStatusCode.PIPELINE_STATUS_STARTING.getCode()
                                    ) &&
                                    nifiStatus.get(NifiStatusCode.NIFI_STATUS_STOPPED.getCode()) == 0 &&
                                    nifiStatus.get(NifiStatusCode.NIFI_STATUS_INVALID.getCode()) == 0
                                ) {
                                    log.debug("{}", PipelineStatusCode.PIPELINE_STATUS_RUN.getCode());
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
                            }
                        }
                    }
                );
                return pipelineListVOs;
        } else {
            log.debug("Empty Pipeline", pipelineListVOs);
            return null;
        }
    }
    /**
     * [Retrieve Pipeline Detail by id]
     * 
     * if pipeline is not exist in DataBase, return null
     * else return pipeline detail
     * @param id Pipeline id
     * @return ResponseEntity<Object> pipeline detail
     * 
     * @since 2023. 8. 16
     * @version 1.2.0
     * @auther Justin
     */
    public PipelineVO getPipelineVOById(Integer id) {
        PipelineVO result = pipelineMapper.getPipeline(id);
        if (ValidateUtil.isEmptyData(result)) {
            return null;
        }
        return result;
    }
    /**
     * Pipeline Status Update (DB)
     *
     * @param id       retrieve Pipeline id
     * @param status   PipelineStatusCode
     * @return ResponseEntity
     * 
     * @since 2023. 8. 21
     * @version 1.2.0
     * @auther Justin
     */
    @Transactional
    public Boolean changePipelineStatus(Integer id, String status) {
        PipelineVO pipelineVO = getPipelineVOById(id);
        Boolean res = true;
        try{
            if (status.equals(PipelineStatusCode.PIPELINE_STATUS_STARTING.getCode())) {
                /* Run Command NiFi */
                res = runPipeline(id, pipelineVO.getCreator(), null, pipelineVO);
            } else if (status.equals(PipelineStatusCode.PIPELINE_STATUS_STOPPING.getCode())) {
                /* Stop Command NiFi */
                res = stopPipeline(id, pipelineVO.getCreator(), null, pipelineVO);
            }
            if (res) {
                if (status.equals(PipelineStatusCode.PIPELINE_STATUS_STARTING.getCode())) {
                    pipelineMapper.changePipelineStatus(id, PipelineStatusCode.PIPELINE_STATUS_RUN.getCode());
                    return true;
                } else if (status.equals(PipelineStatusCode.PIPELINE_STATUS_STOPPING.getCode())) {
                    pipelineMapper.changePipelineStatus(id, PipelineStatusCode.PIPELINE_STATUS_STOPPED.getCode());
                    return true;
                }    
            } else {
                pipelineMapper.changePipelineStatus(id, PipelineStatusCode.PIPELINE_STATUS_FAILED.getCode());
                return false;
            }
        } catch (Exception e){
            pipelineMapper.changePipelineStatus(id, PipelineStatusCode.PIPELINE_STATUS_FAILED.getCode());
            log.error("change Pipeline Status error ={}", e);
            return false;
        }
        return false;
    }
    
    /**
     * Run NiFi Pipeline
     * @param pipelineId using command history aop
     * @param userId using command history aop
     * @param commandId using command history aop
     * @param pipelineVO using command history aop
     * @return String
     * 
     * @since 2023. 8. 16
     * @version 1.2.0
     * @auther Justin
     */
    @CommandHistory(command = CommandStatusCode.COMMAND_RUN)
    private Boolean runPipeline(Integer pipelineId, String userId, Integer commandId, PipelineVO pipelineVO) throws Exception{
        return niFiController.runPipeline(commandId, pipelineVO.getProcessorGroupId());
    }
    
    /**
     * Stop NiFi Pipeline
     * @param pipelineId using command history aop
     * @param userId using command history aop
     * @param commandId using command history aop
     * @param pipelineVO using command history aop
     * @return String
     * 
     * @since 2023. 8. 16
     * @version 1.2.0
     * @auther Justin
     */
    @CommandHistory(command = CommandStatusCode.COMMAND_STOP)
    private Boolean stopPipeline(Integer pipelineId, String userId, Integer commandId, PipelineVO pipelineVO) throws Exception{
        return niFiController.stopPipeline(commandId, pipelineVO.getProcessorGroupId());
    }

    
    public Boolean deletePipeline(Integer pipelineId, String userId){
        try{
            /* Delete Pipeline DB */
            pipelineMapper.deletePipeline(pipelineId, PipelineStatusCode.PIPELINE_STATUS_DELETED.getCode());
            PipelineVO pipelineVO = getPipelineVOById(pipelineId);
            changePipelineStatus(pipelineId, PipelineStatusCode.PIPELINE_STATUS_DELETING.getCode());
            /* Delete Execute NiFi Command */
            Boolean res = deleteNiFiPipeline(pipelineId, userId, null, pipelineVO);
            if (res){
                changePipelineStatus(pipelineId, PipelineStatusCode.PIPELINE_STATUS_DELETED.getCode());
                return true;
            } else {
                changePipelineStatus(pipelineId, PipelineStatusCode.PIPELINE_STATUS_FAILED.getCode());
                return false;
            }
        }
        catch (Exception e){
            log.error(" Delete Pipeline error processGroup Id : {}", pipelineId, e);
            return null;
        }
    }

    @CommandHistory(command = CommandStatusCode.COMMAND_DELETE)
    private Boolean deleteNiFiPipeline(Integer pipelineId, String userId, Integer commandId, PipelineVO pipelineVO) {
        return niFiController.deletePipeline(commandId, pipelineVO.getProcessorGroupId());
    }
    
    public Boolean updatePipeline(Integer pipelineId, PipelineVO pipelineVO, String userId) {
        
        changePipelineStatus(pipelineId, PipelineStatusCode.PIPELINE_STATUS_UPDATING.getCode());

        String processorGroupId = updateNiFiPipeline(pipelineVO.getId(), pipelineVO.getCreator(),null, pipelineVO);
        
        if (processorGroupId != null) {
            int result = pipelineMapper.updatePipeline(pipelineVO);
            if (result != 1) {
                pipelineMapper.changePipelineStatus(pipelineId, PipelineStatusCode.PIPELINE_STATUS_FAILED.getCode());
                return false;
            } else {
                pipelineMapper.changePipelineStatus(pipelineId, PipelineStatusCode.PIPELINE_STATUS_STOPPED.getCode());
                return true;
            }
        } else {
            pipelineMapper.changePipelineStatus(pipelineId, PipelineStatusCode.PIPELINE_STATUS_FAILED.getCode());
            return false;
        }
    }

    @CommandHistory(command = CommandStatusCode.COMMAND_UPDATE)
    private String updateNiFiPipeline(Integer pipelineId, String userId, Integer commandId, PipelineVO pipelineVO) {
        String processorGroupId = niFiController.updatePipeline( commandId, pipelineVO);
        return processorGroupId;
    }

    public PipelineVO getPipelineProperties(
        Integer pipelineid,
        String page,
        String adaptorName,
        String datasetid
    ) {
        PipelineVO pipelineVO = getPipelineVOById(pipelineid);
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
                if (Boolean.TRUE.equals(dataModelVO.getAttributes().get(i).getHasUnitCode())) {
                    PropertyVO propertyVO = new PropertyVO();
                    propertyVO.setName(dataModelVO.getAttributes().get(i).getName());
                    propertyVO.setDetail("unitCode");
                    niFiComponentVO.getRequiredProps().add(propertyVO);
                }
                if (dataModelVO.getAttributes().get(i).getValueType().equals("Date")) {
                    PropertyVO propertyVO = new PropertyVO();
                    propertyVO.setName(dataModelVO.getAttributes().get(i).getName());
                    propertyVO.setDetail("Date Format");
                    niFiComponentVO.getRequiredProps().add(propertyVO);
                }
                if (dataModelVO.getAttributes().get(i).getValueType().equals("GeoJson")) {
                    PropertyVO propertyVO = new PropertyVO();
                    propertyVO.setName(dataModelVO.getAttributes().get(i).getName());
                    propertyVO.setDetail("GeoType");
                    ArrayList<String> geoType = new ArrayList<String>();
                    geoType.add("Point");
                    geoType.add("LineString");
                    geoType.add("Polygon");
                    geoType.add("MultiPoint");
                    geoType.add("MultiLineString");
                    geoType.add("MultiPolygon");
                    propertyVO.setDefaultValue(geoType);
                    niFiComponentVO.getRequiredProps().add(propertyVO);
                }
                PropertyVO propertyVO = new PropertyVO();
                propertyVO.setName(dataModelVO.getAttributes().get(i).getName());
                propertyVO.setType(dataModelVO.getAttributes().get(i).getValueType());
                propertyVO.setDetail(dataModelVO.getAttributes().get(i).getAttributeType());
                niFiComponentVO.getRequiredProps().add(propertyVO);
                niFiComponentVO.setName("DataSetProps");
                niFiComponentVO.setType("Processor");
            }
            adaptorVO.getNifiComponents().add(niFiComponentVO);
            pipelineVO.setConverter(adaptorVO);
        } else {
            return null;
        }
        return pipelineVO;
    }

    public Boolean isExists(Integer id) {
        return pipelineMapper.isExists(id);
    }
}
