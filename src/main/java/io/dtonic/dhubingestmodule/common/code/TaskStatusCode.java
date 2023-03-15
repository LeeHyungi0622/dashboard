package io.dtonic.dhubingestmodule.common.code;

public enum TaskStatusCode {
    TASK_STATUS_WORKING("WORKING"),
    TASK_STATUS_FINISH("FINISH"),
    TASK_STATUS_FAILED("FAILED"),

    TASK_TASK_NAME_RUN("RUN"),
    TASK_TASK_NAME_STOP("STOP");
  
    
    private String code;

    private TaskStatusCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static TaskStatusCode parseCode(String code) {
        for (TaskStatusCode statusCode : values()) {
            if (statusCode.getCode().equals(code)) {
                return statusCode;
            }
        }
        return null;
    }
}
