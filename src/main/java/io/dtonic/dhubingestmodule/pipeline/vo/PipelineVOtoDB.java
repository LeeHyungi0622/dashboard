package io.dtonic.dhubingestmodule.pipeline.vo;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Date;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.ALWAYS)
public class PipelineVOtoDB {

    private Integer id;
    private String creator;
    private String name;
    private String detail;
    private String status;
    private String dataSet;
    private String dataModel;
    private String processorGroupId;
    private String collector;
    private String filter;
    private String converter;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSXXX")
    private Date createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSXXX")
    private Date modifiedAt;
}
