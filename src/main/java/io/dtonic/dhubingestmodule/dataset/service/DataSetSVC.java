package io.dtonic.dhubingestmodule.dataset.service;

import io.dtonic.dhubingestmodule.common.component.Properties;
import io.dtonic.dhubingestmodule.common.service.DataCoreRestSVC;
import io.dtonic.dhubingestmodule.dataset.vo.DataModelVO;
import io.dtonic.dhubingestmodule.dataset.vo.DataSetForDataModelIDVO;
import io.dtonic.dhubingestmodule.dataset.vo.DataSetListBaseInfoVO;
import io.dtonic.dhubingestmodule.dataset.vo.DataSetListResponseVO;
import io.dtonic.dhubingestmodule.dataset.vo.DataSetResponseVO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class DataSetSVC {

    @Autowired
    private Properties properties;

    private static final String DEFAULT_PATH_URL = "datasets";
    private final DataCoreRestSVC dataCoreRestSVC;

    //DataSet List 조회
    public DataSetResponseVO getDataSetList() throws Exception{
        List<String> pathUri = new ArrayList<>();
        pathUri.add(DEFAULT_PATH_URL);
        DataSetListResponseVO dataSetListResponseVO = new DataSetListResponseVO();
        Map<String, String> header = new HashMap<String, String>();
        String datasetUrl = properties.getDatacoreManagerUrl();
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
    public DataModelVO getDataModelId(String DataSetId) {
        String datasetUrl = properties.getDatacoreManagerUrl();
        List<String> pathUri = new ArrayList<>();
        pathUri.add("/datasets");
        pathUri.add(DataSetId);
        Map<String, String> header = new HashMap<String, String>();
        header.put("Accept", "application/json");
        DataModelVO dataModelVO = new DataModelVO();

        dataModelVO.setDatasetId(DataSetId);
        ResponseEntity<DataSetForDataModelIDVO> response = dataCoreRestSVC.get(
            datasetUrl,
            pathUri,
            header,
            null,
            null,
            null,
            DataSetForDataModelIDVO.class
        );
        
        if (response != null) dataModelVO.setId(
            response.getBody().getDatasetBaseInfo().getDataModelId()
        ); else dataModelVO.setId(null);
        return dataModelVO;
    }

    //ModelId를 통한 Model Properties 조회
    public DataModelVO getDataModelProperties(String id) {
        String datamodelUrl = properties.getDatacoreManagerUrl();
        List<String> pathUri = new ArrayList<>();
        pathUri.add("/datamodels");
        pathUri.add(id);

        Map<String, String> header = new HashMap<String, String>();

        header.put("Accept", "application/json");
        ResponseEntity<DataModelVO> response = dataCoreRestSVC.get(
            datamodelUrl,
            pathUri,
            header,
            null,
            null,
            null,
            DataModelVO.class
        );
        DataModelVO dataModelVO = response.getBody();
        return dataModelVO;
    }
}
