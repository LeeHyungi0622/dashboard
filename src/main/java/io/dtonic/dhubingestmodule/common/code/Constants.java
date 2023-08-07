package io.dtonic.dhubingestmodule.common.code;

/**
 * Common constant management class
 * @FileName Constants.java
 * @Project citydatahub_datacore_ui
 * @Brief
 * @Version 1.0
 * @Date 2022. 3. 24.
 * @Author Elvin
 */
public class Constants {

    /** Default package path */
    public static final String BASE_PACKAGE = "io.dtonic";
    /** Kafka processing error logger name */
    public static final String KAFKA_REQUEST_ERROR_LOGGER_NAME = "kafkaRequestErrorLogger";
    /** Content Date Format */
    public static final String CONTENT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
    /** NIFI Content Date Format */
    public static final String NIFI_CONTENT_DATE_FORMAT = "HH:mm:ss z";
    /** PostgreSQL SELECT Date Format */
    public static final String POSTGRES_DATE_FORMAT = "yyyy-MM-dd' 'HH:mm:ss";
    /** PostgreSQL INSERT Date Format */
    public static final String POSTGRES_TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    /** Content Date TimeZone */
    public static final String CONTENT_DATE_TIMEZONE = "Asia/Seoul";

    /** application/ld+json Key */
    public static final String APPLICATION_LD_JSON_VALUE = "application/ld+json";

    public static final int DEFAULT_SRID = 4326;
    public static final String GEO_PREFIX_4326 = "_4326";
    public static final String GEO_PREFIX_3857 = "_3857";
    public static final String COLUMN_DELIMITER = "_";

    public static final String SCHEMA_NAME = "smart_city";
    public static final String PARTIAL_HIST_TABLE_PREFIX = "_hist";
    public static final String FULL_HIST_TABLE_PREFIX = "_full_hist";

    public static final String CHARSET_ENCODING = "UTF-8";

    public static final String PREFIX_SUBSCRIPTION_ID = "urn:ngsi-ld:Subscription:";

    public static final String TOTAL_COUNT = "totalCount";

    public static final String[] MULTI_DATE_FORMATS = new String[] {
        "yyyy-MM-dd'T'HH:mm:ssXXX",
        "yyyy-MM-dd'T'HH:mm:ss.SXXX",
        "yyyy-MM-dd'T'HH:mm:ss.SSXXX",
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        "yyyy-MM-dd'T'HH:mm:ss.SSSSXXX",
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSXXX",
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX",
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        "yyyy-MM-dd'T'HH:mm:ss.SSS",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd HH:mm:ss.SSS",
        "HH:mm:ss z" // 하위 호환
    };

    public static final String[] NIFI_TEMPLATE_NAMES = new String[] {
        "converter",
        "Database-MariaDB",
        "Database-Postgres",
        "Database-MySQL",
        "filter",
        "REST API",
        "REST Server"
    };
}
