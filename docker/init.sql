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
-- 220926 nifi_type 컬럼 제거
-- REST Server colloector 구간
INSERT INTO ingest_manager.adaptor (adaptor_type, adaptor_name, nifi_name, nifi_type) values ('collector', 'REST Server', 'REST Server', 'Processor');

INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values( (SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'Base Path', '들어오는 연결의 기본 경로', true, 'REST Server');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), 'contentListener' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values( (SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'Listening Port', '들어오는 연결을 수신할 포트', true, 'REST Server');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), null );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values( (SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'health-check-port', '수신 상태 검사 요청을 수신할 포트입니다. 설정된 경우 수신 포트와 달라야 합니다. 프로세서가 양방향 SSL을 사용하도록 설정되어 있고 상태 점검 요청에 대해 클라이언트 인증을 지원하지 않는 로드 밸런서가 사용되는 경우 이 포트를 구성합니다. /<base_path>/상태 점검 서비스만 이 포트를 통해 사용할 수 있으며 GET 및 HEAD 요청만 지원됩니다. 프로세서가 SSL을 사용하지 않도록 설정된 경우 이 포트에서도 SSL이 사용되지 않습니다. 프로세서가 단방향 SSL을 사용하도록 설정된 경우 이 포트에서 단방향 SSL이 사용됩니다. 프로세서가 양방향 SSL을 사용하도록 설정된 경우 이 포트에서 단방향 SSL이 사용됩니다(클라이언트 인증은 필요하지 않음).', false, 'REST Server');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), null );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values( (SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'Max Data to Receive per Second', '초당 수신할 최대 데이터 양. 대역폭을 지정된 데이터 속도로 조절할 수 있습니다. 지정하지 않으면 데이터 속도가 조절되지 않습니다.', false, 'REST Server');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), null );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values( (SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'Authorized DN Pattern', '수신 연결의 제목 고유 이름에 적용할 정규식입니다. 패턴이 제목 DN과 일치하지 않으면 프로세서는 HTTP 403 금지 상태로 응답합니다.', true, 'REST Server');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '.*' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values( (SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'authorized-issuer-dn-pattern', '들어오는 연결의 발급자 고유 이름에 적용할 정규식입니다. 패턴이 발급자 DN과 일치하지 않으면 프로세서는 HTTP 403 금지 상태로 응답합니다.', false, 'REST Server');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '.*' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values( (SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'Max Unconfirmed Flowfile Time', '캐시에서 FlowFile이 제거될 때까지 FlowFile이 확인될 때까지 기다리는 최대 시간', true, 'REST Server');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '60 secs' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values( (SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'HTTP Headers to receive as Attributes (Regex)', '흐름 파일 속성과 함께 전달되어야 하는 HTTP 헤더의 이름을 결정하는 정규식을 지정합니다.', false, 'REST Server');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '.*' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values( (SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'Return Code', '모든 HTTP 호출 후 반환되는 HTTP 반환 코드', false, 'REST Server');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '200' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values( (SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'multipart-request-max-size', '요청의 최대 크기입니다. Content-Type: multipart/form-data가 포함된 요청에만 적용되며, 힙 또는 디스크 공간이 가득 차지 않도록 서비스 거부 유형 공격을 방지하는 데 사용됩니다.', true, 'REST Server');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '1 MB' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values( (SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'multipart-read-buffer-size', '수신 파일의 내용을 디스크에 쓰는 임계값 크기입니다. 내용 유형(다중 부품/양식 데이터)이 있는 요청에만 적용됩니다. 힙 또는 디스크 공간이 가득 차지 않도록 서비스 거부 유형의 공격을 방지하는 데 사용됩니다.', true, 'REST Server');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '1 MB' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values( (SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'max-thread-pool-size', '내장된 Jetty 서버에서 사용할 최대 스레드 수입니다. 값은 8과 1000 사이에서 설정할 수 있습니다. 이 속성의 값은 흐름과 운영 체제의 성능에 영향을 미치므로 정당한 경우에만 기본값을 변경해야 합니다. 소수의 HTTP 클라이언트만 서버에 연결하는 경우 기본값보다 작은 값이 적합할 수 있습니다. 많은 수의 HTTP 클라이언트가 서버에 동시에 요청할 것으로 예상되는 경우 더 큰 값이 적합할 수 있습니다.', true, 'REST Server');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '200' );

------------------------------------------------------------------------------------------------------------------------------------------------------------
-- REST API collector 구간
INSERT INTO ingest_manager.adaptor (adaptor_type, adaptor_name, nifi_name, nifi_type) values ('collector', 'REST API', 'REST API', 'Processor');

INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'HTTP Method', 'HTTP 요청 방법(GET, POST, PUT, PATCH, DELETE, HEAD, OPTIONS). 임의 메서드도 지원됩니다. POST, PUT 및 PATCH 이외의 메서드는 메시지 본문 없이 전송됩니다.', true, 'REST API');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), 'POST,GET,PUT,DELETE' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'Remote URL', '연결할 원격 URL(구성표, 호스트, 포트, 경로 포함)입니다.', true, 'REST API');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), null );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'Connection Timeout', '원격 서비스에 연결하기 위한 최대 대기 시간입니다.', true, 'REST API');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '5 sec' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'Read Timeout', '원격 서비스의 응답에 대한 최대 대기 시간입니다.', true, 'REST API');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '15 sec' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'idle-timeout', '원격 서비스에 대한 연결을 닫기 전의 최대 유휴 시간입니다.', true, 'REST API');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '5 mins' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'max-idle-connections', '열려 있는 상태로 유지할 최대 유휴 연결 수입니다.', true, 'REST API');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '5' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'Include Date Header', '요청에 RFC-2616 Date 헤더를 포함합니다.', true, 'REST API');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), 'True,False' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'Follow Redirects', '원격 서버에서 발급한 HTTP 리디렉션을 따릅니다.', true, 'REST API');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), 'True,False' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'cookie-strategy', 'HTTP 쿠키를 허용하고 유지하기 위한 전략입니다. 쿠키를 허용하면 여러 요청에 대해 지속성을 유지할 수 있습니다.', true, 'REST API');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), 'DISABLED,ACCEPT_ALL' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'disable-http2', 'HTTP/2 프로토콜 버전의 사용을 금지할지 여부를 결정합니다. 비활성화된 경우 HTTP/1.1만 지원됩니다.', true, 'REST API');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), 'False,True' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'flow-file-naming-strategy', 'FlowFile의 파일 이름 특성을 설정하는 데 사용되는 전략을 결정합니다.', true, 'REST API');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), 'RANDOM,URL_PATH' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'Attributes to Send', '요청에서 HTTP 헤더로 보낼 특성을 정의하는 정규식입니다. 정의하지 않으면 특성이 헤더로 전송되지 않습니다. 또한 동적 속성 집합은 헤더로 전송됩니다. 동적 속성 키는 헤더 키가 되고 동적 속성 값은 표현식 언어가 헤더 값이 되는 것으로 해석됩니다.', false, 'REST API');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), null );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'Useragent', '각 요청과 함께 전송된 사용자 에이전트 식별자', false, 'REST API');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), null );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'Basic Authentication Username', '클라이언트가 원격 URL에 대해 인증하는 데 사용할 사용자 이름입니다. 제어 문자(0-31), '':'' 또는 DEL(127)을 포함할 수 없습니다.', false, 'REST API');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), null );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'Basic Authentication Password', '클라이언트가 원격 URL에 대해 인증하는 데 사용할 암호입니다.', false, 'REST API');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), null );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'Put Response Body In Attribute', '설정된 경우, 수신된 응답 본문은 별도의 FlowFile 대신 원래 FlowFile 속성에 저장됩니다. 이 속성의 값을 평가하여 넣을 속성 키를 결정합니다.', false, 'REST API');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), null );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'Max Length To Put In Attribute', '응답 본문을 원본 속성에 라우팅하는 경우(\"Put response body in attribute\" 속성을 설정하거나 오류 상태 코드를 수신하여), 속성 값에 입력되는 문자 수는 최대 이 양이 될 것이다. 속성은 메모리에 보관되며 큰 속성은 메모리 부족 문제를 빠르게 발생시키기 때문에 이는 중요합니다. 출력이 이 값보다 길어지면 크기에 맞게 잘립니다. 가능하면 이 크기를 작게 만드는 것을 고려해 보십시오.', false, 'REST API');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), null );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'Digest Authentication', '요약 인증을 사용하여 웹 사이트와 통신할지 여부입니다. 인증에는 ''기본 인증 사용자 이름''과 ''기본 인증 암호''를 사용합니다.', false, 'REST API');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), 'false,true' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'Always Output Response', '수신된 서버 상태 코드 또는 프로세서가 요청 속성에 서버 응답 본문을 넣도록 구성되어 있는지 여부에 관계없이 응답 흐름 파일을 생성하고 ''Response'' 관계로 라우팅합니다. 후자의 구성에서는 특성에 응답 본문과 일반적인 응답 FlowFile이 포함된 요청 FlowFile이 각각의 관계로 전송됩니다.', false, 'REST API');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), 'false,true' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'Add Response Headers to Request', '이 속성을 사용하도록 설정하면 모든 응답 헤더가 원래 요청에 저장됩니다. 이는 응답 헤더가 필요하지만 수신된 상태 코드로 인해 응답이 생성되지 않는 경우일 수 있습니다.', false, 'REST API');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), 'false,true' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'Content-Type', '콘텐츠가 PUT, POST 또는 패치를 통해 전송되는 시기를 지정하는 콘텐츠 유형입니다. 표현식 언어 표현식을 평가한 후 값이 비어 있는 경우 내용 유형은 기본적으로 응용프로그램/옥텟 스트림으로 설정됩니다.', true, 'REST API');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '${mime.type}' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'send-message-body', '참일 경우, POST/PUT/PATCH 요청 시 HTTP 메시지 본문을 보냅니다(기본값). false인 경우 이 요청에 대한 메시지 본문 및 내용 유형 헤더를 표시하지 않습니다.', false, 'REST API');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), 'true,false' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'Use Chunked Encoding', 'POST, PUTing 또는 PATCHing 콘텐츠가 이 속성을 true로 설정하여 ''Content-length'' 헤더를 통과하지 않고 ''chunked'' 값으로 ''Transfer-Encoding''을 전송합니다. 이를 통해 HTTP 1.1에 도입된 데이터 전송 메커니즘이 길이를 알 수 없는 데이터를 청크 단위로 전달할 수 있습니다.', true, 'REST API');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), 'false,true' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'use-etag', 'HTTP 요청에 대해 ETAG(HTTP 엔티티 태그) 지원을 사용합니다.', true, 'REST API');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), 'false,true' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'etag-max-cache-size', 'ETAG 캐시가 증가할 수 있는 최대 크기입니다. 기본 크기는 10MB입니다.', true, 'REST API');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '10MB' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'ignore-response-content', '참일 경우 프로세서는 응답 내용을 흐름 파일에 쓰지 않습니다.', false, 'REST API');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), 'false' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'form-body-form-name', 'Send Message Body가 true이고 FlowFile Form Data Name(흐름 파일 양식 데이터 이름)이 설정된 경우 이 값을 양식 데이터 이름으로 하는 다중 부분/양식 형식으로 FlowFile이 메시지 본문으로 전송됩니다.', false, 'REST API');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), null );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'set-form-filename', 'Send Message Body가 true이고 FlowFile Form Data Name이 설정되어 있고 Set FlowFile Form Data File Name이 true인 경우 FlowFile의 fileName 값이 폼 데이터의 파일 이름 속성으로 설정됩니다.', false, 'REST API');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), 'true,false' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'Scheduling', 'API 호출 스케줄링(단위 = sec)', true, 'REST API');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '120 sec' );

------------------------------------------------------------------------------------------------------------------------------------------------------------
--  Database-Postgres collector 구간 : SQL select query는 선택 값이지만, 흐름 상 true로 설정
INSERT INTO ingest_manager.adaptor (adaptor_type, adaptor_name, nifi_name, nifi_type) values ('collector', 'Database-Postgres', 'Database-Postgres', 'Processor');
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'sql-pre-query', '기본 SQL 쿼리가 실행되기 전에 실행된 세미콜론으로 구분된 쿼리 목록입니다. 예를 들어, 기본 쿼리 앞에 세션 속성을 설정합니다. 세미콜론은 백슬래시(''\\;'')로 이스케이프하여 문 자체에 포함할 수 있습니다. 오류가 없으면 이러한 쿼리의 결과/출력이 억제됩니다.', false, 'Database-Postgres');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), null );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'SQL select query', '실행할 SQL 선택 쿼리입니다. 쿼리는 비어 있거나 상수 값이거나 표현식 언어를 사용하는 속성을 사용하여 작성할 수 있습니다. 이 속성을 지정하면 수신 흐름 파일의 내용에 관계없이 사용됩니다. 이 속성이 비어 있으면 수신 흐름 파일의 내용에 유효한 SQL 선택 쿼리가 포함되어 프로세서에서 데이터베이스에 발급됩니다. 표현식 언어는 흐름 파일 내용에 대해 평가되지 않습니다.', true, 'Database-Postgres'); -- 흐름 상 true
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), null );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'sql-post-query', '기본 SQL 쿼리가 실행된 후 실행되는 세미콜론으로 구분된 쿼리 목록입니다. 주 쿼리 후에 세션 속성을 설정하는 것과 같은 예입니다. 세미콜론은 백슬래시(''\\;'')로 이스케이프하여 문 자체에 포함할 수 있습니다. 오류가 없으면 이러한 쿼리의 결과/출력이 억제됩니다.', false, 'Database-Postgres');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), null );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'Max Wait Time', '실행 중인 SQL 선택 쿼리에 허용되는 최대 시간입니다. 0은 제한이 없음을 의미합니다. 1초 미만의 최대 시간은 0과 같습니다.', false, 'Database-Postgres');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '0 seconds' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'dbf-normalize', '열 이름의 Avro 호환되지 않는 문자를 Avro 호환 문자로 변경할지 여부입니다. 예를 들어 유효한 Avro 레코드를 작성하기 위해 콜론과 마침표가 밑줄로 변경됩니다.', true, 'Database-Postgres');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), 'false,true' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'dbf-user-logical-types', 'DECIMAL/NUMBER, DATE, TIME 및 TIMESTAMP 열에 Avro Logical Types를 사용할지 여부를 지정합니다. 비활성화된 경우 문자열로 기록됩니다. 활성화된 경우 논리 유형이 기본 유형으로 사용되고 기록되며, 특히 논리적 "10진수"로 DECIMAL/NUMBER: 추가 정밀도와 스케일 메타데이터를 가진 바이트로 기록되고 논리적 "date-millis"로 기록되며, 논리적 "time-millis"로 기록되고, 논리적 "time-millis"로 기록되고, 논리적 "time-millis"로 기록됩니다.s는 유닉스 에포크 이후로, 타임스탬프는 논리적 "타임스탬프-millis": 유닉스 에포크 이후로 밀리초를 나타내는 긴 시간으로 작성되었다. 작성된 아브로 레코드의 독자가 이러한 논리적 유형도 알고 있다면, 이러한 값들은 독자 구현에 따라 더 많은 컨텍스트에서 역직렬화될 수 있다.', true, 'Database-Postgres');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), 'false,true' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'compression-format', 'Avro 파일을 쓸 때 사용할 압축 유형입니다. 기본값은 없음입니다.', true, 'Database-Postgres');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), 'NONE,BZIP2,DEFLATE,SNAPPY,LZO' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'dbf-default-precision', 'DECIMAL/NUMBER 값이 "decimal" Avological type으로 작성되는 경우 사용 가능한 자릿수를 나타내는 특정 "precision"이 필요합니다. 일반적으로 정밀도는 열 데이터 유형 정의 또는 데이터베이스 엔진 기본값으로 정의됩니다. 그러나 정의되지 않은 정밀도(0)는 일부 데이터베이스 엔진에서 반환될 수 있습니다. 정의되지 않은 정밀도 숫자를 작성할 때 ''Default Decimal Precision''이 사용됩니다.', true, 'Database-Postgres');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '10' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'dbf-default-scale', 'DECIMAL/NUMBER 값이 "decimal" Avological type으로 작성되는 경우 사용 가능한 10진수 숫자를 나타내는 특정 "scale"이 필요합니다. 일반적으로 축척은 열 데이터 유형 정의 또는 데이터베이스 엔진 기본값으로 정의됩니다. 그러나 정의되지 않은 정밀도(0)가 반환될 때 일부 데이터베이스 엔진에서는 스케일이 불확실할 수도 있습니다. 정의되지 않은 숫자를 쓸 때 "기본 소수점 척도"가 사용됩니다. 값에 지정된 척도보다 많은 소수점이 있는 경우 값은 반올림됩니다. 예를 들어 1.53은 척도 0에서 2가 되고 1.5는 척도 1이 됩니다.', true, 'Database-Postgres');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '0' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'esql-max-rows', '단일 FlowFile에 포함될 최대 결과 행 수입니다. 이렇게 하면 매우 큰 결과 집합을 여러 FlowFiles로 분할할 수 있습니다. 지정한 값이 0이면 모든 행이 단일 FlowFile로 반환됩니다.', true, 'Database-Postgres');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '0' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'esql-output-batch-size', '프로세스 세션을 커밋하기 전에 대기열에 넣을 출력 흐름 파일 수입니다. 0으로 설정하면 모든 결과 집합 행이 처리되고 출력 흐름 파일이 다운스트림 관계로 전송될 준비가 되면 세션이 커밋됩니다. 결과 세트가 큰 경우 프로세서 실행이 끝날 때 대량의 FlowFiles가 전송될 수 있습니다. 이 속성이 설정된 경우 지정된 수의 FlowFiles가 전송 준비가 되면 세션이 커밋되어 FlowFiles가 다운스트림 관계로 해제됩니다. 참고: fragment.count 특성은 이 속성을 설정할 때 FlowFiles에서 설정되지 않습니다.', true, 'Database-Postgres');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '0' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'esql-fetch-size', '한 번에 결과 집합에서 가져올 결과 행 수입니다. 이는 데이터베이스 드라이버에 대한 힌트이며, 이를 준수하지 않거나 정확하지 않을 수 있습니다. 지정한 값이 0이면 힌트가 무시됩니다.', true, 'Database-Postgres');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '0' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'esql-auto-commit', 'DB 연결의 자동 커밋 기능을 활성화하거나 비활성화합니다. 기본값은 ''true''입니다. 기본값은 대부분의 JDBC 드라이버에서 사용할 수 있으며 이 프로세서가 데이터를 읽는 데 사용되기 때문에 대부분의 경우 이 기능은 영향을 미치지 않습니다. 그러나 Postgre와 같은 일부 JDBC 드라이버의 경우SQL 드라이버. 한 번에 가져오는 결과 행 수를 제한하려면 자동 커밋 기능을 사용하지 않도록 설정해야 합니다. 자동 커밋이 사용하도록 설정된 경우 postgreSQL 드라이버는 전체 결과 집합을 한 번에 메모리에 로드합니다. 이로 인해 대용량 데이터 세트를 가져오는 쿼리를 실행할 때 메모리 사용량이 많을 수 있습니다. Postgre에서 이 동작에 대한 자세한 내용SQL 드라이버는 https://jdbc.postgresql.org//recommunication/head/recommunication.html에서 찾을 수 있습니다.', true, 'Database-Postgres');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), 'true,false' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'Scheduling', 'API 호출 스케줄링(단위 = sec)', true, 'Database-Postgres');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '120 sec' );
-- Controller 구간 : Database user와 password는 nifi 상에서는 선택 값이지만, 흐름 상 필수가 되어야 해서 true로 설정
INSERT INTO ingest_manager.adaptor (adaptor_type, adaptor_name, nifi_name, nifi_type) values ('collector', 'Database-Postgres', 'DBCPConnectionPool', 'Controller');
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values( (SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'Database Connection URL', '데이터베이스에 연결하는 데 사용되는 데이터베이스 연결 URL입니다. 데이터베이스 시스템 이름, 호스트, 포트, 데이터베이스 이름 및 일부 매개 변수를 포함할 수 있습니다. 데이터베이스 연결 URL의 정확한 구문은 DBMS에서 지정합니다.', true, 'Database-Postgres');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), 'jdbc:postgresql://postgresql:5432/postgres' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values( (SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'Database User', '데이터베이스 사용자 이름', true, 'Database-Postgres'); -- 흐름 상 true가 되어야 함
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), null );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values( (SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'Password', '데이터베이스 사용자의 암호', true, 'Database-Postgres'); -- 흐름 상 true가 되어야 함
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), null );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values( (SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'Max Wait Time', '연결이 반환될 때까지 풀에서 대기하는 최대 시간(사용 가능한 연결이 없는 경우) 또는 -1에서 무기한 대기하는 최대 시간입니다.', true, 'Database-Postgres');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '500 millis' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values( (SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'Max Total Connections', '이 풀에서 동시에 할당할 수 있는 최대 활성 연결 수 또는 제한이 없는 경우 음수입니다.', true, 'Database-Postgres');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '8' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values( (SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'Validation-query', '연결을 반환하기 전에 연결을 확인하는 데 사용되는 유효성 검사 쿼리입니다. 연결이 잘못되면 연결이 삭제되고 새 유효한 연결이 반환됩니다. 참고!! 검증을 사용하면 성능 저하가 있을 수 있습니다.', false, 'Database-Postgres');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), null );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values( (SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'dbcp-min-idle-conns', '추가 연결을 만들지 않고 풀에서 유휴 상태로 유지할 수 있는 최소 연결 수입니다. 유휴 연결을 허용하지 않으려면 0으로 설정합니다.', false, 'Database-Postgres');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '0' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values( (SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'dbcp-max-idle-conns', '풀에서 추가 연결을 해제하지 않고 유휴 상태로 유지할 수 있는 최대 연결 수입니다. 무제한 유휴 연결을 허용하려면 음수 값으로 설정합니다.', false, 'Database-Postgres');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '8' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values( (SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'dbcp-max-conn-lifetime', '연결의 최대 수명(밀리초)입니다. 이 시간을 초과하면 연결이 다음 활성화, 패시베이션 또는 유효성 검사 테스트에 실패합니다. 0 이하의 값은 연결의 수명이 무한하다는 것을 의미합니다.', false, 'Database-Postgres');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '-1' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values( (SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'dbcp-time-between-eviction-runs', '유휴 연결 제거 스레드를 실행하는 동안 절전 모드로 전환하는 시간(밀리초)입니다. 양성이 아닌 경우 유휴 연결 제거 스레드가 실행되지 않습니다.', false, 'Database-Postgres');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '-1' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values( (SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'dbcp-min-evictable-idle-time', '제거 대상이 되기 전에 연결이 풀에서 유휴 상태로 있을 수 있는 최소 시간입니다.', false, 'Database-Postgres');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '30 mins' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values( (SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'dbcp-soft-min-evictable-idle-time', '최소 수의 유휴 연결이 풀에 남아 있어야 하는 추가 조건을 포함하여 유휴 연결 제거기에 의해 제거될 수 있기 전에 연결이 풀에서 유휴 상태로 있을 수 있는 최소 시간입니다. 이 옵션의 not-soft 버전이 양의 값으로 설정된 경우 유휴 연결 제거기가 먼저 검사합니다. 이 제거기가 유휴 연결을 방문할 때 유휴 시간을 먼저 해당 연결과 비교한 다음 최소 유휴 연결을 포함하여 이 소프트 옵션과 비교합니다. ', false, 'Database-Postgres');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '-1' );


--  Database-MySQL collector 구간 : SQL select query는 선택 값이지만, 흐름 상 true로 설정
INSERT INTO ingest_manager.adaptor (adaptor_type, adaptor_name, nifi_name, nifi_type) values ('collector', 'Database-MySQL', 'Database-MySQL', 'Processor');
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'sql-pre-query', '기본 SQL 쿼리가 실행되기 전에 실행된 세미콜론으로 구분된 쿼리 목록입니다. 예를 들어, 기본 쿼리 앞에 세션 속성을 설정합니다. 세미콜론은 백슬래시(''\\;'')로 이스케이프하여 문 자체에 포함할 수 있습니다. 오류가 없으면 이러한 쿼리의 결과/출력이 억제됩니다.', false, 'Database-MySQL');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), null );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'SQL select query', '실행할 SQL 선택 쿼리입니다. 쿼리는 비어 있거나 상수 값이거나 표현식 언어를 사용하는 속성을 사용하여 작성할 수 있습니다. 이 속성을 지정하면 수신 흐름 파일의 내용에 관계없이 사용됩니다. 이 속성이 비어 있으면 수신 흐름 파일의 내용에 유효한 SQL 선택 쿼리가 포함되어 프로세서에서 데이터베이스에 발급됩니다. 표현식 언어는 흐름 파일 내용에 대해 평가되지 않습니다.', true, 'Database-MySQL'); -- 흐름 상 true
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), null );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'sql-post-query', '기본 SQL 쿼리가 실행된 후 실행되는 세미콜론으로 구분된 쿼리 목록입니다. 주 쿼리 후에 세션 속성을 설정하는 것과 같은 예입니다. 세미콜론은 백슬래시(''\\;'')로 이스케이프하여 문 자체에 포함할 수 있습니다. 오류가 없으면 이러한 쿼리의 결과/출력이 억제됩니다.', false, 'Database-MySQL');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), null );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'Max Wait Time', '실행 중인 SQL 선택 쿼리에 허용되는 최대 시간입니다. 0은 제한이 없음을 의미합니다. 1초 미만의 최대 시간은 0과 같습니다.', false, 'Database-MySQL');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '0 seconds' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'dbf-normalize', '열 이름의 Avro 호환되지 않는 문자를 Avro 호환 문자로 변경할지 여부입니다. 예를 들어 유효한 Avro 레코드를 작성하기 위해 콜론과 마침표가 밑줄로 변경됩니다.', true, 'Database-MySQL');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), 'false,true' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'dbf-user-logical-types', 'DECIMAL/NUMBER, DATE, TIME 및 TIMESTAMP 열에 Avro Logical Types를 사용할지 여부를 지정합니다. 비활성화된 경우 문자열로 기록됩니다. 활성화된 경우 논리 유형이 기본 유형으로 사용되고 기록되며, 특히 논리적 "10진수"로 DECIMAL/NUMBER: 추가 정밀도와 스케일 메타데이터를 가진 바이트로 기록되고 논리적 "date-millis"로 기록되며, 논리적 "time-millis"로 기록되고, 논리적 "time-millis"로 기록되고, 논리적 "time-millis"로 기록됩니다.s는 유닉스 에포크 이후로, 타임스탬프는 논리적 "타임스탬프-millis": 유닉스 에포크 이후로 밀리초를 나타내는 긴 시간으로 작성되었다. 작성된 아브로 레코드의 독자가 이러한 논리적 유형도 알고 있다면, 이러한 값들은 독자 구현에 따라 더 많은 컨텍스트에서 역직렬화될 수 있다.', true, 'Database-MySQL');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), 'false,true' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'compression-format', 'Avro 파일을 쓸 때 사용할 압축 유형입니다. 기본값은 없음입니다.', true, 'Database-MySQL');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), 'NONE,BZIP2,DEFLATE,SNAPPY,LZO' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'dbf-default-precision', 'DECIMAL/NUMBER 값이 "decimal" Avological type으로 작성되는 경우 사용 가능한 자릿수를 나타내는 특정 "precision"이 필요합니다. 일반적으로 정밀도는 열 데이터 유형 정의 또는 데이터베이스 엔진 기본값으로 정의됩니다. 그러나 정의되지 않은 정밀도(0)는 일부 데이터베이스 엔진에서 반환될 수 있습니다. 정의되지 않은 정밀도 숫자를 작성할 때 ''Default Decimal Precision''이 사용됩니다.', true, 'Database-MySQL');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '10' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'dbf-default-scale', 'DECIMAL/NUMBER 값이 "decimal" Avological type으로 작성되는 경우 사용 가능한 10진수 숫자를 나타내는 특정 "scale"이 필요합니다. 일반적으로 축척은 열 데이터 유형 정의 또는 데이터베이스 엔진 기본값으로 정의됩니다. 그러나 정의되지 않은 정밀도(0)가 반환될 때 일부 데이터베이스 엔진에서는 스케일이 불확실할 수도 있습니다. 정의되지 않은 숫자를 쓸 때 "기본 소수점 척도"가 사용됩니다. 값에 지정된 척도보다 많은 소수점이 있는 경우 값은 반올림됩니다. 예를 들어 1.53은 척도 0에서 2가 되고 1.5는 척도 1이 됩니다.', true, 'Database-MySQL');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '0' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'esql-max-rows', '단일 FlowFile에 포함될 최대 결과 행 수입니다. 이렇게 하면 매우 큰 결과 집합을 여러 FlowFiles로 분할할 수 있습니다. 지정한 값이 0이면 모든 행이 단일 FlowFile로 반환됩니다.', true, 'Database-MySQL');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '0' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'esql-output-batch-size', '프로세스 세션을 커밋하기 전에 대기열에 넣을 출력 흐름 파일 수입니다. 0으로 설정하면 모든 결과 집합 행이 처리되고 출력 흐름 파일이 다운스트림 관계로 전송될 준비가 되면 세션이 커밋됩니다. 결과 세트가 큰 경우 프로세서 실행이 끝날 때 대량의 FlowFiles가 전송될 수 있습니다. 이 속성이 설정된 경우 지정된 수의 FlowFiles가 전송 준비가 되면 세션이 커밋되어 FlowFiles가 다운스트림 관계로 해제됩니다. 참고: fragment.count 특성은 이 속성을 설정할 때 FlowFiles에서 설정되지 않습니다.', true, 'Database-MySQL');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '0' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'esql-fetch-size', '한 번에 결과 집합에서 가져올 결과 행 수입니다. 이는 데이터베이스 드라이버에 대한 힌트이며, 이를 준수하지 않거나 정확하지 않을 수 있습니다. 지정한 값이 0이면 힌트가 무시됩니다.', true, 'Database-MySQL');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '0' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'esql-auto-commit', 'DB 연결의 자동 커밋 기능을 활성화하거나 비활성화합니다. 기본값은 ''true''입니다. 기본값은 대부분의 JDBC 드라이버에서 사용할 수 있으며 이 프로세서가 데이터를 읽는 데 사용되기 때문에 대부분의 경우 이 기능은 영향을 미치지 않습니다. 그러나 Postgre와 같은 일부 JDBC 드라이버의 경우SQL 드라이버. 한 번에 가져오는 결과 행 수를 제한하려면 자동 커밋 기능을 사용하지 않도록 설정해야 합니다. 자동 커밋이 사용하도록 설정된 경우 postgreSQL 드라이버는 전체 결과 집합을 한 번에 메모리에 로드합니다. 이로 인해 대용량 데이터 세트를 가져오는 쿼리를 실행할 때 메모리 사용량이 많을 수 있습니다. Postgre에서 이 동작에 대한 자세한 내용SQL 드라이버는 https://jdbc.postgresql.org//recommunication/head/recommunication.html에서 찾을 수 있습니다.', true, 'Database-MySQL');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), 'true,false' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'Scheduling', 'API 호출 스케줄링(단위 = sec)', true, 'Database-MySQL');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '120 sec' );
-- Controller 구간 : Database user와 password는 nifi 상에서는 선택 값이지만, 흐름 상 필수가 되어야 해서 true로 설정
INSERT INTO ingest_manager.adaptor (adaptor_type, adaptor_name, nifi_name, nifi_type) values ('collector', 'Database-MySQL', 'DBCPConnectionPool', 'Controller');
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values( (SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'Database Connection URL', '데이터베이스에 연결하는 데 사용되는 데이터베이스 연결 URL입니다. 데이터베이스 시스템 이름, 호스트, 포트, 데이터베이스 이름 및 일부 매개 변수를 포함할 수 있습니다. 데이터베이스 연결 URL의 정확한 구문은 DBMS에서 지정합니다.', true, 'Database-MySQL');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), 'jdbc:mysql://mysql:3306/mytest' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values( (SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'Database User', '데이터베이스 사용자 이름', true, 'Database-MySQL'); -- 흐름 상 true가 되어야 함
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), null );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values( (SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'Password', '데이터베이스 사용자의 암호', true, 'Database-MySQL'); -- 흐름 상 true가 되어야 함
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), null );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values( (SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'Max Wait Time', '연결이 반환될 때까지 풀에서 대기하는 최대 시간(사용 가능한 연결이 없는 경우) 또는 -1에서 무기한 대기하는 최대 시간입니다. ', true, 'Database-MySQL');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '500 millis' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values( (SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'Max Total Connections', '이 풀에서 동시에 할당할 수 있는 최대 활성 연결 수 또는 제한이 없는 경우 음수입니다.', true, 'Database-MySQL');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '8' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values( (SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'Validation-query', '연결을 반환하기 전에 연결을 확인하는 데 사용되는 유효성 검사 쿼리입니다. 연결이 잘못되면 연결이 삭제되고 새 유효한 연결이 반환됩니다. 참고!! 검증을 사용하면 성능 저하가 있을 수 있습니다.', false, 'Database-MySQL');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), null );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values( (SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'dbcp-min-idle-conns', '추가 연결을 만들지 않고 풀에서 유휴 상태로 유지할 수 있는 최소 연결 수입니다. 유휴 연결을 허용하지 않으려면 0으로 설정합니다.', false, 'Database-MySQL');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '0' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values( (SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'dbcp-max-idle-conns', '풀에서 추가 연결을 해제하지 않고 유휴 상태로 유지할 수 있는 최대 연결 수입니다. 무제한 유휴 연결을 허용하려면 음수 값으로 설정합니다.', false, 'Database-MySQL');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '8' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values( (SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'dbcp-max-conn-lifetime', '연결의 최대 수명(밀리초)입니다. 이 시간을 초과하면 연결이 다음 활성화, 패시베이션 또는 유효성 검사 테스트에 실패합니다. 0 이하의 값은 연결의 수명이 무한하다는 것을 의미합니다.', false, 'Database-MySQL');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '-1' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values( (SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'dbcp-time-between-eviction-runs', '유휴 연결 제거 스레드를 실행하는 동안 절전 모드로 전환하는 시간(밀리초)입니다. 양성이 아닌 경우 유휴 연결 제거 스레드가 실행되지 않습니다.', false, 'Database-MySQL');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '-1' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values( (SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'dbcp-min-evictable-idle-time', '제거 대상이 되기 전에 연결이 풀에서 유휴 상태로 있을 수 있는 최소 시간입니다.', false, 'Database-MySQL');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '30 mins' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values( (SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'dbcp-soft-min-evictable-idle-time', '최소 수의 유휴 연결이 풀에 남아 있어야 하는 추가 조건을 포함하여 유휴 연결 제거기에 의해 제거될 수 있기 전에 연결이 풀에서 유휴 상태로 있을 수 있는 최소 시간입니다. 이 옵션의 not-soft 버전이 양의 값으로 설정된 경우 유휴 연결 제거기가 먼저 검사합니다. 이 제거기가 유휴 연결을 방문할 때 유휴 시간을 먼저 해당 연결과 비교한 다음 최소 유휴 연결을 포함하여 이 소프트 옵션과 비교합니다.', false, 'Database-MySQL');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '-1' );



--  Database-MariaDB collector 구간 : SQL select query는 선택 값이지만, 흐름 상 true로 설정
INSERT INTO ingest_manager.adaptor (adaptor_type, adaptor_name, nifi_name, nifi_type) values ('collector', 'Database-MariaDB', 'Database-MariaDB', 'Processor');
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'sql-pre-query', '기본 SQL 쿼리가 실행되기 전에 실행된 세미콜론으로 구분된 쿼리 목록입니다. 예를 들어, 기본 쿼리 앞에 세션 속성을 설정합니다. 세미콜론은 백슬래시(''\\;'')로 이스케이프하여 문 자체에 포함할 수 있습니다. 오류가 없으면 이러한 쿼리의 결과/출력이 억제됩니다.', false, 'Database-MariaDB');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), null );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'SQL select query', '실행할 SQL 선택 쿼리입니다. 쿼리는 비어 있거나 상수 값이거나 표현식 언어를 사용하는 속성을 사용하여 작성할 수 있습니다. 이 속성을 지정하면 수신 흐름 파일의 내용에 관계없이 사용됩니다. 이 속성이 비어 있으면 수신 흐름 파일의 내용에 유효한 SQL 선택 쿼리가 포함되어 프로세서에서 데이터베이스에 발급됩니다. 표현식 언어는 흐름 파일 내용에 대해 평가되지 않습니다.', true, 'Database-MariaDB'); -- 흐름 상 true
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), null );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'sql-post-query', '기본 SQL 쿼리가 실행된 후 실행되는 세미콜론으로 구분된 쿼리 목록입니다. 주 쿼리 후에 세션 속성을 설정하는 것과 같은 예입니다. 세미콜론은 백슬래시(''\\;'')로 이스케이프하여 문 자체에 포함할 수 있습니다. 오류가 없으면 이러한 쿼리의 결과/출력이 억제됩니다.', false, 'Database-MariaDB');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), null );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'Max Wait Time', '실행 중인 SQL 선택 쿼리에 허용되는 최대 시간입니다. 0은 제한이 없음을 의미합니다. 1초 미만의 최대 시간은 0과 같습니다.', false, 'Database-MariaDB');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '0 seconds' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'dbf-normalize', '열 이름의 Avro 호환되지 않는 문자를 Avro 호환 문자로 변경할지 여부입니다. 예를 들어 유효한 Avro 레코드를 작성하기 위해 콜론과 마침표가 밑줄로 변경됩니다.', true, 'Database-MariaDB');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), 'false,true' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'dbf-user-logical-types', 'DECIMAL/NUMBER, DATE, TIME 및 TIMESTAMP 열에 Avro Logical Types를 사용할지 여부를 지정합니다. 비활성화된 경우 문자열로 기록됩니다. 활성화된 경우 논리 유형이 기본 유형으로 사용되고 기록되며, 특히 논리적 "10진수"로 DECIMAL/NUMBER: 추가 정밀도와 스케일 메타데이터를 가진 바이트로 기록되고 논리적 "date-millis"로 기록되며, 논리적 "time-millis"로 기록되고, 논리적 "time-millis"로 기록되고, 논리적 "time-millis"로 기록됩니다.s는 유닉스 에포크 이후로, 타임스탬프는 논리적 "타임스탬프-millis": 유닉스 에포크 이후로 밀리초를 나타내는 긴 시간으로 작성되었다. 작성된 아브로 레코드의 독자가 이러한 논리적 유형도 알고 있다면, 이러한 값들은 독자 구현에 따라 더 많은 컨텍스트에서 역직렬화될 수 있다.', true, 'Database-MariaDB');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), 'false,true' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'compression-format', 'Avro 파일을 쓸 때 사용할 압축 유형입니다. 기본값은 없음입니다.', true, 'Database-MariaDB');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), 'NONE,BZIP2,DEFLATE,SNAPPY,LZO' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'dbf-default-precision', 'DECIMAL/NUMBER 값이 "decimal" Avological type으로 작성되는 경우 사용 가능한 자릿수를 나타내는 특정 "precision"이 필요합니다. 일반적으로 정밀도는 열 데이터 유형 정의 또는 데이터베이스 엔진 기본값으로 정의됩니다. 그러나 정의되지 않은 정밀도(0)는 일부 데이터베이스 엔진에서 반환될 수 있습니다. 정의되지 않은 정밀도 숫자를 작성할 때 ''Default Decimal Precision''이 사용됩니다.', true, 'Database-MariaDB');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '10' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'dbf-default-scale', 'DECIMAL/NUMBER 값이 "decimal" Avological type으로 작성되는 경우 사용 가능한 10진수 숫자를 나타내는 특정 "scale"이 필요합니다. 일반적으로 축척은 열 데이터 유형 정의 또는 데이터베이스 엔진 기본값으로 정의됩니다. 그러나 정의되지 않은 정밀도(0)가 반환될 때 일부 데이터베이스 엔진에서는 스케일이 불확실할 수도 있습니다. 정의되지 않은 숫자를 쓸 때 "기본 소수점 척도"가 사용됩니다. 값에 지정된 척도보다 많은 소수점이 있는 경우 값은 반올림됩니다. 예를 들어 1.53은 척도 0에서 2가 되고 1.5는 척도 1이 됩니다.', true, 'Database-MariaDB');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '0' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'esql-max-rows', '단일 FlowFile에 포함될 최대 결과 행 수입니다. 이렇게 하면 매우 큰 결과 집합을 여러 FlowFiles로 분할할 수 있습니다. 지정한 값이 0이면 모든 행이 단일 FlowFile로 반환됩니다.', true, 'Database-MariaDB');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '0' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'esql-output-batch-size', '프로세스 세션을 커밋하기 전에 대기열에 넣을 출력 흐름 파일 수입니다. 0으로 설정하면 모든 결과 집합 행이 처리되고 출력 흐름 파일이 다운스트림 관계로 전송될 준비가 되면 세션이 커밋됩니다. 결과 세트가 큰 경우 프로세서 실행이 끝날 때 대량의 FlowFiles가 전송될 수 있습니다. 이 속성이 설정된 경우 지정된 수의 FlowFiles가 전송 준비가 되면 세션이 커밋되어 FlowFiles가 다운스트림 관계로 해제됩니다. 참고: fragment.count 특성은 이 속성을 설정할 때 FlowFiles에서 설정되지 않습니다.', true, 'Database-MariaDB');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '0' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'esql-fetch-size', '한 번에 결과 집합에서 가져올 결과 행 수입니다. 이는 데이터베이스 드라이버에 대한 힌트이며, 이를 준수하지 않거나 정확하지 않을 수 있습니다. 지정한 값이 0이면 힌트가 무시됩니다.', true, 'Database-MariaDB');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '0' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'esql-auto-commit', 'DB 연결의 자동 커밋 기능을 활성화하거나 비활성화합니다. 기본값은 ''true''입니다. 기본값은 대부분의 JDBC 드라이버에서 사용할 수 있으며 이 프로세서가 데이터를 읽는 데 사용되기 때문에 대부분의 경우 이 기능은 영향을 미치지 않습니다. 그러나 Postgre와 같은 일부 JDBC 드라이버의 경우SQL 드라이버. 한 번에 가져오는 결과 행 수를 제한하려면 자동 커밋 기능을 사용하지 않도록 설정해야 합니다. 자동 커밋이 사용하도록 설정된 경우 postgreSQL 드라이버는 전체 결과 집합을 한 번에 메모리에 로드합니다. 이로 인해 대용량 데이터 세트를 가져오는 쿼리를 실행할 때 메모리 사용량이 많을 수 있습니다. Postgre에서 이 동작에 대한 자세한 내용SQL 드라이버는 https://jdbc.postgresql.org//recommunication/head/recommunication.html에서 찾을 수 있습니다.', true, 'Database-MariaDB');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), 'true,false' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'Scheduling', 'API 호출 스케줄링(단위 = sec)', true, 'Database-MariaDB');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '120 sec' );
-- Controller 구간 : Database user와 password는 nifi 상에서는 선택 값이지만, 흐름 상 필수가 되어야 해서 true로 설정
INSERT INTO ingest_manager.adaptor (adaptor_type, adaptor_name, nifi_name, nifi_type) values ('collector', 'Database-MariaDB', 'DBCPConnectionPool', 'Controller');
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'Database Connection URL', '데이터베이스에 연결하는 데 사용되는 데이터베이스 연결 URL입니다. 데이터베이스 시스템 이름, 호스트, 포트, 데이터베이스 이름 및 일부 매개 변수를 포함할 수 있습니다. 데이터베이스 연결 URL의 정확한 구문은 DBMS에서 지정합니다.', true, 'Database-MariaDB');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), 'jdbc:mariadb://mariadb:13306/mytest' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'Database User', '데이터베이스 사용자 이름', true, 'Database-MariaDB'); -- 흐름 상 true가 되어야 함
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), null );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'Password', '데이터베이스 사용자의 암호', true, 'Database-MariaDB'); -- 흐름 상 true가 되어야 함
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), null );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'Max Wait Time', '연결이 반환될 때까지 풀에서 대기하는 최대 시간(사용 가능한 연결이 없는 경우) 또는 -1에서 무기한 대기하는 최대 시간입니다. ', true, 'Database-MariaDB');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '500 millis' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'Max Total Connections', '이 풀에서 동시에 할당할 수 있는 최대 활성 연결 수 또는 제한이 없는 경우 음수입니다.', true, 'Database-MariaDB');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '8' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'Validation-query', '연결을 반환하기 전에 연결을 확인하는 데 사용되는 유효성 검사 쿼리입니다. 연결이 잘못되면 연결이 삭제되고 새 유효한 연결이 반환됩니다. 참고!! 검증을 사용하면 성능 저하가 있을 수 있습니다.', false, 'Database-MariaDB');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), null );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'dbcp-min-idle-conns', '추가 연결을 만들지 않고 풀에서 유휴 상태로 유지할 수 있는 최소 연결 수입니다. 유휴 연결을 허용하지 않으려면 0으로 설정합니다.', false, 'Database-MariaDB');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '0' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'dbcp-max-idle-conns', '풀에서 추가 연결을 해제하지 않고 유휴 상태로 유지할 수 있는 최대 연결 수입니다. 무제한 유휴 연결을 허용하려면 음수 값으로 설정합니다.', false, 'Database-MariaDB');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '8' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'dbcp-max-conn-lifetime', '연결의 최대 수명(밀리초)입니다. 이 시간을 초과하면 연결이 다음 활성화, 패시베이션 또는 유효성 검사 테스트에 실패합니다. 0 이하의 값은 연결의 수명이 무한하다는 것을 의미합니다.', false, 'Database-MariaDB');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '-1' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'dbcp-time-between-eviction-runs', '유휴 연결 제거 스레드를 실행하는 동안 절전 모드로 전환하는 시간(밀리초)입니다. 양성이 아닌 경우 유휴 연결 제거 스레드가 실행되지 않습니다.', false, 'Database-MariaDB');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '-1' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'dbcp-min-evictable-idle-time', '제거 대상이 되기 전에 연결이 풀에서 유휴 상태로 있을 수 있는 최소 시간입니다.', false, 'Database-MariaDB');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '30 mins' );
INSERT INTO ingest_manager.properties (adaptor_id, property_name, detail, is_required, adaptor_name) values((SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'dbcp-soft-min-evictable-idle-time', '최소 수의 유휴 연결이 풀에 남아 있어야 하는 추가 조건을 포함하여 유휴 연결 제거기에 의해 제거될 수 있기 전에 연결이 풀에서 유휴 상태로 있을 수 있는 최소 시간입니다. 이 옵션의 not-soft 버전이 양의 값으로 설정된 경우 유휴 연결 제거기가 먼저 검사합니다. 이 제거기가 유휴 연결을 방문할 때 유휴 시간을 먼저 해당 연결과 비교한 다음 최소 유휴 연결을 포함하여 이 소프트 옵션과 비교합니다.', false, 'Database-MariaDB');
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), '-1' );


-- filter 구간 --
INSERT INTO ingest_manager.adaptor (adaptor_type, adaptor_name, nifi_name, nifi_type) values ('filter', 'filter', 'Base64Decoder', 'Processor');
INSERT INTO ingest_manager.properties (adaptor_name, adaptor_id, property_name, detail, is_required) values( (SELECT adaptor_name FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), (SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'isBase64', null, true);
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), 'true,false' );
INSERT INTO ingest_manager.adaptor (adaptor_type, adaptor_name, nifi_name, nifi_type) values ('filter', 'filter', 'RootKeyFinder', 'Processor');
INSERT INTO ingest_manager.properties (adaptor_name, adaptor_id, property_name, detail, is_required) values( (SELECT adaptor_name FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), (SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'root_key', null, true);
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), null );


-- converter 구간 --
INSERT INTO ingest_manager.adaptor (adaptor_type, adaptor_name, nifi_name, nifi_type) values ('converter', 'converter', 'IDGenerater', 'Processor');
INSERT INTO ingest_manager.properties (adaptor_name, adaptor_id, property_name, detail, is_required) values( (SELECT adaptor_name FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), (SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'level1', null, true);
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), null );
INSERT INTO ingest_manager.properties (adaptor_name, adaptor_id, property_name, detail, is_required) values( (SELECT adaptor_name FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), (SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'level2', null, true);
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), null );
INSERT INTO ingest_manager.properties (adaptor_name, adaptor_id, property_name, detail, is_required) values( (SELECT adaptor_name FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), (SELECT id FROM ingest_manager.adaptor ORDER BY ID DESC LIMIT 1), 'level3', null, true);
INSERT INTO ingest_manager.properties_value (property_id, value) values ( (SELECT id FROM ingest_manager.properties ORDER BY ID DESC LIMIT 1), null );