package io.dtonic.dhubingestmodule.common.code;

public enum NifiStatusCode {
    NIFI_STATUS_STOPPED("Stopped"),
    NIFI_STATUS_RUNNING("Running"),
    NIFI_STATUS_INVALID("Invaild");

    private String code;

    private NifiStatusCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static NifiStatusCode parseCode(String code) {
        for (NifiStatusCode statusCode : values()) {
            if (statusCode.getCode().equals(code)) {
                return statusCode;
            }
        }
        return null;
    }
}
