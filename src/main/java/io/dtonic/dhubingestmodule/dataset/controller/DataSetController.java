package io.dtonic.dhubingestmodule.dataset.controller;

import io.dtonic.dhubingestmodule.dataset.service.DataSetSVC;
import io.dtonic.dhubingestmodule.dataset.vo.DataModelVO;
import io.dtonic.dhubingestmodule.dataset.vo.DataSetListResponseVO;
import io.dtonic.dhubingestmodule.dataset.vo.DataSetPropertiesResponseVO;
import io.dtonic.dhubingestmodule.dataset.vo.DataSetResponseVO;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DataSetController {

    @Autowired
    private DataSetSVC datasetsvc;

    /**
     * Retrieve dataset list
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param accept request accept header
     * @throws Exception retrieve error
     */
    @GetMapping(value = "/datasets")
    public DataSetResponseVO getDatasets(HttpServletRequest request, HttpServletResponse response)
        throws Exception {
        DataSetResponseVO datasetList = datasetsvc.getDataSetList();

        return datasetList;
    }

    /**
     * Retrieve datamodel attributies
     * @param request
     * @param response
     * @param datasetid
     * @return
     * @throws Exception
     */
    @GetMapping(value = "/properties/{datasetid}")
    public DataModelVO getDatasetProperties(
        HttpServletRequest request,
        HttpServletResponse response,
        @PathVariable String datasetid
    )
        throws Exception {
        DataModelVO dataModelVO = datasetsvc.getDataModelId( //model ID 가져오기
            datasetid
        );
        dataModelVO = datasetsvc.getDataModelProperties(dataModelVO.getId()); // 해당 Model의 속성값 가져오기

        return dataModelVO;
    }
}
