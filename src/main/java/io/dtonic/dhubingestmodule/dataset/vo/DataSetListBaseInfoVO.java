package io.dtonic.dhubingestmodule.dataset.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataSetListBaseInfoVO {
    /** Data set ID */
	private String id;
	/** Data set name */
	private String name;
	/** Data set metasetId */
	private String metasetId;
	/** Update interval */
	private String storageRetention;
	
	private String classification;

    private String ownership;

    private String qualityCheckEnabled;

    private String createdAt;

}
