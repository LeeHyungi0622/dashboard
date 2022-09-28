package io.dtonic.dhubingestmodule.nifi.vo;

import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class ConverterVO {

    private String datasetId;
    private List<Map<String, String>> entities;
}
