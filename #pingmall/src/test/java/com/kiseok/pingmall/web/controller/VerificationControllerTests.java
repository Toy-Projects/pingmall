package com.kiseok.pingmall.web.controller;

import com.kiseok.pingmall.common.domain.verification.Verification;
import com.kiseok.pingmall.web.common.BaseControllerTests;
import com.kiseok.pingmall.web.dto.account.AccountRequestDto;
import com.kiseok.pingmall.web.dto.verification.VerificationCodeRequestDto;
import com.kiseok.pingmall.web.dto.verification.VerificationEmailRequestDto;
import com.kiseok.pingmall.web.dto.verification.VerificationResponseDto;
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
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class VerificationControllerTests extends BaseControllerTests {

    @AfterEach
    void tear_down()    {
        accountRepository.deleteAll();
        verificationRepository.deleteAll();
    }

    @DisplayName("이메일 유효성 검사 실패 -> 400 BAD_REQUEST")
    @ParameterizedTest(name = "{index} {displayName} message={0}")
    @ValueSource(strings = {"", " ", "test.com", "test", "test@", ".com"})
    void valid_email_invalid_400(String email) throws Exception {
        VerificationEmailRequestDto verificationEmailRequestDto = VerificationEmailRequestDto.builder()
                .email(email)
                .build();

        this.mockMvc.perform(post("/api/verifications")
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verificationEmailRequestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @DisplayName("이메일 인증 시 중복된 유저 -> 400 BAD_REQUEST")
    @Test
    void valid_email_duplicated_user_400() throws Exception {
        AccountRequestDto accountRequestDto = createAccountRequestDto();
        this.mockMvc.perform(post(ACCOUNT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(accountRequestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
        ;

        VerificationEmailRequestDto verificationEmailRequestDto = VerificationEmailRequestDto.builder()
                .email(accountRequestDto.getEmail())
                .build();

        this.mockMvc.perform(post("/api/verifications")
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verificationEmailRequestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @DisplayName("정상적으로 인증번호 전송 완료 => 201 CREATED")
    @Test
    void valid_email_201() throws Exception {
        VerificationEmailRequestDto requestDto = VerificationEmailRequestDto.builder()
                .email("rltjr219@gmail.com")
                .build();

        this.mockMvc.perform(post("/api/verifications")
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").value(requestDto.getEmail()))
                .andExpect(jsonPath("verificationCode").exists())
                .andExpect(jsonPath("isVerified").value(false))
        ;
    }

    @DisplayName("두번 연속 인증번호 요청시 기존의 인증 엔티티 정상적으로 삭제")
    @Test
    void verify_email_twice() throws Exception  {
        VerificationEmailRequestDto requestDto = VerificationEmailRequestDto.builder()
                .email("rltjr219@gmail.com")
                .build();

        this.mockMvc.perform(post("/api/verifications")
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").value(requestDto.getEmail()))
                .andExpect(jsonPath("verificationCode").exists())
                .andExpect(jsonPath("isVerified").value(false))
        ;

        ResultActions actions = this.mockMvc.perform(post("/api/verifications")
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").value(requestDto.getEmail()))
                .andExpect(jsonPath("verificationCode").exists())
                .andExpect(jsonPath("isVerified").value(false));

        String contentAsString = actions.andReturn().getResponse().getContentAsString();
        VerificationResponseDto responseDto = objectMapper.readValue(contentAsString, VerificationResponseDto.class);

        List<Verification> verificationList = verificationRepository.findAll();
        Verification verification = verificationRepository.findByEmail(requestDto.getEmail()).get();

        assertThat(responseDto.getVerificationCode()).endsWith(verification.getVerificationCode());
        assertThat(verificationList.size()).isEqualTo(1);
    }

    @DisplayName("인증번호 유효성 검사 실패 -> 400 BAD_REQUEST")
    @ParameterizedTest(name = "{index} {displayName} message={0}")
    @MethodSource("validVerifyCode")
    void valid_code_invalid_400(String email, String verificationCode) throws Exception {
        VerificationEmailRequestDto verificationEmailRequestDto = VerificationEmailRequestDto.builder()
                .email("rltjr219@gmail.com")
                .build();

        this.mockMvc.perform(post("/api/verifications")
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verificationEmailRequestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").value(verificationEmailRequestDto.getEmail()))
                .andExpect(jsonPath("verificationCode").exists())
                .andExpect(jsonPath("isVerified").value(false));

        VerificationCodeRequestDto verificationCodeRequestDto = VerificationCodeRequestDto.builder()
                .email(email)
                .verificationCode(verificationCode)
                .build();

        this.mockMvc.perform(put("/api/verifications")
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verificationCodeRequestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @DisplayName("이메일 인증을 요청하지 않은 채 인증 시 -> 404 NOT_FOUND")
    @Test
    void valid_code_without_email_verification_400() throws Exception   {
        VerificationCodeRequestDto verificationCodeRequestDto = VerificationCodeRequestDto.builder()
                .email("test@email.com")
                .verificationCode(UUID.randomUUID().toString().substring(0, 6))
                .build();

        this.mockMvc.perform(put("/api/verifications")
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verificationCodeRequestDto)))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @DisplayName("인증번호가 다를 시 -> 400 BAD_REQUEST")
    @Test
    void valid_code_not_match_verification_code_400() throws Exception  {
        VerificationEmailRequestDto verificationEmailRequestDto = VerificationEmailRequestDto.builder()
                .email("rltjr219@gmail.com")
                .build();

        ResultActions actions = this.mockMvc.perform(post("/api/verifications")
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verificationEmailRequestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").value(verificationEmailRequestDto.getEmail()))
                .andExpect(jsonPath("verificationCode").exists())
                .andExpect(jsonPath("isVerified").value(false));

        String contentAsString = actions.andReturn().getResponse().getContentAsString();
        VerificationResponseDto responseDto = objectMapper.readValue(contentAsString, VerificationResponseDto.class);

        VerificationCodeRequestDto verificationCodeRequestDto = VerificationCodeRequestDto.builder()
                .email("rltjr219@gmail.com")
                .verificationCode(UUID.randomUUID().toString().substring(0, 6))
                .build();

        this.mockMvc.perform(put("/api/verifications")
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verificationCodeRequestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @DisplayName("정상적으로 이메일 인증 완료 -> 200 OK")
    @Test
    void valid_code_200() throws Exception {
        VerificationEmailRequestDto verificationEmailRequestDto = VerificationEmailRequestDto.builder()
                .email("rltjr219@gmail.com")
                .build();

        ResultActions actions = this.mockMvc.perform(post("/api/verifications")
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verificationEmailRequestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").value(verificationEmailRequestDto.getEmail()))
                .andExpect(jsonPath("verificationCode").exists())
                .andExpect(jsonPath("isVerified").value(false));

        String contentAsString = actions.andReturn().getResponse().getContentAsString();
        VerificationResponseDto responseDto = objectMapper.readValue(contentAsString, VerificationResponseDto.class);

        VerificationCodeRequestDto verificationCodeRequestDto = VerificationCodeRequestDto.builder()
                .email("rltjr219@gmail.com")
                .verificationCode(responseDto.getVerificationCode())
                .build();

        this.mockMvc.perform(put("/api/verifications")
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verificationCodeRequestDto)))
                .andDo(print())
                .andExpect(status().isOk())
        ;
    }

    private static Stream<Arguments> validVerifyCode() {
        return Stream.of(
                Arguments.of("", UUID.randomUUID().toString().substring(0, 6)),
                Arguments.of(" ", UUID.randomUUID().toString().substring(0, 6)),
                Arguments.of("test.com", UUID.randomUUID().toString().substring(0, 6)),
                Arguments.of("test", UUID.randomUUID().toString().substring(0, 6)),
                Arguments.of("test@", UUID.randomUUID().toString().substring(0, 6)),
                Arguments.of(".com", UUID.randomUUID().toString().substring(0, 6)),
                Arguments.of("test@email.com", ""),
                Arguments.of("test@email.com", " ")
        );
    }
}
