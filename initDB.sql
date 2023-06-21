-- AUTHORIZATION 뒤의 사용자는 각 DB ID에 맞게 변경할 것
CREATE SCHEMA INGEST_MANAGER AUTHORIZATION POSTGRES;

CREATE SEQUENCE ingest_manager.adaptor_id_seq INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1;

-- 220915 adaptor 테이블 추가
-- 220922 char -> text로 타입 변경
-- 220926 nifi_type 컬럼 삭제
CREATE TABLE IF NOT EXISTS ingest_manager.adaptor (
    id integer NOT NULL DEFAULT nextval('ingest_manager.adaptor_id_seq' :: regclass),
    adaptor_type text COLLATE pg_catalog."default",
    adaptor_name text COLLATE pg_catalog."default",
    nifi_name text COLLATE pg_catalog."default",
    nifi_type text COLLATE pg_catalog."default",
    CONSTRAINT adaptor_pkey PRIMARY KEY (id)
);

CREATE SEQUENCE ingest_manager.properties_id_seq INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1;

-- 220915 properties 테이블 수정
-- 220922 char -> text로 타입 변경
-- 220926 nifi_type 컬럼 추가
CREATE TABLE IF NOT EXISTS ingest_manager.properties (
    id integer NOT NULL DEFAULT nextval('ingest_manager.properties_id_seq' :: regclass),
    adaptor_id integer,
    property_name text COLLATE pg_catalog."default",
    detail text COLLATE pg_catalog."default",
    is_required boolean,
    adaptor_name text COLLATE pg_catalog."default",
    CONSTRAINT properties_pkey PRIMARY KEY (id),
    CONSTRAINT properties_fkey FOREIGN KEY (adaptor_id) REFERENCES ingest_manager.adaptor (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE SEQUENCE ingest_manager.properties_value_id_seq INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1;

-- 220915 properties_value 테이블 수정
-- 220922 properties_value 테이블 수정 : property_name, property_value, is_default 컬럼 삭제 / value 컬럼 추가
CREATE TABLE IF NOT EXISTS ingest_manager.properties_value (
    id integer NOT NULL DEFAULT nextval(
        'ingest_manager.properties_value_id_seq' :: regclass
    ),
    property_id integer,
    value text COLLATE pg_catalog."default",
    CONSTRAINT properties_value_pkey PRIMARY KEY (id),
    CONSTRAINT properties_value_fkey FOREIGN KEY (property_id) REFERENCES ingest_manager.properties (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE SEQUENCE ingest_manager.pipeline_id_seq INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1;

-- 220913 pipeline 테이블 추가
-- 220922 charvar -> text로 변경
CREATE TABLE IF NOT EXISTS ingest_manager.pipeline (
    id integer NOT NULL DEFAULT nextval('ingest_manager.pipeline_id_seq' :: regclass),
    creator text COLLATE pg_catalog."default",
    name text COLLATE pg_catalog."default" NOT NULL,
    detail text COLLATE pg_catalog."default",
    STATUS text COLLATE pg_catalog."default",
    processor_group_id text COLLATE pg_catalog."default",
    data_set text COLLATE pg_catalog."default",
    data_model text COLLATE pg_catalog."default",
    collector text COLLATE pg_catalog."default",
    filter text COLLATE pg_catalog."default",
    converter text COLLATE pg_catalog."default",
    created_at timestamp WITH time zone NOT NULL,
    modified_at timestamp WITH time zone,
    CONSTRAINT pipeline_pkey PRIMARY KEY (id)
);

CREATE SEQUENCE ingest_manager.temp_pipeline_id_seq INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1;

-- 220915 temp_pipeline 테이블 수정
CREATE TABLE IF NOT EXISTS ingest_manager.temp_pipeline (
    id integer NOT NULL DEFAULT nextval('ingest_manager.temp_pipeline_id_seq' :: regclass),
    creator text COLLATE pg_catalog."default",
    name text COLLATE pg_catalog."default" NOT NULL,
    detail text COLLATE pg_catalog."default",
    data_set text COLLATE pg_catalog."default",
    data_model text COLLATE pg_catalog."default",
    collector text COLLATE pg_catalog."default",
    filter text COLLATE pg_catalog."default",
    converter text COLLATE pg_catalog."default",
    created_at timestamp WITH time zone NOT NULL,
    modified_at timestamp WITH time zone,
    CONSTRAINT temp_pipeline_pkey PRIMARY KEY (id)
);

-- Command Task Table 개발 23.03.16 -- 

-- SEQUENCE: ingest_manager.command_history_id_seq

-- DROP SEQUENCE IF EXISTS ingest_manager.command_history_id_seq;

CREATE SEQUENCE IF NOT EXISTS ingest_manager.command_history_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 2147483647
    CACHE 1;

ALTER SEQUENCE ingest_manager.command_history_id_seq
    OWNER TO postgres;

-- DROP TABLE IF EXISTS ingest_manager.command_history;
CREATE TABLE IF NOT EXISTS ingest_manager.command_history
(
    id integer NOT NULL DEFAULT nextval('ingest_manager.command_history_id_seq'::regclass),
    pipeline_id integer NOT NULL,
    command text COLLATE pg_catalog."default",
    status text COLLATE pg_catalog."default",
    user_id text COLLATE pg_catalog."default",
    started_at timestamp with time zone NOT NULL,
    finished_at timestamp with time zone,
    CONSTRAINT command_history_pkey PRIMARY KEY (id),
    CONSTRAINT command_history_fkey FOREIGN KEY (pipeline_id)
        REFERENCES ingest_manager.pipeline (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
        NOT VALID
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

-- Index: fki_command_history_fkey

-- DROP INDEX IF EXISTS ingest_manager.fki_command_history_fkey;

-- SEQUENCE: ingest_manager.task_history_id_seq

-- DROP SEQUENCE IF EXISTS ingest_manager.task_history_id_seq;

CREATE SEQUENCE IF NOT EXISTS ingest_manager.task_history_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 2147483647
    CACHE 1;

ALTER SEQUENCE ingest_manager.task_history_id_seq
    OWNER TO postgres;

CREATE INDEX IF NOT EXISTS fki_command_history_fkey
    ON ingest_manager.command_history USING btree
    (pipeline_id ASC NULLS LAST)
    TABLESPACE pg_default;


-- Table: ingest_manager.task_history
-- DROP TABLE IF EXISTS ingest_manager.task_history;
CREATE TABLE IF NOT EXISTS ingest_manager.task_history
(
    id integer NOT NULL DEFAULT nextval('ingest_manager.task_history_id_seq'::regclass),
    command_id integer NOT NULL,
    task_name text COLLATE pg_catalog."default",
    status text COLLATE pg_catalog."default",
    started_at timestamp with time zone NOT NULL,
    finished_at timestamp with time zone,
    CONSTRAINT task_history_pkey PRIMARY KEY (id),
    CONSTRAINT task_history_fkey FOREIGN KEY (command_id)
        REFERENCES ingest_manager.command_history (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;