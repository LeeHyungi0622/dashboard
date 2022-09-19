package io.dtonic.dhubingestmodule.pipeline.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PipelineCreateVO {

    private String creator;
    private String name;
    private String detail;
    private String status;
    private String data_set;
    private String collector;
    private String filter;
    private String converter;
    private Date createdAt;
    private Date modifiedAt;
}
