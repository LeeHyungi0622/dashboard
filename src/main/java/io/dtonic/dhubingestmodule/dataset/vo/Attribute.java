package io.dtonic.dhubingestmodule.dataset.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.dtonic.dhubingestmodule.common.code.DataCoreUiCode.AttributeType;
import java.util.List;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Attribute extends ObjectMember {

    private String attributeType;
    private Boolean hasObservedAt;
    private Boolean hasUnitCode;
    private String attributeUri;
}
