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

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PipelineSVC {

    @Autowired
    private PipelineMapper pipelineMapper;

    @Autowired
    private PipelineDraftSVC pipelineDraftSVC;

    @Autowired
    private DataSetSVC dataSetSVC;

    @Autowired
    private NiFiController niFiController;
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
    public PipelineVO createPipeline(Integer pipelineId, String userId, Integer commandId, PipelineVO pipelineVO, Integer temperalId) {
        /* Insert Row Pipeline Info */
        pipelineMapper.createPipeline(pipelineVO);
        /* Excute NiFi Create Command */
        String processorGroupId = niFiController.createPipeline(pipelineVO, commandId);
        
        if (processorGroupId != null) {
            /* Delete Row Temperal Pipeline Info */
            Boolean tmpRes = pipelineDraftSVC.deletePipelineDrafts(temperalId);
            if (!tmpRes) {
                log.error("Fail to Delete Temperal Pipeline ID : [{}]", temperalId);
                return null;
            }
            pipelineMapper.updatePipelineProcessgroupId(PipelineStatusCode.PIPELINE_STATUS_STOPPED.getCode(), pipelineVO.getId(), processorGroupId);
            return pipelineVO;
        } else {
            pipelineMapper.changePipelineStatus(pipelineVO.getId(), PipelineStatusCode.PIPELINE_STATUS_FAILED.getCode());
            return null;
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
    @CommandHistory(command = CommandStatusCode.COMMAND_DUPLICATE)
    public PipelineVO duplicatePipeline(Integer pipelineId, String userId, Integer commandId, PipelineVO pipelineVO) {
        /* Insert Row Pipeline Info */
        pipelineMapper.createPipeline(pipelineVO);
        /* Excute NiFi Create Command */
        String processorGroupId = niFiController.createPipeline(pipelineVO, commandId);
    
        if (processorGroupId != null) {
            /* Delete Row Temperal Pipeline Info */
            pipelineMapper.updatePipelineProcessgroupId(PipelineStatusCode.PIPELINE_STATUS_STOPPED.getCode(), pipelineVO.getId(), processorGroupId);
            return pipelineVO;
        } else {
            pipelineMapper.changePipelineStatus(pipelineVO.getId(), PipelineStatusCode.PIPELINE_STATUS_FAILED.getCode());
            return null;
        }
    }
 

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
    public PipelineVO runPipeline(Integer pipelineId, String userId, Integer commandId){
        PipelineVO pipelineVO = getPipelineVOById(pipelineId);
        Boolean res = niFiController.runPipeline(commandId, pipelineVO.getProcessorGroupId());
        if (res) {
            pipelineMapper.changePipelineStatus(pipelineId, PipelineStatusCode.PIPELINE_STATUS_RUN.getCode());
            return pipelineVO;
        } else {
            pipelineMapper.changePipelineStatus(pipelineId, PipelineStatusCode.PIPELINE_STATUS_FAILED.getCode());
            return null;
        }
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
    public PipelineVO stopPipeline(Integer pipelineId, String userId, Integer commandId){
        PipelineVO pipelineVO = getPipelineVOById(pipelineId);
        Boolean res = niFiController.stopPipeline(commandId, pipelineVO.getProcessorGroupId());
        if (res) {
            pipelineMapper.changePipelineStatus(pipelineId, PipelineStatusCode.PIPELINE_STATUS_STOPPED.getCode());
            return pipelineVO;
        } else {
            pipelineMapper.changePipelineStatus(pipelineId, PipelineStatusCode.PIPELINE_STATUS_FAILED.getCode());
            return null;
        }
    }

    @CommandHistory(command = CommandStatusCode.COMMAND_DELETE)
    public PipelineVO deletePipeline(Integer pipelineId, String userId, Integer commandId){
        try{
            PipelineVO pipelineVO = getPipelineVOById(pipelineId);
            /* Delete Pipeline DB */
            pipelineMapper.changePipelineStatus(pipelineId, PipelineStatusCode.PIPELINE_STATUS_DELETING.getCode());
            /* Delete Execute NiFi Command */
            Boolean res = niFiController.deletePipeline(commandId, pipelineVO.getProcessorGroupId());
            if (res){
                pipelineMapper.changePipelineStatus(pipelineId, PipelineStatusCode.PIPELINE_STATUS_DELETED.getCode());
                return pipelineVO;
            } else {
                pipelineMapper.changePipelineStatus(pipelineId, PipelineStatusCode.PIPELINE_STATUS_FAILED.getCode());
                return null;
            }
        }
        catch (Exception e){
            log.error(" Delete Pipeline error processGroup Id : {}", pipelineId, e);
            return null;
        }
    }



    @CommandHistory(command = CommandStatusCode.COMMAND_UPDATE)
    public PipelineVO updatePipeline(Integer pipelineId, String userId, Integer commandId, PipelineVO pipelineVO) {
        
        pipelineMapper.changePipelineStatus(pipelineId, PipelineStatusCode.PIPELINE_STATUS_UPDATING.getCode());

        String processorGroupId = niFiController.updatePipeline( commandId, pipelineVO);
        
        if (processorGroupId != null) {
            int result = pipelineMapper.updatePipeline(pipelineVO);
            if (result != 1) {
                pipelineMapper.changePipelineStatus(pipelineId, PipelineStatusCode.PIPELINE_STATUS_FAILED.getCode());
                return null;
            } else {
                pipelineMapper.changePipelineStatus(pipelineId, PipelineStatusCode.PIPELINE_STATUS_STOPPED.getCode());
                return pipelineVO;
            }
        } else {
            pipelineMapper.changePipelineStatus(pipelineId, PipelineStatusCode.PIPELINE_STATUS_FAILED.getCode());
            return null;
        }
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
