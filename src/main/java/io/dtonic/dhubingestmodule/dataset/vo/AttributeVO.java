package io.dtonic.dhubingestmodule.dataset.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import javax.print.event.PrintJobAdapter;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AttributeVO {

    /** rootAttribute ID */
    private String name;
    private String description;
    /** rootAttribute Uri */
    //private String attributeUri;
}
