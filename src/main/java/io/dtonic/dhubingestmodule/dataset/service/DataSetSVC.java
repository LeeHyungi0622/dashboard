package io.dtonic.dhubingestmodule.dataset.service;

import com.google.gson.Gson;
import io.dtonic.dhubingestmodule.common.component.Properties;
import io.dtonic.dhubingestmodule.common.service.DataCoreRestSVC;
import io.dtonic.dhubingestmodule.dataset.vo.DataModelResponseVO;
import io.dtonic.dhubingestmodule.dataset.vo.DataSetForDataModelIDVO;
import io.dtonic.dhubingestmodule.dataset.vo.DataSetListBaseInfoVO;
import io.dtonic.dhubingestmodule.dataset.vo.DataSetListResponseVO;
import io.dtonic.dhubingestmodule.dataset.vo.DataSetPropertiesResponseVO;
import io.dtonic.dhubingestmodule.dataset.vo.DataSetResponseVO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class DataSetSVC {

    @Autowired
    private Properties properties;

    private static final String DEFAULT_PATH_URL = "datasets";
    private final DataCoreRestSVC dataCoreRestSVC;

    //DataSet List 조회
    public DataSetResponseVO getDataSetList() {
        String pathUri = DEFAULT_PATH_URL;
        DataSetListResponseVO dataSetListResponseVO = new DataSetListResponseVO();
        Map<String, String> header = new HashMap<String, String>();
        String datasetUrl = properties.getDatasetUrl();
        List<String> DataSetId = new ArrayList<>();

        DataSetResponseVO dataSetResponseVO = new DataSetResponseVO();

        header.put("Accept", "application/json");

        ResponseEntity<List<DataSetListBaseInfoVO>> response = dataCoreRestSVC.getList(
            datasetUrl,
            pathUri,
            header,
            null,
            null,
            new ParameterizedTypeReference<List<DataSetListBaseInfoVO>>() {}
        );
        if (response != null) dataSetListResponseVO.setDataSetResponseVO(response.getBody());

        for (int i = 0; i < dataSetListResponseVO.getDataSetResponseVO().size(); i++) DataSetId.add(
            dataSetListResponseVO.getDataSetResponseVO().get(i).getId()
        );

        dataSetResponseVO.setDataSetId(DataSetId);

        return dataSetResponseVO;
    }

    //dataset이 사용하는 Model ID 조회
    public DataSetPropertiesResponseVO getDataModelId(String DataSetId) {
        String datasetUrl = properties.getDatasetUrl();
        String pathUri = "/datasets/" + DataSetId;
        Map<String, String> header = new HashMap<String, String>();
        header.put("Accept", "application/json");
        DataSetPropertiesResponseVO dataSetPropertiesResponseVO = new DataSetPropertiesResponseVO();

        dataSetPropertiesResponseVO.setDatasetId(DataSetId);
        ResponseEntity<DataSetForDataModelIDVO> response = dataCoreRestSVC.get(
            datasetUrl,
            pathUri,
            header,
            null,
            null,
            DataSetForDataModelIDVO.class
        );
        if (response != null) dataSetPropertiesResponseVO.setDatamodelId(
            response.getBody().getDatasetBaseInfo().getDataModelId()
        );

        return dataSetPropertiesResponseVO;
    }

    //dataset이 사용하는 Model Properties 조회
    public DataSetPropertiesResponseVO getDataModelProperties(
        DataSetPropertiesResponseVO dataSetPropertiesResponseVO
    ) {
        String datamodelUrl = properties.getDatasetUrl();
        String pathUri = "/datamodels/" + dataSetPropertiesResponseVO.getDatamodelId();
        Map<String, String> header = new HashMap<String, String>();

        header.put("Accept", "application/json");
        ResponseEntity<DataModelResponseVO> response = dataCoreRestSVC.get(
            datamodelUrl,
            pathUri,
            header,
            null,
            null,
            DataModelResponseVO.class
        );

        if (response != null) dataSetPropertiesResponseVO.setAttribute(
            response.getBody().getAttributes()
        );

        return dataSetPropertiesResponseVO;
    }
}
