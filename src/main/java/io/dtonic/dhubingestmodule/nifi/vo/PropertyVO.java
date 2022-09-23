package io.dtonic.dhubingestmodule.nifi.vo;

import java.util.List;
import lombok.Data;

@Data
public class PropertyVO {

    private String name;
    private String detail;
    private Boolean isRequired;
    private List<String> defaultValue;
    private String inputValue;
}
