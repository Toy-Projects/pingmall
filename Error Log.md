# Error log

### **_1. 테스트 실행 시 `@DisplayName`이 적용이 안되고, 테스트 메소드 명이 보임_**
  - `Preferences` -> `Build, Execution, Deployment` -> `Build Tools` -> `Gradle`
    - `Run tests using`
      - `Gradle` -> `IntelliJ IDEA` 로 변경

<br>

### **_2. 제품 불러오는 테스트 시 `Infinite Recursion` 에러 발생_**
  - `@JsonIdentityInfo(Jackson 2.0+)`를 사용하여 해결
    - `JSON` 타입으로 엔티티를 변환할때 `@Id`를 바탕으로 중복된 아이디는 `JSON`으로 변환시키지 않음 

<br>

### **_3. 주문 목록 저장 시 `Java`에서 제공하는 `@Valid` 로는 `Collection` 타입을 검증할 수 없음_**
  - `Spring`의 `Validator`를 상속받아 `Collection` 타입을 검증하도록 `OrdersValidator(CumsomValidator)` 구현
  - 검증 시 에러가 검출되면 `BindResults`를 파라미터로 넣고 `BindException`를 발생시켜 오류 처리

<br>

### **_4. 사용하지 않는 컬럼 삭제 시 연관관계 문제로 `Denied` 됨_**
  - `mysql -u root -p` 로 `MySQL` 접속
  - `use information_scheme;`
  - `select * from table_constraints;` -> 해당 `제약조건명` 확인
  - `alter table [테이블명] drop constraint [제약조건명];`
  - `alter table [테이블명] drop [컬럼명];`

<br>

### **_5. `HATEOAS` 적용 중 `Deprecated` 된 메소드, 클래스 발견_**
  - `EntityModel.super`
  - `Link`