package io.dtonic.dhubingestmodule.dataset.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AttributeVO {

    /** rootAttribute ID */
    private String name;
    /** rootAttribute Uri */
    //private String attributeUri;
}
