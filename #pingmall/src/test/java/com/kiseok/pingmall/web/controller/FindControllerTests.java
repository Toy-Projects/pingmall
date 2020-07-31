package com.kiseok.pingmall.web.controller;

import com.kiseok.pingmall.common.domain.account.Account;
import com.kiseok.pingmall.common.domain.account.AccountRole;
import com.kiseok.pingmall.common.domain.verification.Verification;
import com.kiseok.pingmall.web.common.BaseControllerTests;
import com.kiseok.pingmall.web.dto.account.AccountRequestDto;
import com.kiseok.pingmall.web.dto.find.FindPasswordRequestDto;
import com.kiseok.pingmall.web.dto.find.FindPasswordResponseDto;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import java.util.UUID;
import java.util.stream.Stream;
import static com.kiseok.pingmall.common.resources.RestDocsResource.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FindControllerTests extends BaseControllerTests {

    @BeforeEach
    void setUp() throws Exception {
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
    }

    @AfterEach
    void tearDown()    {
        this.accountRepository.deleteAll();
        this.verificationRepository.deleteAll();
    }

    @DisplayName("Email 찾기 유효성 검사 실패 -> 404 NOT_FOUND")
    @ParameterizedTest(name = "{index} {displayName} message={0}")
    @ValueSource(strings = {"", " ", "yks"})
    void find_email_invalid_404(String name) throws Exception  {
        this.mockMvc.perform(get(FIND_EMAIL_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .param("name", name))
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

    @DisplayName("정상적으로 Email 찾기 -> 200 OK")
    @Test
    void find_email_200() throws Exception  {
        this.mockMvc.perform(get(FIND_EMAIL_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .param("name", createAccountRequestDto().getName()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("email").value(appProperties.getTestEmail()))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.find-password").exists())
                .andExpect(jsonPath("_links.login-account").exists())
                .andDo(document(FIND_EMAIL.getRel(),
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel(PROFILE.getRel()).description("link to profile"),
                                linkWithRel(FIND_PASSWORD.getRel()).description("link to find password"),
                                linkWithRel(LOGIN_ACCOUNT.getRel()).description("link to login account")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        responseFields(
                                fieldWithPath("email").description("E-Mail found by Account's Name"),
                                fieldWithPath("_links.self.href").description("link to self"),
                                fieldWithPath("_links.profile.href").description("link to profile"),
                                fieldWithPath("_links.find-password.href").description("link to find password"),
                                fieldWithPath("_links.login-account.href").description("link to login account")
                        )
                ))
        ;
    }

    @DisplayName("Password찾기 유효성 검사 실패 -> 400 BAD_REQUEST")
    @ParameterizedTest(name = "{index} {displayName} message={0}")
    @MethodSource("validFindPassword")
    void find_password_invalid_400(String email, String name) throws Exception  {
        FindPasswordRequestDto requestDto = createFindPasswordRequestDto(email, name);

        this.mockMvc.perform(put(FIND_PASSWORD_URL)
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

    @DisplayName("Password 찾을 때 DB에 없는 유저 찾을 시 -> 404 NOT_FOUND")
    @ParameterizedTest(name = "{index} {displayName} message={0}")
    @MethodSource("noneExistFindPassword")
    void find_password_not_exist_404(String email, String name) throws Exception  {
        FindPasswordRequestDto requestDto = createFindPasswordRequestDto(email, name);

        this.mockMvc.perform(put(FIND_PASSWORD_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
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

    @DisplayName("정상적으로 Password 찾기 -> 200 OK")
    @Test
    void find_password_200() throws Exception   {
        String email = appProperties.getMyEmail();
        Verification verification = Verification.builder()
                .email(email)
                .verificationCode(UUID.randomUUID().toString().substring(0, 6))
                .isVerified(true)
                .build();

        verificationRepository.save(verification);

        AccountRequestDto accountRequestDto = createAccountRequestDto();
        accountRequestDto.setEmail(email);

        this.mockMvc.perform(post(ACCOUNT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(accountRequestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").value(email))
                .andExpect(jsonPath("password").doesNotExist())
                .andExpect(jsonPath("name").value(appProperties.getTestName()))
                .andExpect(jsonPath("address").value(appProperties.getTestAddress()))
                .andExpect(jsonPath("balance").value(appProperties.getTestBalance()))
                .andExpect(jsonPath("accountRole").value(AccountRole.USER.name()))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.login-account").exists())
                .andExpect(jsonPath("_links.profile").exists())
        ;

        FindPasswordRequestDto findPasswordRequestDto =
                createFindPasswordRequestDto(email, createAccountRequestDto().getName());

        ResultActions actions = this.mockMvc.perform(put(FIND_PASSWORD_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(findPasswordRequestDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("password").exists())
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.find-email").exists())
                .andExpect(jsonPath("_links.login-account").exists())
                .andDo(document(FIND_PASSWORD.getRel(),
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel(PROFILE.getRel()).description("link to profile"),
                                linkWithRel(FIND_EMAIL.getRel()).description("link to find email"),
                                linkWithRel(LOGIN_ACCOUNT.getRel()).description("link to login account")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        requestFields(
                                fieldWithPath("email").description("E-mail of Account to Find Password"),
                                fieldWithPath("name").description("Name of Account to Find Password")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        responseFields(
                                fieldWithPath("password").description("Temporary Password"),
                                fieldWithPath("message").description("Message for Temporary password's usage"),
                                fieldWithPath("_links.self.href").description("link to self"),
                                fieldWithPath("_links.profile.href").description("link to profile"),
                                fieldWithPath("_links.find-email.href").description("link to find email"),
                                fieldWithPath("_links.login-account.href").description("link to login account")
                        )
                ))
        ;
        String contentAsString = actions.andReturn().getResponse().getContentAsString();
        FindPasswordResponseDto responseDto = objectMapper.readValue(contentAsString, FindPasswordResponseDto.class);
        Account account = accountRepository.findByEmail(email).get();

        assertEquals(account.getPassword(), responseDto.getPassword());
    }

    private static Stream<Arguments> validFindPassword() {
        return Stream.of(
                Arguments.of("", "testName", true),
                Arguments.of(" ", "testName", true),
                Arguments.of("@email.com", "testName", true),
                Arguments.of("test@email.", "testName", true),
                Arguments.of("test@.com", "testName", true),
                Arguments.of("test@email.com", "", true),
                Arguments.of("test@email.com", " ", true)
        );
    }

    private static Stream<Arguments> noneExistFindPassword() {
        return Stream.of(
                Arguments.of("kiseok@email.com", "testName", true),
                Arguments.of("test@email.com", "kiseok", true)
        );
    }

}
