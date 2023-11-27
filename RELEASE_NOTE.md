# Version 1.2.0 - 2023. 08. 28
### [Feature] Object 타입 지원
- 기존 Object Type에 대해 Ingest를 지원하지 못하였지만, Object Type도 정상 적재할 수 있는 기능 추가
- 관련 설정 메뉴얼은 [Object Type Ingest Manual]() 참조

 *Contributer : Justin* 

### [Refactor] 전반적인 코드 개선
- 기존 `nifi` package 안의 기능 별로 분리되어 있지 않은 코드를 분리하여 repackaging
- 이러한 이슈로 NiFi UI에 접속하여 템플릿 삭제 후 Ingest Manager를 재 구동하는 불편함 개선.
- History 기능에 대해 AOP를 이용하는 로직으로 변경

 *Contributer : Justin* 

### [Feature] NiFi Version 1.23.0 Migration
- 기존 Ingest Manager에서 사용하고 있던 NiFi의 Version은 `1.16.3`에서 `1.23.0`으로 업데이트.
- **Developer** NiFi Version Up migration Guide는 [링크]()참조.

 *Contributer : Justin* 

### [Feature] NiFi Template 업로드 로직 수정
- 기존 NiFi Template Upload 시, 삭제 후 업로드 하는 로직이 적용이 되어 있지 않아 템플릿 변경 건에도 반영이 되지 않는 이슈 존재.
- 이러한 이슈로 NiFi UI에 접속하여 템플릿 삭제 후 Ingest Manager를 재 구동하는 불편함 개선.
- application.yml의 `nifi.template.init`을 `true`로 할 경우, Ingest Manager 재 구동시, 삭제 후 업로드 하는 로직 동작

 *Contributer : Justin* 

### [Docs] IngestManager Front-end Refactoring
- 컴포넌트에 적용되어 있는 CSS를 TailwindCSS를 적용하여 통일성 있게 수정 (Common.css, HTML inline-style, component scoped style)
- 화살표 함수(arrow function)와 일반 함수가 혼재되어 있는 부분을 통일성 있게 수정
- 사용되지 않는 코드 삭제
- 정규 표현식 처리 부분을 공통화하여 수정 (입력값에 대한 빈 공백 처리 등)

 *Contributer : Mike* 
 ---
# Version 1.1.3 - 2023. 08. 04
### [Fix, Perf] NiFi Disk 부하 관련 로직 수정
- 특정 데이터 수집 시, NiFi에서 데이터 처리 도중 데이터 용량이 급증 (특히, Filter 구성 요소 중 SplitJson Processor에서 발생)하여 정상적인 동작을 하지 않음.
- Ingest Manager에서 NiFi를 통해 데이터 전처리를 진행할 때, NiFi 데이터 단위인 Flowfile 내부의 Attribute에 원본데이터와 Root Key를 통해 정제된 데이터를 둘 다 저장하여 전처리를 진행하는 로직으로 구성되어 있음.
- 이에 따라 원본 데이터 (API 호출 시, 가져오는 원본데이터)의 크기가 커질 수록 (이슈 리포트에 사용되었던 API 호출 시, 4MB정도의 원본 데이터 발생) NiFi의 데이터 부하가 심하게 걸리는 로직임을 확인
- 또한, 특히 부하가 심하게 생겼던 SplitJson Processor를 거쳐 Flowfile이 나눠질 경우 나눠진 Flowfile 내의 Attribute의 속성 값은 원본 Flowfile 그대로 가져가는 특성 때문에 나눠진 하나의 Flowfile Size가 [원본데이터 + Root Key를 통해 정제된 데이터]로 구성되는 것을 확인
- 이러한 부하를 해결하기 위해 기존 Attribute에 저장하고 있는 원본데이터(`root_key`)와 Root Key를 통해 정제된 데이터(`filtered_data`)를 초기화하는 프로세서를 Filter Adaptor에 추가로 구성하여 해당 이슈에 대해 해결하였고, Convertor 부분에 JsonPath를 이용하기 위해 `convert_data`라는 attribute를 추가하는 UpdataAttribute Processor를 추가 구성함.

 *Contributer : Justin* 
 ---
# Version 1.1.2 - 2023. 07. 27
### [Fix] Root Key 에러 수정
- 기존 root key가 없을 경우 NiFi에서는 Attribute에 ` `(빈칸)을 추가해서 처리했음.
- 이에 따라 Array Type에 대해 지원하기 위해 raw data에서 root key 하위의 `[]`리스트 데이터를 가져오는 도중 root key가 빈칸이여서 에러 발생
- root key가 빈칸일 경우, root key를 제외하는 로직 추가

 *Contributer : Justin* 
### [Refactor] NiFi Data Type Convert 로직 분리
- 기존 NiFi에서 데이터 타입을 분리할 경우, 하나의 메서드에서 수행했으나, 코드 유지보수가 어렵다고 판단하여 `NiFiConvert` Class를 도입하여 코드 가독성 개선.
- 또한 NiFi 내부 템플릿 중 `ConvertDateType` Processor내에서 `Date` 타입 뿐만이 아닌 여러 데이터 타입 (`GeoJson`, `Array` ... etc)에 대한 변경이 이루어져 추후 유지보수가 어렵다고 판단하여 `Convert*Type` 개별 Processor로 분리하여 유지보수에 용이하게 함.
- 기존 `int, double, bool, array, geojson ..` Type을 Insert 할 때, 쌍따옴표 가 value에 붙어 있어 JoltTransformJSON Processor를 사용하였지만, 이제는 Map to Json이 아닌 String 조합으로 로직이 변경되었기 때문에 해당 프로세서는 사용하지 않음.
- Refactoring은 Version 1.2.x에 Refactoring 완료 예정. 

 *Contributer : Justin* 
### [Fix] 파이프라인 등록 에러 로직 변경
- 기존 파이프라인 등록 에러 시, 보기버튼을 클릭하여 수정할 수 있었으나 파이프라인 등록 에러시 삭제 후 재등록으로 정책 설정.
- 이에 따라 에러 시, 보기 버튼을 비활성화 하고, 실행 버튼 클릭 시 `삭제 후 재등록`메세지 출력

 *Contributer : Justin* 
### [Fix] 원천 데이터 Null 정상 변환
- 기존 원천 데이터 변환 시 원천 데이터 Value가 Null일 경우, 해당 값이 'null' String도 없이 빈칸으로 입력되는 오류 존재
- 기존 Convertor 템플릿으로는 null에 대한 정상 데이터 반환이 어려워 Convertor 템플릿 변경
- NGSI-LD String 조합시 `isEmpty()`와 `ifElse()` NiFi Expression을 통해 Attribute에 null 데이터가 들어가 Empty일 경우 NGSI-LD String 조합 시, 제외하는 로직 추가

 *Contributer : Justin* 

---
# Version 1.1.1 - 2023. 06. 28
### [Feature] 원천 데이터 변환 규칙, 데이터 정제 Root Key 공백 입력 지원
- 기존 원천 데이터 변환 시 공백 및 특수문자(`(,)`) 입력을 지원하지 않아 공공데이터포털 등 타 API 서비스에서 key값에 공백 및 특수문자(`(,)`)가 포함되어 있는 경우 NiFi UI에 직접 접근하여 공백 및 특수문자(`(,)`)를 처리 할 수 있도록 수정했던 번거로움 개선
- 공백이 포함된 경우 UI에 `"원천 데이터"."속성"."면적(제곱킬로)"` 으로 입력하면, NiFi에서는 `$.['원천 데이터'].속성.['면적(제곱킬로)']` 으로 변경하여 처리하는 로직 개발

 *Contributer : Justin* 
### [Feature] 파이프라인 생성 시, NiFI UX 개선
- 기존 UX는 Ingest Manager를 통해 생성한 파이프라인을 NiFi에서 확인할 경우 프로세서 그룹이 중첩되어 표시
- UX를 개선하여 Collector - Filter - Convertor - output port 순으로 정렬 하여 생성.
- 정렬 단위는 `PositionX - 740 * 정렬 순` 으로 설정

*Contributer : Justin* 
---
# Version 1.1.0 - 2023. 06. 22
### [Feature] NiFi Redirect Button 추가
- 설치된 Ingest Manager와 연동되어 있는 NiFi UI에 접근할 수 있는 기능 개발
- 해당 버튼은 파이프라인 목록 우측 상단에 구현

 *Contributer : Jenna* 
### [Feature] 파이프라인 동작 이력 기능 개발
- Ingest Manager를 통해 생성한 파이프라인의 동작 이력을 저장하고 사용자에게 보여주는 기능 개발
- 파이프라인 생성, 수정, 실행, 정지에 대한 동작 이력을 남기고 해당 동작에 대한 상세 정보를 확인할 수 있다.
- 상세 정보는 해당 동작 이력의 자세히 버튼을 클릭하면 확인할 수 있다.
- 관련 기능 상세는 [Pipeline History Manual](/IngestManager/Manual/Pipeline-History-Manual) 참조

*Contributer : DongJin* 
### [Feature] GeoJson, Array 타입 지원
- DataCore에서 DataModel의 Property 타입이 GeoJson, Array* 일 경우 Ingest Manager에서 단순 String 처리 해서 넣었던 로직을
실제 각 타입에 맞게 Ingest 할 수 있도록 기능 개발
- 관련 설정 메뉴얼은 [GeoJson, Array Type Ingest Manual](/IngestManager/Manual/GeoJson,-Array-Type-Ingest-Manual) 참조

*Contributer : Justin* 

### [Feature] NiFi HTTP 지원
- 기존 Ingest Manager에서 NiFi를 사용할 때, SSL이 적용된 HTTPS만을 지원하고 있었음.
- NiFi의 SSL을 적용하지 않는 HTTP 통신을 지원할 수 있도록 추가 개발
- Application.yml의 nifi.url의 String을 읽어 http protocal이면, 토큰 발급/검증/요청 로직을 거치지 않음.
```
nifi:
    url: http://localhost:8008
    user: (아무거나) -> http일 경우 사용하지 않음.
    password: (아무거나) -> http일 경우 사용하지 않음.
```

*Contributer : Justin* 

### [Fix] Protocal 변경 시, UI URL 종속성 해결
- 기존 Ingest Manager 설치 시, 백엔드의 SSL 설정 여부에 따라 Front UI의 URL을 수동으로 바꿔주고 다시 빌드 후 배포하는 비효율적인 배포 방법을 개선.
- 백엔드의 Protocal을 자동으로 인식하여 Front URL을 설정할 수 있도록 종속성 추가

*Contributer : Justin* 
### [Fix] DataModel의 ID와 Type 분리 적용 지원
- 기존에는 DataCore에서 DataModel의 ID와 Type이 같을 경우에만 Ingest Manager에서 정상적인 작동이 되었던 로직을 다를 때에도 정상적으로 파이프라인을 생성할 수 있게 하여 사용성 개선.

*Contributer : Justin* 