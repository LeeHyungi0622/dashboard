package io.dtonic.dhubingestmodule.pipeline.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.dtonic.dhubingestmodule.common.code.Constants;
import io.dtonic.dhubingestmodule.nifi.vo.AdaptorVO;
import java.util.Date;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.ALWAYS)
public class PipelineVO {

    private Integer id;
    private String creator;
    private String name;
    private String detail;
    private String status;
    private String dataSet;
    private String dataModel;
    private String processorGroupId;
    private AdaptorVO collector;
    private AdaptorVO filter;
    private AdaptorVO converter;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSXXX", timezone = Constants.CONTENT_DATE_TIMEZONE)
    private Date createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSXXX", timezone = Constants.CONTENT_DATE_TIMEZONE)
    private Date modifiedAt;
}
