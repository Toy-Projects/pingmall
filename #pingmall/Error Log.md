# Error log

1. 테스트 실행 시 `@DisplayName`이 적용이 안되고, 테스트 메소드 명이 보임
  - `Preferences` -> `Build, Execution, Deployment` -> `Build Tools` -> `Gradle`
    - `Run tests using`
      - `Gradle` -> `IntelliJ IDEA` 로 변경
      
2. 제품 불러오는 테스트 시 `Infinite Recursion` 에러 발생
  - `@JsonIdentityInfo(Jackson 2.0+)`를 사용하여 해결
    - `JSON` 타입으로 엔티티를 변환할때 `@Id`를 바탕으로 중복된 아이디는 `JSON`으로 변환시키지 않음 