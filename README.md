# #pingmall
쇼핑몰의 전반적인 시스템을 구현해보기 위한 토이 프로젝트
<!-- 
## [DATA MODEL](https://www.notion.so/yks095/DATA-MODEL-7675b80f84e64a9c9398d236560471a8)

## [TABLE](https://www.notion.so/yks095/TABLE-cb15f18742df4ee5a6ac0a45edce3cdc)

## [API SPEC](https://www.notion.so/yks095/API-SPEC-3ea457e5507145719f3159a2525f6895) -->

|Desc          |Doc|
|:---:     |:---:|
|Data Model|[  link to notion  ](https://www.notion.so/yks095/DATA-MODEL-7675b80f84e64a9c9398d236560471a8)|
|Table     |[  link to notion  ](https://www.notion.so/yks095/TABLE-cb15f18742df4ee5a6ac0a45edce3cdc)|
|API Spec  |[  link to notion  ](https://www.notion.so/yks095/API-SPEC-3ea457e5507145719f3159a2525f6895)|

## 개발환경
|도구|버전|
|:---:|:---:|
|Spring|Spring Boot 2.3.0|
|운영체제|Mac OS X|
|개발 툴|IntelliJ IDEA Ultimate|
|JDK|JDK 8(>=8)|
|빌드 툴|Gradle 6.3.x|
|데이터베이스|MySQL 8.0.19|

## 설정
<details><summary> 세부 사항 </summary>

<br>

- `#pingmall/src/main/resources` 하위에 `application.yml`생성 후 아래의 내용 삽입
```
spring:
  datasource:                           # DB 관련 설정
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/{DB_NAME}?characterEncoding=utf8&serverTimezone=UTC
    username: {DB_USER}
    password: {DB_PASSWORD}
  jpa:                                  # JPA 관련 설정
    show-sql: true
    hibernate:
      ddl-auto: create
    open-in-view: false
  h2:                                   # H2 관련 설정
    console:
      enabled: true
  servlet:
    multipart:                          # 파일 업로드 관련 설정
      enabled: true
      max-file-size: 200MB
      max-request-size: 215MB
  mail:                                 # 이메일 전송 관련 설정
    host: smtp.gmail.com
    port: 587
    username: {GMAIL_ID_TO_SEND_EMAIL}
    password: {GMAIL_PASSWORD_TO_SEND_EMAIL}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
  output:                               # Error Log.md 참조 요망
    ansi:
      enabled: always

app-property:                           # Test Code 에서 Type Safety를 위해 자주 사용되는 값 설정
  my-email: {ANY_REAL_EMAIL_FOR_TEST_CODE}
  test-email: test@email.com
  test-password: testPW123!
  test-modified-password: modifiedPW123!
  test-name: testName
  test-balance: 9999999
  test-modified-name: modifiedName
  test-address: testAddress
  test-modified-address: modifiedAddress
  test-product-name: testProductName
  test-modified-product-name: modifiedProductName
  test-size: 265
  test-modified-size: 270
  test-image: testImage
  test-modified-image: modifiedImage
  test-price: 20000
  test-modified-price: 10000
  test-stock: 10
  test-modified-stock: 5
  test-amount: 2
  test-content: testContent
  test-modified-content: modifiedContent

image:                                  # 이미지 다운로드시 임의로 프로젝트 내 images폴더에 저장
  location: ./images

logging:                                # 로그 관련 설정
  file:
    path: ./logs
    max-size: 10MB
    max-history: 1
```
</details>