package io.dtonic.dhubingestmodule.pipeline.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.dtonic.dhubingestmodule.adaptor.vo.AdaptorVO;
import io.dtonic.dhubingestmodule.common.code.Constants;

import java.util.Date;

import org.json.JSONObject;

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
    private String collectorString;
    private String filterString;
    private String converterString;
    private AdaptorVO collector;
    private AdaptorVO filter;
    private AdaptorVO converter;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSXXX", timezone = Constants.CONTENT_DATE_TIMEZONE)
    private Date createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSXXX", timezone = Constants.CONTENT_DATE_TIMEZONE)
    private Date modifiedAt;

    public void setCollector(AdaptorVO collector){
        this.collector = collector;
        this.collectorString = adaptorToString(collector);
    }
    public void setFilter(AdaptorVO filter){
        this.filter = filter;
        this.filterString = adaptorToString(filter);
    }
    public void setConverter(AdaptorVO converter){
        this.converter = converter;
        this.converterString = adaptorToString(converter);
    }

    private String adaptorToString(AdaptorVO adaptorVO){
        JSONObject jsonObject = new JSONObject(adaptorVO);
        return jsonObject.toString();
    }
}
