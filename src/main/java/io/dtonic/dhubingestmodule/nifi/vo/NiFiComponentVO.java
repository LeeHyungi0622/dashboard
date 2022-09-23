package io.dtonic.dhubingestmodule.nifi.vo;

import java.util.List;
import lombok.Data;

@Data
public class NiFiComponentVO {

    private String type;
    private String name;
    private List<PropertyVO> properties;
}
