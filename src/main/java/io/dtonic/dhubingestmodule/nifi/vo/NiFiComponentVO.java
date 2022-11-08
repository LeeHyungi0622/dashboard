package io.dtonic.dhubingestmodule.nifi.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.ALWAYS)
public class NiFiComponentVO {

    private String type;
    private String name;
    private List<PropertyVO> requiredProps = new ArrayList<>();
    private List<PropertyVO> optionalProps = new ArrayList<>();
}
