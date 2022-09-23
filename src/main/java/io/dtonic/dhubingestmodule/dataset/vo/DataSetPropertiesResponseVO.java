package io.dtonic.dhubingestmodule.dataset.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataSetPropertiesResponseVO {

    private String datasetId;
    private String datamodelId;
    private List<AttributeVO> attribute;
}
