package io.dtonic.dhubingestmodule.pipeline.vo;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.dtonic.dhubingestmodule.common.code.Constants;

import java.util.Date;
import lombok.Data;

@Data
public class PipelineDraftsListResponseVO {

    private Integer id;
    private String name;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss z", timezone = Constants.CONTENT_DATE_TIMEZONE)
    private Date modifiedAt;

    private Boolean isCollector;
    private Boolean isFilter;
    private Boolean isConverter;
}
