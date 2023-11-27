package io.dtonic.dhubingestmodule.dataset.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Attribute extends ObjectMember {

    private String attributeType;
    private Boolean hasObservedAt;
    private Boolean hasUnitCode;
    private String attributeUri;
}
