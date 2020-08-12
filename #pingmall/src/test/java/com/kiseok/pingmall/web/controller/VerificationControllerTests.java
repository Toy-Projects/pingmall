package com.kiseok.pingmall.web.controller;

import com.kiseok.pingmall.common.domain.verification.Verification;
import com.kiseok.pingmall.web.common.BaseControllerTests;
import com.kiseok.pingmall.web.dto.account.AccountRequestDto;
import com.kiseok.pingmall.web.dto.verification.VerificationCodeRequestDto;
import com.kiseok.pingmall.web.dto.verification.VerificationEmailRequestDto;
import com.kiseok.pingmall.web.dto.verification.VerificationResponseDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
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
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class VerificationControllerTests extends BaseControllerTests {

    @AfterEach
    void tear_down()    {
        this.accountRepository.deleteAll();
        this.verificationRepository.deleteAll();
    }

    @DisplayName("이메일 유효성 검사 실패 -> 400 BAD_REQUEST")
    @ParameterizedTest(name = "{index} {displayName} message={0}")
    @ValueSource(strings = {"", " ", "test.com", "test", "test@", ".com"})
    void valid_email_invalid_400(String email) throws Exception {
        VerificationEmailRequestDto verificationEmailRequestDto = VerificationEmailRequestDto.builder()
                .email(email)
                .build();

        this.mockMvc.perform(post(VERIFICATION_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verificationEmailRequestDto)))
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

    @DisplayName("이메일 인증 시 중복된 유저 -> 400 BAD_REQUEST")
    @Test
    void valid_email_duplicated_user_400() throws Exception {
        AccountRequestDto accountRequestDto = createAccountRequestDto();
        Verification verification = Verification.builder()
                .email(accountRequestDto.getEmail())
                .verificationCode(UUID.randomUUID().toString().substring(0, 6))
                .isVerified(true)
                .build();
        verificationRepository.save(verification);

        this.mockMvc.perform(post(ACCOUNT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(accountRequestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
        ;

        VerificationEmailRequestDto verificationEmailRequestDto = createVerificationEmailRequestDto();
        verificationEmailRequestDto.setEmail(appProperties.getTestEmail());

        this.mockMvc.perform(post(VERIFICATION_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verificationEmailRequestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").exists())
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("code").exists())
                .andExpect(jsonPath("erroredAt").exists())
                .andExpect(jsonPath("errors").exists())
        ;
    }

    @DisplayName("정상적으로 인증번호 전송 완료 => 201 CREATED")
    @Test
    void valid_email_201() throws Exception {
        VerificationEmailRequestDto requestDto = createVerificationEmailRequestDto();

        this.mockMvc.perform(post(VERIFICATION_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").value(appProperties.getMyEmail()))
                .andExpect(jsonPath("verificationCode").exists())
                .andExpect(jsonPath("isVerified").value(false))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.login-account").exists())
                .andExpect(jsonPath("_links.load-all-products").exists())
                .andDo(document(VERIFY_EMAIL.getRel(),
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel(PROFILE.getRel()).description("link to profile"),
                                linkWithRel(LOGIN_ACCOUNT.getRel()).description("link to login account"),
                                linkWithRel(LOAD_ALL_PRODUCT.getRel()).description("link to load all products")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        requestFields(
                                fieldWithPath("email").description("New Email to verify")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.LOCATION).description("location header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        responseFields(
                                fieldWithPath("id").description("Identifier of New Verification"),
                                fieldWithPath("email").description("Email of New Verification"),
                                fieldWithPath("verificationCode").description("Verification Code of New Verification"),
                                fieldWithPath("isVerified").description("Results of current email is verified"),
                                fieldWithPath("_links.self.href").description("link to self"),
                                fieldWithPath("_links.profile.href").description("link to profile"),
                                fieldWithPath("_links.login-account.href").description("link to login account"),
                                fieldWithPath("_links.load-all-products.href").description("link to load all products")
                        )
                ))
        ;
    }

    @DisplayName("두번 연속 인증번호 요청시 기존의 인증 엔티티 정상적으로 삭제")
    @Test
    void verify_email_twice() throws Exception  {
        VerificationEmailRequestDto requestDto = createVerificationEmailRequestDto();

        this.mockMvc.perform(post(VERIFICATION_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").value(appProperties.getMyEmail()))
                .andExpect(jsonPath("verificationCode").exists())
                .andExpect(jsonPath("isVerified").value(false))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.login-account").exists())
                .andExpect(jsonPath("_links.load-all-products").exists())
        ;

        ResultActions actions = this.mockMvc.perform(post(VERIFICATION_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").value(appProperties.getMyEmail()))
                .andExpect(jsonPath("verificationCode").exists())
                .andExpect(jsonPath("isVerified").value(false))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.login-account").exists())
                .andExpect(jsonPath("_links.load-all-products").exists())
                ;

        String contentAsString = actions.andReturn().getResponse().getContentAsString();
        VerificationResponseDto responseDto = objectMapper.readValue(contentAsString, VerificationResponseDto.class);

        List<Verification> verificationList = verificationRepository.findAll();
        Verification verification = verificationRepository.findByEmail(requestDto.getEmail()).get();

        assertThat(responseDto.getVerificationCode()).isEqualTo(verification.getVerificationCode());
        assertThat(verificationList.size()).isEqualTo(1);
    }

    @DisplayName("인증번호 유효성 검사 실패 -> 400 BAD_REQUEST")
    @ParameterizedTest(name = "{index} {displayName} message={0}")
    @MethodSource("validVerifyCode")
    void valid_code_invalid_400(String email, String verificationCode) throws Exception {
        VerificationEmailRequestDto verificationEmailRequestDto = createVerificationEmailRequestDto();

        this.mockMvc.perform(post(VERIFICATION_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verificationEmailRequestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").value(appProperties.getMyEmail()))
                .andExpect(jsonPath("verificationCode").exists())
                .andExpect(jsonPath("isVerified").value(false))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.login-account").exists())
                .andExpect(jsonPath("_links.load-all-products").exists())
        ;

        VerificationCodeRequestDto verificationCodeRequestDto = VerificationCodeRequestDto.builder()
                .email(email)
                .verificationCode(verificationCode)
                .build();

        this.mockMvc.perform(put(VERIFICATION_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verificationCodeRequestDto)))
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

    @DisplayName("이메일 인증을 요청하지 않은 채 인증 시 -> 404 NOT_FOUND")
    @Test
    void valid_code_without_email_verification_400() throws Exception   {
        VerificationCodeRequestDto verificationCodeRequestDto = createVerificationCodeRequestDto();

        this.mockMvc.perform(put(VERIFICATION_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verificationCodeRequestDto)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("status").exists())
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("code").exists())
                .andExpect(jsonPath("erroredAt").exists())
                .andExpect(jsonPath("errors").exists())
        ;
    }

    @DisplayName("인증번호가 다를 시 -> 400 BAD_REQUEST")
    @Test
    void valid_code_not_match_verification_code_400() throws Exception  {
        VerificationEmailRequestDto verificationEmailRequestDto = createVerificationEmailRequestDto();

        this.mockMvc.perform(post(VERIFICATION_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verificationEmailRequestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").value(appProperties.getMyEmail()))
                .andExpect(jsonPath("verificationCode").exists())
                .andExpect(jsonPath("isVerified").value(false))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.login-account").exists())
                .andExpect(jsonPath("_links.load-all-products").exists())
        ;

        VerificationCodeRequestDto verificationCodeRequestDto = createVerificationCodeRequestDto();

        this.mockMvc.perform(put(VERIFICATION_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verificationCodeRequestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").exists())
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("code").exists())
                .andExpect(jsonPath("erroredAt").exists())
                .andExpect(jsonPath("errors").exists())
        ;
    }

    @DisplayName("정상적으로 이메일 인증 완료 -> 200 OK")
    @Test
    void valid_code_200() throws Exception {
        VerificationEmailRequestDto verificationEmailRequestDto = createVerificationEmailRequestDto();

        ResultActions actions = this.mockMvc.perform(post(VERIFICATION_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verificationEmailRequestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").value(appProperties.getMyEmail()))
                .andExpect(jsonPath("verificationCode").exists())
                .andExpect(jsonPath("isVerified").value(false))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.login-account").exists())
                .andExpect(jsonPath("_links.load-all-products").exists())
                ;

        String contentAsString = actions.andReturn().getResponse().getContentAsString();
        VerificationResponseDto responseDto = objectMapper.readValue(contentAsString, VerificationResponseDto.class);

        VerificationCodeRequestDto verificationCodeRequestDto = createVerificationCodeRequestDto();
        verificationCodeRequestDto.setVerificationCode(responseDto.getVerificationCode());

        this.mockMvc.perform(put(VERIFICATION_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verificationCodeRequestDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(VERIFY_CODE.getRel(),
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel(PROFILE.getRel()).description("link to profile"),
                                linkWithRel(LOGIN_ACCOUNT.getRel()).description("link to login account"),
                                linkWithRel(LOAD_ALL_PRODUCT.getRel()).description("link to load all products")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        requestFields(
                                fieldWithPath("email").description("New Email to verify"),
                                fieldWithPath("verificationCode").description("Received New Verification Code")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        responseFields(
                                fieldWithPath("id").description("Identifier of New Verification"),
                                fieldWithPath("email").description("Email of New Verification"),
                                fieldWithPath("verificationCode").description("Verification Code of New Verification"),
                                fieldWithPath("isVerified").description("Results of current email is verified"),
                                fieldWithPath("_links.self.href").description("link to self"),
                                fieldWithPath("_links.profile.href").description("link to profile"),
                                fieldWithPath("_links.login-account.href").description("link to login account"),
                                fieldWithPath("_links.load-all-products.href").description("link to load all products")
                        )
                ))
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
