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
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public void createPipeline(Integer id, PipelineVO pipelineVO) {
        String processorGroupId = niFiController.createPipeline(pipelineVO);
        if (!ValidateUtil.isStringEmpty(processorGroupId)) {
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
                                nifiStatus.get(NifiStatusCode.NIFI_STATUS_STOPPED.getCode()) == 0 &&
                                nifiStatus.get(NifiStatusCode.NIFI_STATUS_INVALID.getCode()) == 0
                            ) {
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
                jsonObject.getJSONObject(AdaptorName.ADAPTOR_NAME_COLLECTOR.getCode()).toString(),
                jsonObject.getJSONObject(AdaptorName.ADAPTOR_NAME_FILTER.getCode()).toString(),
                jsonObject.getJSONObject(AdaptorName.ADAPTOR_NAME_CONVERTER.getCode()).toString()
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
        AdaptorVO adaptorVO = pipelineDraftSVC.getPipelineproperties(adaptorName);
        if (page.equals(AdaptorName.ADAPTOR_NAME_COLLECTOR.getCode())) {
            pipelineVO.setCollector(adaptorVO);
        } else if (page.equals(AdaptorName.ADAPTOR_NAME_FILTER.getCode())) {
            pipelineVO.setFilter(adaptorVO);
        } else if (page.equals(AdaptorName.ADAPTOR_NAME_CONVERTER.getCode())) {
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
        }
        return pipelineVO;
    }

    public Boolean isExists(Integer id) {
        return pipelineMapper.isExists(id);
    }
}
