package io.dtonic.dhubingestmodule.pipeline.service;

import io.dtonic.dhubingestmodule.common.code.AdaptorName;
import io.dtonic.dhubingestmodule.common.code.DataCoreUiCode;
import io.dtonic.dhubingestmodule.common.code.NifiStatusCode;
import io.dtonic.dhubingestmodule.common.code.PipelineStatusCode;
import io.dtonic.dhubingestmodule.common.exception.BadRequestException;
import io.dtonic.dhubingestmodule.dataset.service.DataSetSVC;
import io.dtonic.dhubingestmodule.dataset.vo.DataModelVO;
import io.dtonic.dhubingestmodule.nifi.controller.NiFiController;
import io.dtonic.dhubingestmodule.nifi.vo.AdaptorVO;
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
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
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

    @Transactional
    public ResponseEntity createPipeline(Integer id, PipelineVO pipelineVO) {
        String processorGroupId = niFiController.createPipeline(pipelineVO);
        if (processorGroupId != null) {
            JSONObject jsonObject = new JSONObject(pipelineVO);
            int result = pipelineMapper.createPipeline(
                jsonObject.getString("creator"),
                jsonObject.getString("name"),
                jsonObject.getString("detail"),
                PipelineStatusCode.PIPELINE_STATUS_STOPPED.getCode(),
                jsonObject.getString("dataSet"),
                jsonObject.getString("dataModel"),
                jsonObject.getJSONObject(AdaptorName.ADAPTOR_NAME_COLLECTOR.getCode()).toString(),
                jsonObject.getJSONObject(AdaptorName.ADAPTOR_NAME_FILTER.getCode()).toString(),
                jsonObject.getJSONObject(AdaptorName.ADAPTOR_NAME_CONVERTER.getCode()).toString(),
                processorGroupId
            );
            if (result != 1) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("It's not created Pipeline in DB");
                // throw new BadRequestException(
                //     DataCoreUiCode.ErrorCode.BAD_REQUEST,
                //     "It's not created Pipeline in DB"
                //);
            }
            //임시 파이프라인 삭제
            pipelineDraftSVC.deletePipelineDrafts(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Nifi error");
        }
    }

    @Transactional
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
                    log.debug("{}", nifiStatus);
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
                            changePipelineStatus(pipeline.getId(), pipeline.getStatus());
                        }
                    }
                }
            );
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
    public ResponseEntity changePipelineStatus(Integer id, String status) {
        PipelineVO pipelineVO = (PipelineVO) getPipelineVOById(id).getBody();
        Boolean Nifiresult = false;
        if (status.equals(PipelineStatusCode.PIPELINE_STATUS_STARTING.getCode())) {
            Nifiresult = niFiController.runPipeline(pipelineVO.getProcessorGroupId());
        } else if (status.equals(PipelineStatusCode.PIPELINE_STATUS_STOPPING.getCode())) {
            Nifiresult = niFiController.stopPipeline(pipelineVO.getProcessorGroupId());
        } else {
            Nifiresult = true;
        }
        if (Nifiresult) {
            int result = pipelineMapper.changePipelineStatus(id, status);
            if (result != 1) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Change Pipeline Status Error in DB");
            } else {
                return ResponseEntity.ok().build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Nifi error");
        }
    }

    @Transactional
    public ResponseEntity deletePipeline(Integer id) {
        PipelineVO pipelineVO = (PipelineVO) getPipelineVOById(id).getBody();
        if (niFiController.deletePipeline(pipelineVO.getProcessorGroupId())) {
            int result = pipelineMapper.deletePipeline(id);
            if (result != 1) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Delete Pipeline Error in DB");
            } else {
                return ResponseEntity.ok().build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Nifi error");
        }
    }

    @Transactional
    public ResponseEntity updatePipeline(Integer id, PipelineVO pipelineVO) {
        String processorGroupId = niFiController.updatePipeline(pipelineVO);
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
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Update Pipeline Error");
            } else {
                return ResponseEntity.ok().build();
            }
        } else {
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
            return ResponseEntity.status(org.apache.http.HttpStatus.SC_BAD_REQUEST).body("invalid Page name");
        }
        return ResponseEntity.ok().body(pipelineVO);
    }

    public Boolean isExists(Integer id) {
        return pipelineMapper.isExists(id);
    }
}
