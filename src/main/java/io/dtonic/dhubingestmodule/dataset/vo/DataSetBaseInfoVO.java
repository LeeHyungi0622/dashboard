package io.dtonic.dhubingestmodule.dataset.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataSetBaseInfoVO {

    /** Data set ID */
    private String id;
    /** Data set name */
    private String name;
    /** Data set description */
    private String description;
    /** Update interval */
    private String updateInterval;
    /** Category(code) */
    private String category;
    /** Provider */
    private String providerOrganization;
    /** Provider system */
    private String providerSystem;
    /** Whether data is processed */
    private String isProcessed;
    /** Ownership */
    private String ownership;
    /** Keyword(code) */
    private List<String> keyword;
    /** License(code) */
    private String license;
    /** Provider API uri */
    private String providingApiUri;
    /** Restrictions */
    private String restrictions;
    /** Data set extension */
    private String datasetExtension;
    /** Data set items */
    private String datasetItems;
    /** Target regions */
    private String targetRegions;
    /** Source Data set Ids */
    private List<String> sourceDatasetIds;
    /** Whether quality is verified */
    private Boolean qualityCheckEnabled;
    /** Data identifier type(code) */
    private String dataIdentifierType;
    /** Data model ID */
    private String dataModelId;
    /** Historical data retention period (Day) */
    private Integer storageRetention;
    /** Topic retention period (ms) */
    private Long topicRetention;
    /** Data store uri */
    private List<String> dataStoreUri;
    /** Creator ID */
    private String creatorId;
    /** Creation date */
    private String createdAt;
    /** Modifier ID */
    private String modifierId;
    /** Modified date */
    private String modifiedAt;
}
