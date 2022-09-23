-- AUTHORIZATION 뒤의 사용자는 각 DB ID에 맞게 변경할 것
CREATE SCHEMA INGEST_MANAGER
    AUTHORIZATION DATACORE;

CREATE SEQUENCE ingest_manager.adaptor_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 2147483647
    CACHE 1;


-- 220915 adaptor 테이블 추가
CREATE TABLE IF NOT EXISTS ingest_manager.adaptor
(
    id integer NOT NULL DEFAULT nextval('ingest_manager.adaptor_id_seq'::regclass),
    adaptor_type character varying(255) COLLATE pg_catalog."default",
    adaptor_name character varying(255) COLLATE pg_catalog."default",
    nifi_type character varying(255) COLLATE pg_catalog."default",
    nifi_name character varying(255) COLLATE pg_catalog."default",
    CONSTRAINT adaptor_pkey PRIMARY KEY (id)
);


CREATE SEQUENCE ingest_manager.properties_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 2147483647
    CACHE 1;

-- 220915 properties 테이블 수정
CREATE TABLE IF NOT EXISTS ingest_manager.properties
(
    id integer NOT NULL DEFAULT nextval('ingest_manager.properties_id_seq'::regclass),
    adaptor_id integer,
    property_name character varying(255) COLLATE pg_catalog."default",
    detail character varying(255) COLLATE pg_catalog."default",
    is_required boolean,
    CONSTRAINT properties_pkey PRIMARY KEY (id),
    CONSTRAINT properties_fkey FOREIGN KEY (adaptor_id)
        REFERENCES ingest_manager.adaptor (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);


CREATE SEQUENCE ingest_manager.properties_value_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 2147483647
    CACHE 1;

-- 220915 properties_value 테이블 수정
CREATE TABLE IF NOT EXISTS ingest_manager.properties_value
(
    id integer NOT NULL DEFAULT nextval('ingest_manager.properties_value_id_seq'::regclass),
    property_id integer,
    property_name character varying(255) COLLATE pg_catalog."default",
    property_value character varying(255) COLLATE pg_catalog."default",
    is_default boolean,
    CONSTRAINT properties_value_pkey PRIMARY KEY (id),
    CONSTRAINT properties_value_fkey FOREIGN KEY (property_id)
        REFERENCES ingest_manager.properties (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE SEQUENCE ingest_manager.pipeline_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 2147483647
    CACHE 1;

-- 220913 pipeline 테이블 추가
CREATE TABLE IF NOT EXISTS ingest_manager.pipeline
(
    id integer NOT NULL DEFAULT nextval('ingest_manager.pipeline_id_seq'::regclass),
    creator character varying(255) COLLATE pg_catalog."default",
    name character varying(255) COLLATE pg_catalog."default" NOT NULL,
    detail character varying(255) COLLATE pg_catalog."default",
    status character varying(255) COLLATE pg_catalog."default",
    processor_group_id character varying(255) COLLATE pg_catalog."default",
    data_set character varying(255) COLLATE pg_catalog."default",
    collector text COLLATE pg_catalog."default",
    filter text COLLATE pg_catalog."default",
    converter text COLLATE pg_catalog."default",
    created_at timestamp with time zone NOT NULL,
    modified_at timestamp with time zone,
    CONSTRAINT pipeline_pkey PRIMARY KEY (id)
);


CREATE SEQUENCE ingest_manager.temp_pipeline_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 2147483647
    CACHE 1;

-- 220915 temp_pipeline 테이블 수정
CREATE TABLE IF NOT EXISTS ingest_manager.temp_pipeline
(
    id integer NOT NULL DEFAULT nextval('ingest_manager.temp_pipeline_id_seq'::regclass),
    creator character varying(255) COLLATE pg_catalog."default",
    name character varying(255) COLLATE pg_catalog."default" NOT NULL,
    detail character varying(255) COLLATE pg_catalog."default",
    data_set character varying(255) COLLATE pg_catalog."default",
    collector text COLLATE pg_catalog."default",
    filter text COLLATE pg_catalog."default",
    converter text COLLATE pg_catalog."default",
    created_at timestamp with time zone NOT NULL,
    modified_at timestamp with time zone,
    CONSTRAINT temp_pipeline_pkey PRIMARY KEY (id)
);

