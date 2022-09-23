package io.dtonic.dhubingestmodule.dataset.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataModelResponseVO {

    private List<String> context;
    /** DataModel ID */
    private String id;
    /** DataModel type */
    private String type;
    /** DataModel type Uri */
    private String typeUri;
    /** DataModel name */
    private String name;
    /** Entity description */
    private String description;
    /** Featured Search Attribute Id list */
    private List<String> indexAttributeNames;
    /** Entity Attribute Information */
    private List<AttributeVO> attributes;
    /** Creator ID */
    private String creatorId;
    /** Creation date */
    private String createdAt;
    /** Modifier ID */
    private String modifierId;
    /** Modified date */
    private String modifiedAt;
}
