package io.dtonic.dhubingestmodule.common.code;

public enum MonitoringCode {
    /* Delete Pipeline */
    CLEAR_QUEUE("Clear_Queue"),
    DISABLE_CONTROLLER("Disable_Controller"),
    STOP_PIPELINE("Stop_Pipeline"),
    DELETE_CONNECTION("Delete_Connection"),
    DELETE_PIPELINE("Delete_Pipeline"),

    /* Create Pipeline */
    CREATE_PROCESSGROUP("Create_Processgroup"),
    CREATE_OUTPUTPORT_PIPELINE("Create_OutputPort_pipeline"),
    CREATE_ADAPTOR_COLLECTOR("Create_Adaptor_Collector"),
    CREATE_ADAPTOR_FILTER("Create_Adaptor_Filter"),
    CREATE_ADAPTOR_CONVERTOR("Create_Adaptor_Convertor"),
    CREATE_CONNECTION_BETWEEN_PROCESSGROUP("Create_Connection_Between_Processgroup"),
    ENABLE_CONTROLLERS("Enable_Controllers");

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
