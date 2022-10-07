package io.dtonic.dhubingestmodule.common.code;

public enum PipelineStatusCode {
    PIPELINE_STATUS_STOPPED("STOPPED"),
    PIPELINE_STATUS_STARTING("STARTING"),
    PIPELINE_STATUS_RUN("RUN"),
    PIPELINE_STATUS_STOPPING("STOPPING"),
    PIPELINE_NIFISTATUS_RUNNING("RUNNING"),
    PIPELINE_NIFISTATUS_STOP("STOP");

    private String code;

    private PipelineStatusCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static PipelineStatusCode parseCode(String code) {
        for (PipelineStatusCode statusCode : values()) {
            if (statusCode.getCode().equals(code)) {
                return statusCode;
            }
        }
        return null;
    }
}
