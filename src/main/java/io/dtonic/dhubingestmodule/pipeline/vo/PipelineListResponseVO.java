package io.dtonic.dhubingestmodule.pipeline.vo;

import java.util.Date;
import lombok.Data;

@Data
public class PipelineListResponseVO {

    private Integer id;
    private String name;
    private String detail;
    private String data_set;
    private String status;
    private Date createdAt;
    private Date modifiedAt;
}
