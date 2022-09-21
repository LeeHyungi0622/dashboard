package io.dtonic.dhubingestmodule.dataset.service;

import com.google.gson.Gson;
import io.dtonic.dhubingestmodule.common.component.Properties;
import io.dtonic.dhubingestmodule.common.service.DataCoreRestSVC;
import io.dtonic.dhubingestmodule.dataset.vo.DataSetListBaseInfoVO;
import io.dtonic.dhubingestmodule.dataset.vo.DataSetListResponseVO;
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

    public DataSetResponseVO getDataSets() {
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
}
