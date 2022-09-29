package io.dtonic.dhubingestmodule.pipeline.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import io.dtonic.dhubingestmodule.pipeline.vo.DataCollectorVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineListResponseVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineVO;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
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

    @Autowired
    private ObjectMapper objectMapper;

    public void createPipeline(Integer id, PipelineVO pipelineVO) {
        // String processorGroupId = niFiController.createPipeline(pipelineVO);
        String processorGroupId = "Test";
        JSONObject jsonObject = new JSONObject(pipelineVO);
        int result = pipelineMapper.createPipeline(
            jsonObject.getString("creator"),
            jsonObject.getString("name"),
            jsonObject.getString("detail"),
            "Stopped",
            jsonObject.getString("dataSet"),
            jsonObject.getJSONObject("collector").toString(),
            jsonObject.getJSONObject("filter").toString(),
            jsonObject.getJSONObject("converter").toString(),
            processorGroupId
        );
        //임시 파이프라인 삭제
        if (result == 1) {
            //pipelineDraftSVC.deletePipelineDrafts(id);
        } else {
            throw new BadRequestException(
                DataCoreUiCode.ErrorCode.CREATE_ENTITY_TABLE_ERROR,
                "Create Pipeline Error"
            );
        }
    }

    // private final NiFoControlSVC nificontrolSVC;
    @Transactional
    public List<PipelineListResponseVO> getPipelineList() {
        List<PipelineListResponseVO> pipelineListVOs = pipelineMapper.getPipelineList();
        for (int i = 0; i < pipelineListVOs.size(); i++) {
            String temp_status = "Starting"; // Nifi API Response stats 값 가져와서 (현재 임시값)
            String cur_status = pipelineListVOs.get(i).getStatus(); // 현재 DB status값
            if (
                cur_status.equals(PipelineStatusCode.PIPELINE_STATUS_STARTING.getCode()) ||
                cur_status.equals(PipelineStatusCode.PIPELINE_STATUS_STOPPING.getCode())
            ) { // DB 상태 Starting or Stopping 경우 상태변화
                // 일어남
                if (
                    cur_status.equals(PipelineStatusCode.PIPELINE_STATUS_STARTING.getCode()) &&
                    temp_status.equals(PipelineStatusCode.PIPELINE_NIFISTATUS_RUNNING.getCode())
                ) pipelineListVOs
                    .get(i)
                    .setStatus(PipelineStatusCode.PIPELINE_STATUS_RUN.getCode()); else if ( // DB상태 Run // DB상태 Starting Nifi 상태 Running =>
                    cur_status.equals(PipelineStatusCode.PIPELINE_STATUS_STOPPING.getCode()) &&
                    temp_status.equals(PipelineStatusCode.PIPELINE_NIFISTATUS_STOP.getCode())
                ) pipelineListVOs
                    .get(i)
                    .setStatus(PipelineStatusCode.PIPELINE_STATUS_STOPPED.getCode()); // DB상태 Stopped // DB상태 Stopping Nifi 상태 Stop =>
                changePipelineStatus(
                    pipelineListVOs.get(i).getId(),
                    pipelineListVOs.get(i).getStatus()
                );
            }
        }
        return pipelineListVOs;
    }

    public PipelineVO getPipelineVOById(Integer id) {
        PipelineVO result = pipelineMapper.getPipeline(id);
        if (result == null) {
            throw new BadRequestException(
                DataCoreUiCode.ErrorCode.NOT_EXIST_ENTITY,
                "Pipeline Not Exist"
            );
        }
        return result;
    }

    @Transactional
    public void changePipelineStatus(Integer id, String status) {
        //NifiAPI
        // PipelineVO pipelineVO = getPipelineVOById(id);
        // if (status.equals(PipelineStatusCode.PIPELINE_STATUS_STARTING.getCode())) {
        //     niFiController.runPipeline(pipelineVO.getProcessorGroupId());
        // } else if (status.equals(PipelineStatusCode.PIPELINE_STATUS_STOPPING.getCode())) {
        //     niFiController.stopPipeline(pipelineVO.getProcessorGroupId());
        // }
        int result = pipelineMapper.changePipelineStatus(id, status);
        if (result != 1) {
            throw new BadRequestException(
                DataCoreUiCode.ErrorCode.BAD_REQUEST,
                "Change Pipeline Status Error"
            );
        }
    }

    @Transactional
    public void deletePipeline(Integer id) {
        //NifiAPI
        // PipelineVO pipelineVO = getPipelineVOById(id);
        // niFiController.deletePipeline(pipelineVO.getProcessorGroupId());
        int result = pipelineMapper.deletePipeline(id);
        if (result != 1) {
            throw new BadRequestException(
                DataCoreUiCode.ErrorCode.BAD_REQUEST,
                "Delete Pipeline Error"
            );
        }
    }

    public Boolean isExists(Integer id) {
        return pipelineMapper.isExists(id);
    }

    @Transactional
    public void updatePipeline(Integer id, PipelineVO pipelineVO) {
        //String processorGroupId = niFiController.updatePipeline(pipelineVO);
        String processorGroupId = "Test";
        JSONObject jsonObject = new JSONObject(pipelineVO);
        int result = pipelineMapper.updatePipeline(
            jsonObject.getInt("id"),
            jsonObject.getString("name"),
            jsonObject.getString("detail"),
            jsonObject.getString("dataSet"),
            processorGroupId,
            jsonObject.getJSONObject("collector").toString(),
            jsonObject.getJSONObject("filter").toString(),
            jsonObject.getJSONObject("converter").toString()
        );
        //임시 파이프라인 삭제
        if (result == 1) {
            pipelineDraftSVC.deletePipelineDrafts(id);
        } else {
            throw new BadRequestException(
                DataCoreUiCode.ErrorCode.CREATE_ENTITY_TABLE_ERROR,
                "Create Pipeline Error"
            );
        }
    }

    public PipelineVO getPipelineProperties(
        Integer pipelineid,
        Integer page,
        String adaptorName,
        String datasetid
    ) {
        PipelineVO pipelineVO = pipelineMapper.getPipeline(pipelineid);
        AdaptorVO adaptorVO = getPipelineproperties(adaptorName);
        switch (page) {
            case 1: //수집기 선택시 (수집 pipelineVO 속성 리턴)
                pipelineVO.setCollector(adaptorVO);
                break;
            case 2: //수집에서 다음 누를때(정제 pipelineVO 속성 리턴)
                pipelineVO.setFilter(adaptorVO);
                break;
            case 3: //데이터셋 선택시 (변환 pipelineVO 속성 리턴)
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
            if (propertyVO.get(i).getAdaptorId() != cur_adaptor_id) {
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
                    isStringEmpty(propertyVO.get(i).getInputValue())
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

    static boolean isStringEmpty(String str) {
        return str == null || str.isEmpty();
    }
}
