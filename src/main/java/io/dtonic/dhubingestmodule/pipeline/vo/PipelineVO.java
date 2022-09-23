package io.dtonic.dhubingestmodule.pipeline.vo;

import io.dtonic.dhubingestmodule.nifi.vo.AdaptorVO;
import java.util.Date;
import lombok.Data;

@Data
public class PipelineVO {

    private String id;

    private String creator;
    private String name;
    private String detail;
    private String status;
    private String dataSet;
    private String processorGroupId;
    private AdaptorVO collector;
    private AdaptorVO filter;
    private AdaptorVO converter;
}
