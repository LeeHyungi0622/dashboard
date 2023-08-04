package io.dtonic.dhubingestmodule.history.vo;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class CommandVO {
    
    private Integer id;

    private Integer pipelineId;

    private String command;

    private String status;

    private String userId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSXXX")
    private Date startedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSXXX")
    private Date finishedAt;
    
    
    
}
