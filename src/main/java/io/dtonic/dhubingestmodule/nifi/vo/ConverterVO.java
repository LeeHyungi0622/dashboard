package io.dtonic.dhubingestmodule.nifi.vo;

import java.util.List;
import lombok.Data;

@Data
public class ConverterVO {

    private String datasetId;
    private List<Object> entities;
}
