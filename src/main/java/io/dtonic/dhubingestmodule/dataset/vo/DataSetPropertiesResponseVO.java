package io.dtonic.dhubingestmodule.dataset.vo;

import lombok.Data;

@Data
public class DataSetPropertiesResponseVO {

    private String name;
    private Boolean isRequired;
    private String valueType;
    private String attributeType;
}
