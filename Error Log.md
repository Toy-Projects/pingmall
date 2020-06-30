# Error log

1. 테스트 실행 시 `@DisplayName`이 적용이 안되고, 테스트 메소드 명이 보임
  - `Preferences` -> `Build, Execution, Deployment` -> `Build Tools` -> `Gradle`
    - `Run tests using`
      - `Gradle` -> `IntelliJ IDEA` 로 변경
      
2. 제품 불러오는 테스트 시 `Infinite Recursion` 에러 발생
  - `@JsonIdentityInfo(Jackson 2.0+)`를 사용하여 해결
    - `JSON` 타입으로 엔티티를 변환할때 `@Id`를 바탕으로 중복된 아이디는 `JSON`으로 변환시키지 않음 

3. 주문 목록 저장 시 `Java`에서 제공하는 `@Valid` 로는 `Collection` 타입을 검증할 수 없음
  - `Spring`의 `Validator`를 상속받아 `Collection` 타입을 검증하도록 `OrdersValidator(CumsomValidator) 구현`
  - 검증 시 에러가 검출되면 `BindResults`를 파라미터로 넣고 `BindException`를 발생시켜 오류 처리

4. 사용하지 않는 컬럼 삭제 시 연관관계 문제로 denied 됨
  - `mysql -u root -p`로 `MySQL` 접속
  - `use information_scheme;` -> `select * from table_constraints;` -> 해당 `제약조건명` 확인
  - `alter table 테이블명 drop constraint 제약조건명;`
  - `alter table 테이블명 drop 컬럼;`
  - 위의 순서대로 쿼리를 날려 사용하지 않는 컬럼 삭제