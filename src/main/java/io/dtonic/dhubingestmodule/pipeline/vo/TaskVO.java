package io.dtonic.dhubingestmodule.pipeline.vo;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class TaskVO {
    private Integer id;

    private Integer commandId;

    private String taskName;

    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSXXX")
    private Date startedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSXXX")
    private Date finishedAt;
}
