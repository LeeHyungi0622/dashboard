package io.dtonic.dhubingestmodule.pipeline.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dtonic.dhubingestmodule.nifi.vo.AdaptorVO;
import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PipelineResponseVO {

    private Integer id;
    private String creator;
    private String name;
    private String detail;
    private String status;
    private String dataSet;
    private String processorGroupId;
    private List<AdaptorVO> collector;
    private List<AdaptorVO> filter;
    private List<AdaptorVO> converter;
    private Date createdAt;
    private Date modifiedAt;
}
