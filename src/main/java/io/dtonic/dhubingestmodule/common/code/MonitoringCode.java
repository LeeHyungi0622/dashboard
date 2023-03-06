package io.dtonic.dhubingestmodule.common.code;

public enum MonitoringCode {
    /* Delete Pipeline */
    CLEAR_QUEUE("ClearQueue"),
    DISABLE_CONTROLLER("DisableController"),
    STOP_PIPELINE("StopPipeline"),
    DELETE_CONNECTION("DeleteConnection"),
    DELETE_PIPELINE("DeletePipeline"),

    /* Create Pipeline */
    CREATE_PROCESSGROUP("CreateProcessGroup"),
    CREATE_OUTPUT_INPIPELINE("CreateOutputInpipeline"),
    CREATE_ADAPTOR("CreateAdaptor"),
    CREATE_CONNECTION_BETWEEN_PROCESSGROUP("CreateConnectionBetweenProcessgroup"),
    ENABLE_CONTROLLERS("EnableControllers");

    private String code;

    private MonitoringCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static MonitoringCode parseCode(String code) {
        for (MonitoringCode monitoringcode : values()) {
            if (monitoringcode.getCode().equals(code)) {
                return monitoringcode;
            }
        }
        return null;
    }
}
