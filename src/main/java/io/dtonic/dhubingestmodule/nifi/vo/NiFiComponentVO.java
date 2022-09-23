package io.dtonic.dhubingestmodule.nifi.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NiFiComponentVO {

    private String type;
    private String name;
    private List<PropertyVO> properties = new ArrayList<>();
}
