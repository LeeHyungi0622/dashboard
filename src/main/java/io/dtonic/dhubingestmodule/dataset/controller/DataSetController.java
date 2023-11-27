package io.dtonic.dhubingestmodule.dataset.controller;

import io.dtonic.dhubingestmodule.dataset.service.DataSetSVC;
import io.dtonic.dhubingestmodule.dataset.vo.DataModelVO;
import io.dtonic.dhubingestmodule.dataset.vo.DataSetResponseVO;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
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
    @GetMapping(value = "/datasets/list")
    public ResponseEntity<DataSetResponseVO> getDatasets(HttpServletRequest request, HttpServletResponse response)
         {
            DataSetResponseVO dataSetResponseVO = new DataSetResponseVO();
            try {
                dataSetResponseVO = datasetsvc.getDataSetList();
                return ResponseEntity.ok(dataSetResponseVO);
            } catch (Exception e) {
                log.error("Fail to Get Dataset list from Datacore Manager", e);
                return ResponseEntity.badRequest().body(dataSetResponseVO);
            }
    }

    /**
     * Retrieve datamodel attributies
     * @param request
     * @param response
     * @param datasetid
     * @return
     * @throws Exception
     */
    @GetMapping(value = "/dataset/properties/{datasetId}")
    public ResponseEntity<DataModelVO> getDatasetProperties(
        HttpServletRequest request,
        HttpServletResponse response,
        @PathVariable String datasetId
    )
        {
        DataModelVO dataModelVO = new DataModelVO();
        try {
            dataModelVO = datasetsvc.getDataModelId(datasetId);
            dataModelVO = datasetsvc.getDataModelProperties(dataModelVO.getId());
            return ResponseEntity.ok(dataModelVO);
        } catch (Exception e) {
            log.error("Fail to Get Dataset Properties from Datacore Manager", e);
            return ResponseEntity.badRequest().body(dataModelVO);
        }

    }
}
