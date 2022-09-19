package io.dtonic.dhubingestmodule.pipeline.vo;

import lombok.Data;

@Data
public class PipelineListRetrieveVO {

    private Integer offset;
    private Integer limit;
    private String status;
    private String searchObject;
    private String searchValue;
}
