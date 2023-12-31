package io.dtonic.dhubingestmodule.nifi.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PropertyVO {

    private Integer adaptorId;
    private String name;
    private String detail;
    private String type;
    private Boolean isRequired;
    private List<String> defaultValue = new ArrayList<>();
    private String inputValue;
}
