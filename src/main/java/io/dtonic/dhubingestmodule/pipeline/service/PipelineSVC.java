package io.dtonic.dhubingestmodule.pipeline.service;

import io.dtonic.dhubingestmodule.common.code.DataCoreUiCode;
import io.dtonic.dhubingestmodule.common.code.PipelineStatusCode;
import io.dtonic.dhubingestmodule.common.exception.BadRequestException;
import io.dtonic.dhubingestmodule.dataset.service.DataSetSVC;
import io.dtonic.dhubingestmodule.dataset.vo.DataModelVO;
import io.dtonic.dhubingestmodule.nifi.controller.NiFiController;
import io.dtonic.dhubingestmodule.nifi.vo.AdaptorVO;
import io.dtonic.dhubingestmodule.nifi.vo.NiFiComponentVO;
import io.dtonic.dhubingestmodule.nifi.vo.PropertyVO;
import io.dtonic.dhubingestmodule.pipeline.mapper.PipelineDraftMapper;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Slf4j
public class PipelineSVC {

    @Autowired
    private PipelineMapper pipelineMapper;

    @Autowired
    private PipelineDraftSVC pipelineDraftSVC;

    @Autowired
    private DataSetSVC dataSetSVC;

    @Autowired
    private PipelineDraftMapper pipelineDraftMapper;

    @Autowired
    private NiFiController niFiController;

    @Transactional
    public void createPipeline(Integer id, PipelineVO pipelineVO) {
        String processorGroupId = niFiController.createPipeline(pipelineVO);
        if (!ValidateUtil.isStringEmpty(processorGroupId)) {
            JSONObject jsonObject = new JSONObject(pipelineVO);
            int result = pipelineMapper.createPipeline(
                jsonObject.getString("creator"),
                jsonObject.getString("name"),
                jsonObject.getString("detail"),
                "Stopped",
                jsonObject.getString("dataSet"),
                jsonObject.getString("dataModel"),
                jsonObject.getJSONObject("collector").toString(),
                jsonObject.getJSONObject("filter").toString(),
                jsonObject.getJSONObject("converter").toString(),
                processorGroupId
            );
            if (result != 1) {
                throw new BadRequestException(
                    DataCoreUiCode.ErrorCode.BAD_REQUEST,
                    "It's not created Pipeline in DB"
                );
            }
            //임시 파이프라인 삭제
            pipelineDraftSVC.deletePipelineDrafts(id);
        }
    }

    @Transactional
    public List<PipelineListResponseVO> getPipelineList() {
        List<PipelineListResponseVO> pipelineListVOs = pipelineMapper.getPipelineList();
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
                                nifiStatus.get("Stopped") == 0 &&
                                nifiStatus.get("Invaild") == 0
                            ) {
                                pipeline.setStatus(
                                    PipelineStatusCode.PIPELINE_STATUS_RUN.getCode()
                                );
                            } else if ( //case: DB=Stopping, Nifi=Stopped
                                curStatus.equals(
                                    PipelineStatusCode.PIPELINE_STATUS_STOPPING.getCode()
                                ) &&
                                nifiStatus.get("Running") == 0 &&
                                nifiStatus.get("Invaild") == 0
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
        return pipelineListVOs;
    }

    public PipelineVO getPipelineVOById(Integer id) {
        PipelineVO result = pipelineMapper.getPipeline(id);
        if (ValidateUtil.isEmptyData(result)) {
            throw new BadRequestException(
                DataCoreUiCode.ErrorCode.NOT_EXIST_ENTITY,
                "Pipeline Not Exist"
            );
        }
        return result;
    }

    @Transactional
    public void changePipelineStatus(Integer id, String status) {
        PipelineVO pipelineVO = getPipelineVOById(id);
        Boolean Nifiresult = false;
        if (status.equals(PipelineStatusCode.PIPELINE_STATUS_STARTING.getCode())) {
            Nifiresult = niFiController.runPipeline(pipelineVO.getProcessorGroupId());
        } else if (status.equals(PipelineStatusCode.PIPELINE_STATUS_STOPPING.getCode())) {
            Nifiresult = niFiController.stopPipeline(pipelineVO.getProcessorGroupId());
        }
        if (Nifiresult) {
            int result = pipelineMapper.changePipelineStatus(id, status);
            if (result != 1) {
                throw new BadRequestException(
                    DataCoreUiCode.ErrorCode.BAD_REQUEST,
                    "Change Pipeline Status Error in DB"
                );
            }
        }
    }

    @Transactional
    public void deletePipeline(Integer id) {
        PipelineVO pipelineVO = getPipelineVOById(id);
        if (niFiController.deletePipeline(pipelineVO.getProcessorGroupId())) {
            int result = pipelineMapper.deletePipeline(id);
            if (result != 1) {
                throw new BadRequestException(
                    DataCoreUiCode.ErrorCode.BAD_REQUEST,
                    "Delete Pipeline Error in DB"
                );
            }
        }
    }

    @Transactional
    public void updatePipeline(Integer id, PipelineVO pipelineVO) {
        String processorGroupId = niFiController.updatePipeline(pipelineVO);
        if (ValidateUtil.isStringEmpty(processorGroupId)) {
            JSONObject jsonObject = new JSONObject(pipelineVO);
            int result = pipelineMapper.updatePipeline(
                jsonObject.getInt("id"),
                jsonObject.getString("name"),
                jsonObject.getString("detail"),
                jsonObject.getString("dataSet"),
                jsonObject.getString("dataModel"),
                processorGroupId,
                jsonObject.getJSONObject("collector").toString(),
                jsonObject.getJSONObject("filter").toString(),
                jsonObject.getJSONObject("converter").toString()
            );

            if (result != 1) {
                throw new BadRequestException(
                    DataCoreUiCode.ErrorCode.CREATE_ENTITY_TABLE_ERROR,
                    "Update Pipeline Error"
                );
            }
        }
    }

    public PipelineVO getPipelineProperties(
        Integer pipelineid,
        String page,
        String adaptorName,
        String datasetid
    ) {
        PipelineVO pipelineVO = getPipelineVOById(pipelineid);
        AdaptorVO adaptorVO = getPipelineproperties(adaptorName);
        switch (page) {
            case "collector": //수집기 선택시 (수집 pipelineVO 속성 리턴)
                pipelineVO.setCollector(adaptorVO);
                break;
            case "filter": //수집에서 다음 누를때(정제 pipelineVO 속성 리턴)
                pipelineVO.setFilter(adaptorVO);
                break;
            case "converter": //데이터셋 선택시 (변환 pipelineVO 속성 리턴)
                DataModelVO dataModelVO = dataSetSVC.getDataModelId( //model ID 가져오기
                    datasetid
                );
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
                break;
            default:
                throw new BadRequestException(
                    DataCoreUiCode.ErrorCode.BAD_REQUEST,
                    "Page name Error"
                );
        }

        return pipelineVO;
    }

    //adaptor의 속성값 가져오기
    public AdaptorVO getPipelineproperties(String adaptorName) {
        NiFiComponentVO niFiComponentVO = new NiFiComponentVO();
        List<NiFiComponentVO> niFiComponentVOs = new ArrayList<NiFiComponentVO>();
        List<PropertyVO> propertyVO = pipelineDraftMapper.getPipelineproperties(adaptorName);
        AdaptorVO adaptorVO = new AdaptorVO();
        Integer cur_adaptor_id = propertyVO.get(0).getAdaptorId();
        for (int i = 0; i < propertyVO.size(); i++) {
            if (propertyVO.get(i).getAdaptorId() != cur_adaptor_id) { //Processor property값을 모두 불러왔을 시
                niFiComponentVO.setName(
                    pipelineDraftMapper.getAdaptorinfo(cur_adaptor_id).getName()
                );
                niFiComponentVO.setType(
                    pipelineDraftMapper.getAdaptorinfo(cur_adaptor_id).getType()
                );
                cur_adaptor_id = propertyVO.get(i).getAdaptorId();
                niFiComponentVOs.add(niFiComponentVO);
                niFiComponentVO = new NiFiComponentVO();
            }
            if (propertyVO.get(i).getIsRequired()) {
                if (
                    propertyVO.get(i).getDefaultValue().size() > 0 &&
                    ValidateUtil.isStringEmpty(propertyVO.get(i).getInputValue())
                ) {
                    propertyVO.get(i).setInputValue(propertyVO.get(i).getDefaultValue().get(0));
                }
                niFiComponentVO.getRequiredProps().add(propertyVO.get(i));
            } else {
                niFiComponentVO.getOptionalProps().add(propertyVO.get(i));
            }
            if (i == propertyVO.size() - 1) {
                niFiComponentVO.setName(
                    pipelineDraftMapper.getAdaptorinfo(cur_adaptor_id).getName()
                );
                niFiComponentVO.setType(
                    pipelineDraftMapper.getAdaptorinfo(cur_adaptor_id).getType()
                );
            }
        }
        niFiComponentVOs.add(niFiComponentVO);
        adaptorVO.setNifiComponents(niFiComponentVOs);
        adaptorVO.setName(adaptorName);
        return adaptorVO;
    }

    public Boolean isExists(Integer id) {
        return pipelineMapper.isExists(id);
    }
}
