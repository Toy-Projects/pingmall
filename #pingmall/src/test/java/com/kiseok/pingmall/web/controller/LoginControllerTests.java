package com.kiseok.pingmall.web.controller;

import com.kiseok.pingmall.common.domain.account.AccountRole;
import com.kiseok.pingmall.common.domain.verification.Verification;
import com.kiseok.pingmall.web.common.BaseControllerTests;
import com.kiseok.pingmall.web.dto.LoginRequestDto;
import com.kiseok.pingmall.web.dto.account.AccountRequestDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.util.UUID;
import java.util.stream.Stream;
import static com.kiseok.pingmall.common.resources.RestDocsResource.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LoginControllerTests extends BaseControllerTests {

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

    @DisplayName("로그인 시 유효성 검사 실패 -> 400 BAD_REQUEST")
    @ParameterizedTest(name = "{index} {displayName} message={0}")
    @MethodSource("validLoginAccount")
    void login_account_invalid_400(String email, String password) throws Exception   {
        LoginRequestDto requestDto = LoginRequestDto.builder()
                .email(email)
                .password(password)
                .build();

        this.mockMvc.perform(post(LOGIN_URL)
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

    @DisplayName("로그인 실패 -> 401 UNAUTHORIZED")
    @ParameterizedTest(name = "{index} {displayName} message={0}")
    @MethodSource("failedLoginAccount")
    void login_account_failed_401(String email, String password) throws Exception   {
        LoginRequestDto requestDto = LoginRequestDto.builder()
                .email(email)
                .password(password)
                .build();

        this.mockMvc.perform(post(LOGIN_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("status").exists())
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("code").exists())
                .andExpect(jsonPath("errors").exists())
                .andExpect(jsonPath("erroredAt").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.load-index").exists())
                .andExpect(jsonPath("_links.profile").exists())
        ;
    }

    @DisplayName("정상적으로 로그인 -> 200 OK")
    @Test
    void login_account() throws Exception   {
        LoginRequestDto requestDto = createLoginRequestDto();

        this.mockMvc.perform(post(LOGIN_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("token").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.create-product").exists())
                .andExpect(jsonPath("_links.load-all-products").exists())
                .andExpect(jsonPath("_links.find-email").exists())
                .andExpect(jsonPath("_links.find-password").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document(LOGIN_ACCOUNT.getRel(),
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel(PROFILE.getRel()).description("link to profile"),
                                linkWithRel(CREATE_PRODUCT.getRel()).description("link to create product"),
                                linkWithRel(FIND_EMAIL.getRel()).description("link to find email"),
                                linkWithRel(FIND_PASSWORD.getRel()).description("link to find password"),
                                linkWithRel(LOAD_ALL_PRODUCT.getRel()).description("link to load all products")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        requestFields(
                                fieldWithPath("email").description("E-mail of Account to Login"),
                                fieldWithPath("password").description("Password of Account to Login")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        responseFields(
                                fieldWithPath("token").description("Token of Login Account"),
                                fieldWithPath("_links.self.href").description("link to self"),
                                fieldWithPath("_links.profile.href").description("link to profile"),
                                fieldWithPath("_links.create-product.href").description("link to create product"),
                                fieldWithPath("_links.find-email.href").description("link to find email"),
                                fieldWithPath("_links.find-password.href").description("link to find password"),
                                fieldWithPath("_links.load-all-products.href").description("link to load all products")
                        )
                ))
        ;
    }

    private static Stream<Arguments> validLoginAccount() {
        return Stream.of(
                Arguments.of("", "testPW123!", true),
                Arguments.of(" ", "testPW123!", true),
                Arguments.of("test@email.com", "", true),
                Arguments.of("test@email.com", " ", true)
        );
    }

    private static Stream<Arguments> failedLoginAccount() {
        return Stream.of(
                Arguments.of("kiseok@email.com", "testPW123!", true),
                Arguments.of("test@email.com", "kiseokPW123!", true)
        );
    }

}
