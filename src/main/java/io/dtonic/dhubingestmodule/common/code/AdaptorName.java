package io.dtonic.dhubingestmodule.common.code;

public enum AdaptorName {
    ADAPTOR_NAME_COLLECTOR("collector"),
    ADAPTOR_NAME_FILTER("filter"),
    ADAPTOR_NAME_CONVERTER("converter");

    private String code;

    private AdaptorName(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static AdaptorName parseCode(String code) {
        for (AdaptorName adaptor : values()) {
            if (adaptor.getCode().equals(code)) {
                return adaptor;
            }
        }
        return null;
    }
}
