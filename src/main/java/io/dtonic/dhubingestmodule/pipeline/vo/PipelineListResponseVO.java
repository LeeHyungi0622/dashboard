package io.dtonic.dhubingestmodule.pipeline.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.dtonic.dhubingestmodule.common.code.Constants;

import java.util.Date;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.ALWAYS)
public class PipelineListResponseVO {

    private Integer id;
    private String name;
    private String dataSet;
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss z", timezone = Constants.CONTENT_DATE_TIMEZONE)
    private Date createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss z", timezone = Constants.CONTENT_DATE_TIMEZONE)
    private Date modifiedAt;
}
