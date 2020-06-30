package com.kiseok.pingmall.web.controller;

import com.kiseok.pingmall.common.domain.account.AccountRole;
import com.kiseok.pingmall.web.BaseControllerTests;
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
import org.springframework.http.MediaType;
import java.util.stream.Stream;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LoginControllerTests extends BaseControllerTests {

    @BeforeEach
    void setUp() throws Exception {
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
        ;
    }

    @AfterEach
    void tearDown()    {
        this.accountRepository.deleteAll();
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
