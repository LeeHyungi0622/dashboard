package io.dtonic.dhubingestmodule.dataset.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataModelVO {

    private String datasetId;
    private List<String> context;
    private String id;
    private String type;
    private String typeUri;
    private String name;
    private String description;
    private List<String> indexAttributeNames;
    private List<Attribute> attributes;
}
