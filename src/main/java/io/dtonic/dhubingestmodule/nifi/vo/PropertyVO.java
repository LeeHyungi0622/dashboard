package io.dtonic.dhubingestmodule.nifi.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PropertyVO {

    private String name;
    private String detail;
    private Boolean isRequired;
    private List<String> defaultValue = new ArrayList<>();
    private String inputValue;
}
