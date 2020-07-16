package com.kiseok.pingmall.web.controller;

import com.kiseok.pingmall.common.domain.account.Account;
import com.kiseok.pingmall.common.domain.account.AccountRole;
import com.kiseok.pingmall.web.common.BaseControllerTests;
import com.kiseok.pingmall.web.dto.account.AccountRequestDto;
import com.kiseok.pingmall.web.dto.find.FindPasswordRequestDto;
import com.kiseok.pingmall.web.dto.find.FindPasswordResponseDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FindControllerTests extends BaseControllerTests {

    @AfterEach
    void tearDown()    {
        accountRepository.deleteAll();
    }

    @DisplayName("Email 찾기 유효성 검사 실패 -> 404 NOT_FOUND")
    @ParameterizedTest(name = "{index} {displayName} message={0}")
    @ValueSource(strings = {"", " ", "yks"})
    void find_email_invalid_404(String name) throws Exception  {
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

        this.mockMvc.perform(get(FIND_EMAIL_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .param("name", requestDto.getName()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("email").value(appProperties.getTestEmail()))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.find-password").exists())
                .andExpect(jsonPath("_links.login-account").exists())
        ;
    }

    @DisplayName("Password찾기 유효성 검사 실패 -> 400 BAD_REQUEST")
    @ParameterizedTest(name = "{index} {displayName} message={0}")
    @MethodSource("validFindPassword")
    void find_password_invalid_400(String email, String name) throws Exception  {
        AccountRequestDto accountRequestDto = createAccountRequestDto();
        this.mockMvc.perform(post(ACCOUNT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(accountRequestDto)))
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

        FindPasswordRequestDto findPasswordRequestDto = createFindPasswordRequestDto(email, name);

        this.mockMvc.perform(put(FIND_PASSWORD_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(findPasswordRequestDto)))
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
        AccountRequestDto accountRequestDto = createAccountRequestDto();
        this.mockMvc.perform(post(ACCOUNT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(accountRequestDto)))
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

        FindPasswordRequestDto findPasswordRequestDto = createFindPasswordRequestDto(email, name);

        this.mockMvc.perform(put(FIND_PASSWORD_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(findPasswordRequestDto)))
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
        AccountRequestDto accountRequestDto = createAccountRequestDto();
        this.mockMvc.perform(post(ACCOUNT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(accountRequestDto)))
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

        FindPasswordRequestDto findPasswordRequestDto =
                createFindPasswordRequestDto(accountRequestDto.getEmail(), accountRequestDto.getName());

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
        ;

        String contentAsString = actions.andReturn().getResponse().getContentAsString();
        FindPasswordResponseDto responseDto = objectMapper.readValue(contentAsString, FindPasswordResponseDto.class);
        Account account = accountRepository.findByEmail(accountRequestDto.getEmail()).get();

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
