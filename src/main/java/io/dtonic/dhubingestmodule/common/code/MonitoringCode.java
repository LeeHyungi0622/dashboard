package io.dtonic.dhubingestmodule.common.code;

public enum MonitoringCode {
    /* Delete Pipeline */
    CLEAR_QUEUE_PROCESSGROUP("Clear_Queue_Processgroup"),
    CLEAR_QUEUE_FUNNEL("Clear_Queue_Funnel"),
    DISABLE_CONTROLLER("Disable_Controller"),
    DELETE_CONNECTION("Delete_Connection"),
    DELETE_PROCESSGROUP("Delete_Processgroup"),
    
    /* Create Pipeline */
    CREATE_PROCESSGROUP("Create_Processgroup"),
    CREATE_OUTPUTPORT_PIPELINE("Create_OutputPort_pipeline"),
    CREATE_ADAPTOR_COLLECTOR("Create_Adaptor_Collector"),
    CREATE_ADAPTOR_FILTER("Create_Adaptor_Filter"),
    CREATE_ADAPTOR_CONVERTOR("Create_Adaptor_Convertor"),
    CREATE_CONNECTION_BETWEEN_PROCESSGROUP("Create_Connection_Between_Processgroup"),
    CREATE_CONNECTION_BETWEEN_PROCESSGROUP_OUTPUT("Create_Connection_Between_Processgroup_Output"),
    CREATE_CONNECTION_BETWEEN_OUTPUT_FUNNEL("Create_Connection_Between_Output_Funnel"),
    ENABLE_CONTROLLER("Enable_Controller"),
    
    /* Status Pipeline */
    RUN_PIPELINE("Run_Pipeline"),
    STOP_PIPELINE("Stop_Pipeline");

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
