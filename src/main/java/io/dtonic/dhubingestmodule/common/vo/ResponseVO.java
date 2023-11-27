package io.dtonic.dhubingestmodule.common.vo;

import org.springframework.http.HttpStatus;

import lombok.Data;
@Data
public class ResponseVO {
    HttpStatus status;
    String message;
    Object data;
}
