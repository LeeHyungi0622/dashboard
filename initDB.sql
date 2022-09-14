-- AUTHORIZATION 뒤의 사용자는 각 DB ID에 맞게 변경할 것
CREATE SCHEMA INGEST_MANAGER
    AUTHORIZATION DATACORE;


-- 220913 properties 테이블 추가
CREATE TABLE IF NOT EXISTS ingest_manager.properties
(
    id character varying(255) COLLATE pg_catalog."default" NOT NULL,
    adaptor_type character varying(255) COLLATE pg_catalog."default",
    adaptor_name character varying(255) COLLATE pg_catalog."default",
    nifi_type character varying(255) COLLATE pg_catalog."default",
    processor_name character varying(255) COLLATE pg_catalog."default",
    detail character varying(255) COLLATE pg_catalog."default",
    is_required boolean,
    CONSTRAINT properties_pkey PRIMARY KEY (id)
);

-- 220913 properties_value 테이블 추가
CREATE TABLE IF NOT EXISTS ingest_manager.properties_value
(
    id character varying(255) COLLATE pg_catalog."default" NOT NULL,
    property_id character varying(255) COLLATE pg_catalog."default",
    property_name character varying(255) COLLATE pg_catalog."default",
    property_value character varying(255) COLLATE pg_catalog."default",
    is_default boolean,
    CONSTRAINT properties_value_pkey PRIMARY KEY (id),
    CONSTRAINT properties_value_fkey FOREIGN KEY (property_id)
        REFERENCES ingest_manager.properties (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);


-- 220913 pipeline 테이블 추가
CREATE TABLE IF NOT EXISTS ingest_manager.pipeline
(
    id character varying(255) COLLATE pg_catalog."default" NOT NULL,
    creator character varying(255) COLLATE pg_catalog."default",
    name character varying(255) COLLATE pg_catalog."default" NOT NULL,
    detail character varying(255) COLLATE pg_catalog."default",
    status character varying(255) COLLATE pg_catalog."default",
    processor_group_id character varying(255) COLLATE pg_catalog."default",
    data_set character varying(255) COLLATE pg_catalog."default",
    collector character varying(255) COLLATE pg_catalog."default",
    filter character varying(255) COLLATE pg_catalog."default",
    converter character varying(255) COLLATE pg_catalog."default",
    created_at timestamp with time zone NOT NULL,
    modified_at timestamp with time zone,
    CONSTRAINT pipeline_pkey PRIMARY KEY (id)
);

-- 220913 temp_pipeline 테이블 추가
CREATE TABLE IF NOT EXISTS ingest_manager.temp_pipeline
(
    id character varying(255) COLLATE pg_catalog."default" NOT NULL,
    creator character varying(255) COLLATE pg_catalog."default",
    name character varying(255) COLLATE pg_catalog."default" NOT NULL,
    detail character varying(255) COLLATE pg_catalog."default",
    processor_group_id character varying(255) COLLATE pg_catalog."default",
    data_set character varying(255) COLLATE pg_catalog."default",
    collector character varying(255) COLLATE pg_catalog."default",
    filter character varying(255) COLLATE pg_catalog."default",
    converter character varying(255) COLLATE pg_catalog."default",
    created_at timestamp with time zone NOT NULL,
    modified_at timestamp with time zone,
    CONSTRAINT temp_pipeline_pkey PRIMARY KEY (id)
);

