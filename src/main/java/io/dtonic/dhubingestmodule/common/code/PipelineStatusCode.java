package io.dtonic.dhubingestmodule.common.code;

public enum PipelineStatusCode {
    PIPELINE_STATUS_STOPPED("Stopped"),
    PIPELINE_STATUS_STARTING("Starting"),
    PIPELINE_STATUS_RUN("Run"),
    PIPELINE_STATUS_STOPPING("Stopping"),
    PIPELINE_NIFISTATUS_RUNNING("Running"),
    PIPELINE_NIFISTATUS_STOP("Stop");

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
