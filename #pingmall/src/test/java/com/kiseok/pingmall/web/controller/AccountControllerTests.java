package com.kiseok.pingmall.web.controller;

import com.kiseok.pingmall.common.domain.account.Account;
import com.kiseok.pingmall.common.domain.account.AccountRole;
import com.kiseok.pingmall.common.domain.verification.Verification;
import com.kiseok.pingmall.web.common.BaseControllerTests;
import com.kiseok.pingmall.web.dto.account.AccountDepositRequestDto;
import com.kiseok.pingmall.web.dto.account.AccountModifyRequestDto;
import com.kiseok.pingmall.web.dto.account.AccountRequestDto;
import com.kiseok.pingmall.web.dto.account.AccountResponseDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import static com.kiseok.pingmall.common.resources.RestDocsResource.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AccountControllerTests extends BaseControllerTests {

    @BeforeEach
    void setUp() {
        Verification verification = Verification.builder()
                .email(appProperties.getTestEmail())
                .verificationCode(UUID.randomUUID().toString().substring(0, 6))
                .isVerified(true)
                .build();

        Verification anotherVerification = Verification.builder()
                .email(ANOTHER + appProperties.getTestEmail())
                .verificationCode(UUID.randomUUID().toString().substring(0, 6))
                .isVerified(true)
                .build();

        verificationRepository.save(verification);
        verificationRepository.save(anotherVerification);
    }

    @AfterEach
    void tearDown()    {
        this.accountRepository.deleteAll();
        this.verificationRepository.deleteAll();
    }

    @DisplayName("유저 생성 시 유효성 검사 실패 -> 400 BAD_REQUEST")
    @ParameterizedTest(name = "{index} {displayName} message={0}")
    @MethodSource("validSaveAccount")
    void save_account_invalid_400(String email, String password, String name, String address, Long balance) throws Exception    {
        AccountRequestDto requestDto = AccountRequestDto.builder()
                .email(email)
                .password(password)
                .name(name)
                .address(address)
                .balance(balance)
                .build();

        this.mockMvc.perform(post(ACCOUNT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").exists())
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("code").exists())
                .andExpect(jsonPath("erroredAt").exists())
                .andExpect(jsonPath("errors.[*].field").exists())
                .andExpect(jsonPath("errors.[*].value").exists())
                .andExpect(jsonPath("errors.[*].reason").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.load-index").exists())
                .andExpect(jsonPath("_links.profile").exists())
        ;
    }

    @DisplayName("유저 생성 시 중복 -> 400 BAD_REQUEST")
    @Test
    void save_account_duplicate_400() throws Exception  {
        AccountRequestDto requestDto = createAccountRequestDto();

        this.mockMvc.perform(post(ACCOUNT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").value(appProperties.getTestEmail()))
                .andExpect(jsonPath("password").doesNotExist())
                .andExpect(jsonPath("name").value(appProperties.getTestName()))
                .andExpect(jsonPath("address").value(appProperties.getTestAddress()))
                .andExpect(jsonPath("balance").value(appProperties.getTestBalance()))
                .andExpect(jsonPath("accountRole").value(AccountRole.USER.name()))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.login-account").exists())
                .andExpect(jsonPath("_links.profile").exists())
        ;

        this.mockMvc.perform(post(ACCOUNT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").exists())
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("code").exists())
                .andExpect(jsonPath("erroredAt").exists())
                .andExpect(jsonPath("errors").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.load-index").exists())
                .andExpect(jsonPath("_links.profile").exists())
        ;
    }

    @DisplayName("정상적으로 유저 생성 -> 201 CREATED")
    @Test
    void save_account_201() throws Exception  {
        AccountRequestDto requestDto = createAccountRequestDto();

        this.mockMvc.perform(post("/api/accounts")
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").value(appProperties.getTestEmail()))
                .andExpect(jsonPath("password").doesNotExist())
                .andExpect(jsonPath("name").value(appProperties.getTestName()))
                .andExpect(jsonPath("address").value(appProperties.getTestAddress()))
                .andExpect(jsonPath("balance").value(appProperties.getTestBalance()))
                .andExpect(jsonPath("accountRole").value(AccountRole.USER.name()))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.login-account").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document(CREATE_ACCOUNT.getRel(),
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel(PROFILE.getRel()).description("link to profile"),
                                linkWithRel(LOGIN_ACCOUNT.getRel()).description("link to login account")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        requestFields(
                                fieldWithPath("email").description("E-Mail of new Account"),
                                fieldWithPath("password").description("Password of new Account"),
                                fieldWithPath("name").description("Name of Account"),
                                fieldWithPath("address").description("Address of new Account"),
                                fieldWithPath("balance").description("Balance of new Account")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.LOCATION).description("location header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        responseFields(
                                fieldWithPath("id").description("Identifier of new Account"),
                                fieldWithPath("email").description("E-mail of new Account"),
                                fieldWithPath("name").description("Name of new Account"),
                                fieldWithPath("address").description("Address of new Account"),
                                fieldWithPath("balance").description("Balance of new Account"),
                                fieldWithPath("accountRole").description("Role of new Account"),
                                fieldWithPath("_links.self.href").description("link to self"),
                                fieldWithPath("_links.profile.href").description("link to profile"),
                                fieldWithPath("_links.login-account.href").description("link to login account")
                        )
                ))
        ;

        List<Account> accountList = accountRepository.findAll();
        assertEquals(appProperties.getTestEmail(), accountList.get(0).getEmail());
        assertEquals(appProperties.getTestName(), accountList.get(0).getName());
        assertEquals(appProperties.getTestAddress(), accountList.get(0).getAddress());
        assertEquals(appProperties.getTestBalance(), accountList.get(0).getBalance());
        assertEquals(AccountRole.USER.name(), accountList.get(0).getAccountRole().name());
    }

    @DisplayName("디비에 없는 유저 불러오기 -> 404 NOT_FOUND")
    @Test
    void loadAccount_not_exist_404() throws Exception {
        AccountRequestDto requestDto = createAccountRequestDto();

        ResultActions actions = this.mockMvc.perform(post(ACCOUNT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").value(appProperties.getTestEmail()))
                .andExpect(jsonPath("password").doesNotExist())
                .andExpect(jsonPath("name").value(appProperties.getTestName()))
                .andExpect(jsonPath("address").value(appProperties.getTestAddress()))
                .andExpect(jsonPath("balance").value(appProperties.getTestBalance()))
                .andExpect(jsonPath("accountRole").value(AccountRole.USER.name()))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.login-account").exists())
                .andExpect(jsonPath("_links.profile").exists())
        ;

        String token = generateToken(actions);

        this.mockMvc.perform(get(ACCOUNT_URL + "-1")
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, token))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("status").exists())
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("code").exists())
                .andExpect(jsonPath("erroredAt").exists())
                .andExpect(jsonPath("errors").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.load-index").exists())
                .andExpect(jsonPath("_links.profile").exists())
        ;
    }

    @DisplayName("정상적으로 유저 불러오기 -> 200 OK")
    @Test
    void load_account_200() throws Exception {
        AccountRequestDto requestDto = createAccountRequestDto();

        ResultActions actions = this.mockMvc.perform(post(ACCOUNT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").value(appProperties.getTestEmail()))
                .andExpect(jsonPath("password").doesNotExist())
                .andExpect(jsonPath("name").value(appProperties.getTestName()))
                .andExpect(jsonPath("address").value(appProperties.getTestAddress()))
                .andExpect(jsonPath("balance").value(appProperties.getTestBalance()))
                .andExpect(jsonPath("accountRole").value(AccountRole.USER.name()))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.login-account").exists())
                .andExpect(jsonPath("_links.profile").exists())
        ;

        String contentAsString = actions.andReturn().getResponse().getContentAsString();
        AccountResponseDto responseDto = objectMapper.readValue(contentAsString, AccountResponseDto.class);
        String token = generateToken(actions);

        this.mockMvc.perform(get(ACCOUNT_URL + responseDto.getId())
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").value(responseDto.getEmail()))
                .andExpect(jsonPath("password").doesNotExist())
                .andExpect(jsonPath("name").value(responseDto.getName()))
                .andExpect(jsonPath("address").value(responseDto.getAddress()))
                .andExpect(jsonPath("balance").value(responseDto.getBalance()))
                .andExpect(jsonPath("accountRole").value(responseDto.getAccountRole().name()))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.create-account").exists())
                .andExpect(jsonPath("_links.modify-account").exists())
                .andExpect(jsonPath("_links.delete-account").exists())
                .andExpect(jsonPath("_links.deposit-account").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document(LOAD_ACCOUNT.getRel(),
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel(PROFILE.getRel()).description("link to profile"),
                                linkWithRel(CREATE_ACCOUNT.getRel()).description("link to create account"),
                                linkWithRel(DEPOSIT_ACCOUNT.getRel()).description("link to deposit account"),
                                linkWithRel(MODIFY_ACCOUNT.getRel()).description("link to modify account"),
                                linkWithRel(DELETE_ACCOUNT.getRel()).description("link to delete account")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        responseFields(
                                fieldWithPath("id").description("Identifier of Account"),
                                fieldWithPath("email").description("E-mail of Account"),
                                fieldWithPath("name").description("Name of Account"),
                                fieldWithPath("address").description("Address of Account"),
                                fieldWithPath("balance").description("Balance of Account"),
                                fieldWithPath("accountRole").description("Role of Account"),
                                fieldWithPath("_links.self.href").description("link to self"),
                                fieldWithPath("_links.profile.href").description("link to profile"),
                                fieldWithPath("_links.create-account.href").description("link to create account"),
                                fieldWithPath("_links.deposit-account.href").description("link to deposit account"),
                                fieldWithPath("_links.modify-account.href").description("link to modify account"),
                                fieldWithPath("_links.delete-account.href").description("link to delete account")
                        )
                ))
        ;
    }

    @DisplayName("유저 잔액 추가 유효성 검사 -> 400 BAD_REQUEST")
    @ParameterizedTest(name = "{index} {displayName} message={0}")
    @ValueSource(longs = {0L, 10000000L})
    void deposit_account_invalid_400(Long balance) throws Exception {
        AccountRequestDto requestDto = createAccountRequestDto();

        ResultActions actions = this.mockMvc.perform(post(ACCOUNT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").value(appProperties.getTestEmail()))
                .andExpect(jsonPath("password").doesNotExist())
                .andExpect(jsonPath("name").value(appProperties.getTestName()))
                .andExpect(jsonPath("address").value(appProperties.getTestAddress()))
                .andExpect(jsonPath("balance").value(appProperties.getTestBalance()))
                .andExpect(jsonPath("accountRole").value(AccountRole.USER.name()))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.login-account").exists())
                .andExpect(jsonPath("_links.profile").exists())
        ;

        String jwt = generateToken(actions);
        String contentAsString = actions.andReturn().getResponse().getContentAsString();
        AccountResponseDto responseDto = objectMapper.readValue(contentAsString, AccountResponseDto.class);
        AccountDepositRequestDto depositRequestDto = AccountDepositRequestDto.builder()
                .balance(balance)
                .build();

        this.mockMvc.perform(put(ACCOUNT_URL + responseDto.getId() + "/balance")
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(depositRequestDto))
                .header(HttpHeaders.AUTHORIZATION, jwt))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").exists())
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("code").exists())
                .andExpect(jsonPath("erroredAt").exists())
                .andExpect(jsonPath("errors.[*].field").exists())
                .andExpect(jsonPath("errors.[*].value").exists())
                .andExpect(jsonPath("errors.[*].reason").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.load-index").exists())
                .andExpect(jsonPath("_links.profile").exists())
        ;
    }

    @DisplayName("유저 잔액 추가 시 유저 null -> 400 BAD_REQUEST")
    @Test
    void deposit_account_user_null_404() throws Exception   {
        AccountRequestDto requestDto = createAccountRequestDto();

        ResultActions actions = this.mockMvc.perform(post(ACCOUNT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").value(appProperties.getTestEmail()))
                .andExpect(jsonPath("password").doesNotExist())
                .andExpect(jsonPath("name").value(appProperties.getTestName()))
                .andExpect(jsonPath("address").value(appProperties.getTestAddress()))
                .andExpect(jsonPath("balance").value(appProperties.getTestBalance()))
                .andExpect(jsonPath("accountRole").value(AccountRole.USER.name()))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.login-account").exists())
                .andExpect(jsonPath("_links.profile").exists())
        ;

        String jwt = generateToken(actions);
        AccountDepositRequestDto depositRequestDto = AccountDepositRequestDto.builder()
                .balance(100L)
                .build();

        this.mockMvc.perform(put(ACCOUNT_URL + "-1/balance")
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(depositRequestDto))
                .header(HttpHeaders.AUTHORIZATION, jwt))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("status").exists())
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("code").exists())
                .andExpect(jsonPath("erroredAt").exists())
                .andExpect(jsonPath("errors").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.load-index").exists())
                .andExpect(jsonPath("_links.profile").exists())
        ;
    }

    @DisplayName("예금할 유저 ID와 시도한 유저 ID가 다를 경우 -> 400 BAD_REQUEST")
    @Test
    void deposit_account_id_not_match_400() throws Exception    {
        AccountRequestDto requestDto = createAccountRequestDto();

        ResultActions actions = this.mockMvc.perform(post(ACCOUNT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").value(appProperties.getTestEmail()))
                .andExpect(jsonPath("password").doesNotExist())
                .andExpect(jsonPath("name").value(appProperties.getTestName()))
                .andExpect(jsonPath("address").value(appProperties.getTestAddress()))
                .andExpect(jsonPath("balance").value(appProperties.getTestBalance()))
                .andExpect(jsonPath("accountRole").value(AccountRole.USER.name()))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.login-account").exists())
                .andExpect(jsonPath("_links.profile").exists())
        ;

        String contentAsString = actions.andReturn().getResponse().getContentAsString();
        AccountResponseDto responseDto = objectMapper.readValue(contentAsString, AccountResponseDto.class);
        AccountDepositRequestDto depositRequestDto = AccountDepositRequestDto.builder()
                .balance(10000L)
                .build();

        AccountRequestDto requestDto2 = createAnotherAccountRequestDto();

        ResultActions actions2 = this.mockMvc.perform(post(ACCOUNT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto2)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").value(ANOTHER + appProperties.getTestEmail()))
                .andExpect(jsonPath("password").doesNotExist())
                .andExpect(jsonPath("name").value(ANOTHER + appProperties.getTestName()))
                .andExpect(jsonPath("address").value(ANOTHER + appProperties.getTestAddress()))
                .andExpect(jsonPath("balance").value(appProperties.getTestBalance()))
                .andExpect(jsonPath("accountRole").value(AccountRole.USER.name()))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.login-account").exists())
                .andExpect(jsonPath("_links.profile").exists())
        ;
        String jwt = generateToken(actions2);

        this.mockMvc.perform(put(ACCOUNT_URL + responseDto.getId() + "/balance")
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(depositRequestDto))
                .header(HttpHeaders.AUTHORIZATION, jwt))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").exists())
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("code").exists())
                .andExpect(jsonPath("erroredAt").exists())
                .andExpect(jsonPath("errors").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.load-index").exists())
                .andExpect(jsonPath("_links.profile").exists())
        ;
    }

    @DisplayName("인증 없이 예금 시 -> 401 UNAUTHORIZED")
    @Test
    void deposit_account_unauthorized_401() throws Exception   {
        AccountRequestDto requestDto = createAccountRequestDto();

        ResultActions actions = this.mockMvc.perform(post(ACCOUNT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").value(appProperties.getTestEmail()))
                .andExpect(jsonPath("password").doesNotExist())
                .andExpect(jsonPath("name").value(appProperties.getTestName()))
                .andExpect(jsonPath("address").value(appProperties.getTestAddress()))
                .andExpect(jsonPath("balance").value(appProperties.getTestBalance()))
                .andExpect(jsonPath("accountRole").value(AccountRole.USER.name()))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.login-account").exists())
                .andExpect(jsonPath("_links.profile").exists())
        ;

        String contentAsString = actions.andReturn().getResponse().getContentAsString();
        AccountResponseDto responseDto = objectMapper.readValue(contentAsString, AccountResponseDto.class);

        AccountDepositRequestDto depositRequestDto = AccountDepositRequestDto.builder()
                .balance(100L)
                .build();

        this.mockMvc.perform(put(ACCOUNT_URL + responseDto.getId() + "/balance")
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(depositRequestDto)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
        ;
    }

    @DisplayName("정상적으로 유저 잔액 추가 -> 200 OK")
    @Test
    void deposit_account_200() throws Exception {
        AccountRequestDto requestDto = createAccountRequestDto();

        ResultActions actions = this.mockMvc.perform(post(ACCOUNT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").value(appProperties.getTestEmail()))
                .andExpect(jsonPath("password").doesNotExist())
                .andExpect(jsonPath("name").value(appProperties.getTestName()))
                .andExpect(jsonPath("address").value(appProperties.getTestAddress()))
                .andExpect(jsonPath("balance").value(appProperties.getTestBalance()))
                .andExpect(jsonPath("accountRole").value(AccountRole.USER.name()))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.login-account").exists())
                .andExpect(jsonPath("_links.profile").exists())
        ;

        String jwt = generateToken(actions);
        String contentAsString = actions.andReturn().getResponse().getContentAsString();
        AccountResponseDto responseDto = objectMapper.readValue(contentAsString, AccountResponseDto.class);
        AccountDepositRequestDto depositRequestDto = AccountDepositRequestDto.builder()
                .balance(100L)
                .build();

        this.mockMvc.perform(put(ACCOUNT_URL + responseDto.getId() + "/balance")
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(depositRequestDto))
                .header(HttpHeaders.AUTHORIZATION, jwt))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").value(appProperties.getTestEmail()))
                .andExpect(jsonPath("password").doesNotExist())
                .andExpect(jsonPath("name").value(appProperties.getTestName()))
                .andExpect(jsonPath("address").value(appProperties.getTestAddress()))
                .andExpect(jsonPath("balance").value(10000099L))
                .andExpect(jsonPath("accountRole").value(AccountRole.USER.name()))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.load-account").exists())
                .andExpect(jsonPath("_links.modify-account").exists())
                .andExpect(jsonPath("_links.delete-account").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document(DEPOSIT_ACCOUNT.getRel(),
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel(PROFILE.getRel()).description("link to profile"),
                                linkWithRel(LOAD_ACCOUNT.getRel()).description("link to load account"),
                                linkWithRel(MODIFY_ACCOUNT.getRel()).description("link to modify account"),
                                linkWithRel(DELETE_ACCOUNT.getRel()).description("link to delete account")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header"),
                                headerWithName(HttpHeaders.AUTHORIZATION).description("authorization header")
                        ),
                        requestFields(
                                fieldWithPath("balance").description("Additional Balance of Account")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        responseFields(
                                fieldWithPath("id").description("Identifier of Account"),
                                fieldWithPath("email").description("E-mail of Account"),
                                fieldWithPath("name").description("Name of Account"),
                                fieldWithPath("address").description("Address of Account"),
                                fieldWithPath("balance").description("Balance of Account"),
                                fieldWithPath("accountRole").description("Role of Account"),
                                fieldWithPath("_links.self.href").description("link to self"),
                                fieldWithPath("_links.profile.href").description("link to profile"),
                                fieldWithPath("_links.load-account.href").description("link to load account"),
                                fieldWithPath("_links.modify-account.href").description("link to modify account"),
                                fieldWithPath("_links.delete-account.href").description("link to delete account")
                        )
                ))
        ;
    }



    @DisplayName("유저 수정 유효성 검사 -> 400 BAD_REQUEST")
    @ParameterizedTest(name = "{index} {displayName} message={0}")
    @MethodSource("validModifyAccount")
    void modify_account_invalid_400(String password, String name, String address) throws Exception  {
        AccountRequestDto requestDto = createAccountRequestDto();

        ResultActions actions = this.mockMvc.perform(post(ACCOUNT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").value(appProperties.getTestEmail()))
                .andExpect(jsonPath("password").doesNotExist())
                .andExpect(jsonPath("name").value(appProperties.getTestName()))
                .andExpect(jsonPath("address").value(appProperties.getTestAddress()))
                .andExpect(jsonPath("balance").value(appProperties.getTestBalance()))
                .andExpect(jsonPath("accountRole").value(AccountRole.USER.name()))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.login-account").exists())
                .andExpect(jsonPath("_links.profile").exists())
        ;

        String contentAsString = actions.andReturn().getResponse().getContentAsString();
        AccountResponseDto responseDto = objectMapper.readValue(contentAsString, AccountResponseDto.class);
        String token = generateToken(actions);

        AccountModifyRequestDto modifyRequestDto = AccountModifyRequestDto.builder()
                .password(password)
                .name(name)
                .address(address)
                .build();

        this.mockMvc.perform(put(ACCOUNT_URL + responseDto.getId())
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(modifyRequestDto))
                .header(HttpHeaders.AUTHORIZATION, token))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").exists())
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("code").exists())
                .andExpect(jsonPath("erroredAt").exists())
                .andExpect(jsonPath("errors.[*].field").exists())
                .andExpect(jsonPath("errors.[*].value").exists())
                .andExpect(jsonPath("errors.[*].reason").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.load-index").exists())
                .andExpect(jsonPath("_links.profile").exists())
        ;
    }

    @DisplayName("디비에 없는 유저 수정 -> 404 NOT_FOUND")
    @Test
    void modify_account_not_exist_404() throws Exception    {
        AccountRequestDto requestDto = createAccountRequestDto();

        ResultActions actions = this.mockMvc.perform(post(ACCOUNT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").value(appProperties.getTestEmail()))
                .andExpect(jsonPath("password").doesNotExist())
                .andExpect(jsonPath("name").value(appProperties.getTestName()))
                .andExpect(jsonPath("address").value(appProperties.getTestAddress()))
                .andExpect(jsonPath("balance").value(appProperties.getTestBalance()))
                .andExpect(jsonPath("accountRole").value(AccountRole.USER.name()))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.login-account").exists())
                .andExpect(jsonPath("_links.profile").exists())
        ;

        String token = generateToken(actions);
        AccountModifyRequestDto modifyRequestDto = createAccountModifyRequestDto();

        this.mockMvc.perform(put(ACCOUNT_URL + "-1")
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(modifyRequestDto))
                .header(HttpHeaders.AUTHORIZATION, token))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("status").exists())
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("code").exists())
                .andExpect(jsonPath("erroredAt").exists())
                .andExpect(jsonPath("errors").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.load-index").exists())
                .andExpect(jsonPath("_links.profile").exists())
        ;
    }

    @DisplayName("수정할 유저 ID와 시도한 유저 ID가 다를 경우 -> 400 BAD_REQUEST")
    @Test
    void modify_account_id_not_match_400() throws Exception {
        AccountRequestDto requestDto = createAccountRequestDto();

        ResultActions actions = this.mockMvc.perform(post(ACCOUNT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").value(appProperties.getTestEmail()))
                .andExpect(jsonPath("password").doesNotExist())
                .andExpect(jsonPath("name").value(appProperties.getTestName()))
                .andExpect(jsonPath("address").value(appProperties.getTestAddress()))
                .andExpect(jsonPath("balance").value(appProperties.getTestBalance()))
                .andExpect(jsonPath("accountRole").value(AccountRole.USER.name()))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.login-account").exists())
                .andExpect(jsonPath("_links.profile").exists())
        ;

        String contentAsString = actions.andReturn().getResponse().getContentAsString();
        AccountResponseDto responseDto = objectMapper.readValue(contentAsString, AccountResponseDto.class);
        AccountModifyRequestDto modifyRequestDto = createAccountModifyRequestDto();
        AccountRequestDto requestDto2 = createAnotherAccountRequestDto();

        ResultActions actions2 = this.mockMvc.perform(post(ACCOUNT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto2)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").value(ANOTHER + appProperties.getTestEmail()))
                .andExpect(jsonPath("password").doesNotExist())
                .andExpect(jsonPath("name").value(ANOTHER + appProperties.getTestName()))
                .andExpect(jsonPath("address").value(ANOTHER + appProperties.getTestAddress()))
                .andExpect(jsonPath("balance").value(appProperties.getTestBalance()))
                .andExpect(jsonPath("accountRole").value(AccountRole.USER.name()))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.login-account").exists())
                .andExpect(jsonPath("_links.profile").exists())
        ;

        String token = generateToken(actions2);

        this.mockMvc.perform(put(ACCOUNT_URL + responseDto.getId())
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(modifyRequestDto))
                .header(HttpHeaders.AUTHORIZATION, token))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").exists())
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("code").exists())
                .andExpect(jsonPath("erroredAt").exists())
                .andExpect(jsonPath("errors").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.load-index").exists())
                .andExpect(jsonPath("_links.profile").exists())
        ;
    }

    @DisplayName("정상적으로 유저 수정 -> 200 OK")
    @Test
    void modify_account_200() throws Exception  {
        AccountRequestDto requestDto = createAccountRequestDto();

        ResultActions actions = this.mockMvc.perform(post(ACCOUNT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").value(appProperties.getTestEmail()))
                .andExpect(jsonPath("password").doesNotExist())
                .andExpect(jsonPath("name").value(appProperties.getTestName()))
                .andExpect(jsonPath("address").value(appProperties.getTestAddress()))
                .andExpect(jsonPath("balance").value(appProperties.getTestBalance()))
                .andExpect(jsonPath("accountRole").value(AccountRole.USER.name()))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.login-account").exists())
                .andExpect(jsonPath("_links.profile").exists())
        ;

        String contentAsString = actions.andReturn().getResponse().getContentAsString();
        AccountResponseDto responseDto = objectMapper.readValue(contentAsString, AccountResponseDto.class);
        String token = generateToken(actions);
        AccountModifyRequestDto modifyRequestDto = createAccountModifyRequestDto();

        this.mockMvc.perform(put(ACCOUNT_URL + responseDto.getId())
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(modifyRequestDto))
                .header(HttpHeaders.AUTHORIZATION, token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").value(responseDto.getEmail()))
                .andExpect(jsonPath("password").doesNotExist())
                .andExpect(jsonPath("name").value(modifyRequestDto.getName()))
                .andExpect(jsonPath("address").value(modifyRequestDto.getAddress()))
                .andExpect(jsonPath("balance").value(responseDto.getBalance()))
                .andExpect(jsonPath("accountRole").value(responseDto.getAccountRole().name()))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.load-account").exists())
                .andExpect(jsonPath("_links.deposit-account").exists())
                .andExpect(jsonPath("_links.delete-account").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document(MODIFY_ACCOUNT.getRel(),
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel(PROFILE.getRel()).description("link to profile"),
                                linkWithRel(LOAD_ACCOUNT.getRel()).description("link to load account"),
                                linkWithRel(DEPOSIT_ACCOUNT.getRel()).description("link to deposit account"),
                                linkWithRel(DELETE_ACCOUNT.getRel()).description("link to delete account")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header"),
                                headerWithName(HttpHeaders.AUTHORIZATION).description("authorization header")
                        ),
                        requestFields(
                                fieldWithPath("password").description("Modified Password of Account"),
                                fieldWithPath("name").description("Modified Name of Account"),
                                fieldWithPath("address").description("Modified Address of Account")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        responseFields(
                                fieldWithPath("id").description("Identifier of Modified Account"),
                                fieldWithPath("email").description("E-mail of Modified Account"),
                                fieldWithPath("name").description("Name of Modified Account"),
                                fieldWithPath("address").description("Address of Modified Account"),
                                fieldWithPath("balance").description("Balance of Modified Account"),
                                fieldWithPath("accountRole").description("Role of Modified Account"),
                                fieldWithPath("_links.self.href").description("link to self"),
                                fieldWithPath("_links.profile.href").description("link to profile"),
                                fieldWithPath("_links.load-account.href").description("link to load account"),
                                fieldWithPath("_links.deposit-account.href").description("link to deposit account"),
                                fieldWithPath("_links.delete-account.href").description("link to delete account")
                        )
                ))
        ;

        List<Account> accountList = accountRepository.findAll();
        assertEquals(requestDto.getEmail(), accountList.get(0).getEmail());
        assertEquals(modifyRequestDto.getName(), accountList.get(0).getName());
        assertEquals(modifyRequestDto.getAddress(), accountList.get(0).getAddress());
    }

    @DisplayName("디비에 없는 유저 삭제 -> 404 NOT_FOUND")
    @Test
    void remove_account_not_exist_404() throws Exception    {
        AccountRequestDto requestDto = createAccountRequestDto();

        ResultActions actions = this.mockMvc.perform(post(ACCOUNT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").value(appProperties.getTestEmail()))
                .andExpect(jsonPath("password").doesNotExist())
                .andExpect(jsonPath("name").value(appProperties.getTestName()))
                .andExpect(jsonPath("address").value(appProperties.getTestAddress()))
                .andExpect(jsonPath("balance").value(appProperties.getTestBalance()))
                .andExpect(jsonPath("accountRole").value(AccountRole.USER.name()))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.login-account").exists())
                .andExpect(jsonPath("_links.profile").exists())
        ;

        String token = generateToken(actions);

        this.mockMvc.perform(delete(ACCOUNT_URL + "-1")
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, token))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("status").exists())
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("code").exists())
                .andExpect(jsonPath("erroredAt").exists())
                .andExpect(jsonPath("errors").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.load-index").exists())
                .andExpect(jsonPath("_links.profile").exists())
        ;
    }

    @DisplayName("삭제할 유저 ID와 시도한 유저ID가 다를 경우 -> 400 BAD_REQUEST")
    @Test
    void delete_account_id_not_match_400() throws Exception {
        AccountRequestDto requestDto = createAccountRequestDto();

        ResultActions actions = this.mockMvc.perform(post(ACCOUNT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").value(appProperties.getTestEmail()))
                .andExpect(jsonPath("password").doesNotExist())
                .andExpect(jsonPath("name").value(appProperties.getTestName()))
                .andExpect(jsonPath("address").value(appProperties.getTestAddress()))
                .andExpect(jsonPath("balance").value(appProperties.getTestBalance()))
                .andExpect(jsonPath("accountRole").value(AccountRole.USER.name()))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.login-account").exists())
                .andExpect(jsonPath("_links.profile").exists())
        ;

        String contentAsString = actions.andReturn().getResponse().getContentAsString();
        AccountResponseDto responseDto = objectMapper.readValue(contentAsString, AccountResponseDto.class);
        AccountRequestDto requestDto2 = createAnotherAccountRequestDto();

        ResultActions actions2 = this.mockMvc.perform(post(ACCOUNT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto2)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").value(ANOTHER + appProperties.getTestEmail()))
                .andExpect(jsonPath("password").doesNotExist())
                .andExpect(jsonPath("name").value(ANOTHER + appProperties.getTestName()))
                .andExpect(jsonPath("address").value(ANOTHER + appProperties.getTestAddress()))
                .andExpect(jsonPath("balance").value(appProperties.getTestBalance()))
                .andExpect(jsonPath("accountRole").value(AccountRole.USER.name()))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.login-account").exists())
                .andExpect(jsonPath("_links.profile").exists())
        ;

        String token = generateToken(actions2);

        this.mockMvc.perform(delete(ACCOUNT_URL + responseDto.getId())
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, token))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").exists())
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("code").exists())
                .andExpect(jsonPath("erroredAt").exists())
                .andExpect(jsonPath("errors").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.load-index").exists())
                .andExpect(jsonPath("_links.profile").exists())
        ;
    }

    @DisplayName("정상적으로 유저 삭제")
    @Test
    void remove_account_200() throws Exception  {
        AccountRequestDto requestDto = createAccountRequestDto();

        ResultActions actions = this.mockMvc.perform(post(ACCOUNT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").value(appProperties.getTestEmail()))
                .andExpect(jsonPath("password").doesNotExist())
                .andExpect(jsonPath("name").value(appProperties.getTestName()))
                .andExpect(jsonPath("address").value(appProperties.getTestAddress()))
                .andExpect(jsonPath("balance").value(appProperties.getTestBalance()))
                .andExpect(jsonPath("accountRole").value(AccountRole.USER.name()))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.login-account").exists())
                .andExpect(jsonPath("_links.profile").exists())
        ;

        String contentAsString = actions.andReturn().getResponse().getContentAsString();
        AccountResponseDto responseDto = objectMapper.readValue(contentAsString, AccountResponseDto.class);
        String token = generateToken(actions);

        this.mockMvc.perform(delete(ACCOUNT_URL + responseDto.getId())
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").value(appProperties.getTestEmail()))
                .andExpect(jsonPath("password").doesNotExist())
                .andExpect(jsonPath("name").value(appProperties.getTestName()))
                .andExpect(jsonPath("address").value(appProperties.getTestAddress()))
                .andExpect(jsonPath("balance").value(appProperties.getTestBalance()))
                .andExpect(jsonPath("accountRole").value(AccountRole.USER.name()))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.login-account").exists())
                .andExpect(jsonPath("_links.create-account").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document(DELETE_ACCOUNT.getRel(),
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel(PROFILE.getRel()).description("link to profile"),
                                linkWithRel(LOGIN_ACCOUNT.getRel()).description("link to login account"),
                                linkWithRel(CREATE_ACCOUNT.getRel()).description("link to create account")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header"),
                                headerWithName(HttpHeaders.AUTHORIZATION).description("authorization header")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        responseFields(
                                fieldWithPath("id").description("Identifier of Deleted Account"),
                                fieldWithPath("email").description("E-mail of Deleted Account"),
                                fieldWithPath("name").description("Name of Deleted Account"),
                                fieldWithPath("address").description("Address of Deleted Account"),
                                fieldWithPath("balance").description("Balance of Deleted Account"),
                                fieldWithPath("accountRole").description("Role of Deleted Account"),
                                fieldWithPath("_links.self.href").description("link to self"),
                                fieldWithPath("_links.profile.href").description("link to profile"),
                                fieldWithPath("_links.login-account.href").description("link to login account"),
                                fieldWithPath("_links.create-account.href").description("link to create account")
                        )
                ))
        ;
    }

    private static Stream<Arguments> validSaveAccount()   {
        return Stream.of(
                Arguments.of("", "testPassword", "testName", "testAddress", 200L, true),
                Arguments.of(" ", "testPassword", "testName", "testAddress", 200L,true),
                Arguments.of("test@email.com", "", "testName", "testAddress", 200L, true),
                Arguments.of("test@email.com", " ", "testName", "testAddress", 200L, true),
                Arguments.of("test@email.com", "testPassword", "", "testAddress", 200L, true),
                Arguments.of("test@email.com", "testPassword", " ", "testAddress", 200L, true),
                Arguments.of("test@email.com", "testPassword", "testName", "", 200L, true),
                Arguments.of("test@email.com", "testPassword", "testNAme", " ", 200L, true),
                Arguments.of("test@email.com", "testPassword", "testName", "", null, true)
        );
    }

    private static Stream<Arguments> validModifyAccount()   {
        return Stream.of(
                Arguments.of("", "modifiedTestName", "modifiedTestAddress", true),
                Arguments.of(" ", "modifiedTestName", "modifiedTestAddress", true),
                Arguments.of("modifiedTestPassword", "", "modifiedTestAddress", true),
                Arguments.of("modifiedTestPassword", " ", "modifiedTestAddress", true),
                Arguments.of("modifiedTestPassword", "modifiedTestName", "", true),
                Arguments.of("modifiedTestPassword", "modifiedTestName", " ", true)
        );
    }
}