package io.dtonic.dhubingestmodule.common.code;

public enum AdaptorName {
    COLLECTOR_NAME_REST_API("REST API"),
    COLLECTOR_NAME_REST_SERVER("REST Server"),
    COLLECTOR_NAME_DB_MYSQL("Database-MySQL"),
    COLLECTOR_NAME_DB_MARIA("Database-MariaDB"),
    COLLECTOR_NAME_DB_POSTGRES("Database-Postgres"),
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
