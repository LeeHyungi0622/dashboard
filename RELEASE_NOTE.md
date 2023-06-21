# Version 1.1
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
### [Fix] Protocal 변경 시, UI URL 종속성 해결
- 기존 Ingest Manager 설치 시, 백엔드의 SSL 설정 여부에 따라 Front UI의 URL을 수동으로 바꿔주고 다시 빌드 후 배포하는 비효율적인 배포 방법을 개선.
- 백엔드의 Protocal을 자동으로 인식하여 Front URL을 설정할 수 있도록 종속성 추가

*Contributer : Justin* 
### [Fix] DataModel의 ID와 Type 분리 적용 지원
- 기존에는 DataCore에서 DataModel의 ID와 Type이 같을 경우에만 Ingest Manager에서 정상적인 작동이 되었던 로직을 다를 때에도 정상적으로 파이프라인을 생성할 수 있게 하여 사용성 개선.

*Contributer : Justin* 