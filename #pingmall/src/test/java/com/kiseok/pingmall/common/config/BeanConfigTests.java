package com.kiseok.pingmall.common.config;

import com.kiseok.pingmall.common.BaseCommonTests;
import com.kiseok.pingmall.common.domain.account.Account;
import com.kiseok.pingmall.web.dto.account.AccountRequestDto;
import com.kiseok.pingmall.web.dto.account.AccountResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BeanConfigTests extends BaseCommonTests {

    @DisplayName("ModelMapper 테스트")
    @Test
    void test_model_mapper() {
        AccountRequestDto requestDto = AccountRequestDto.builder()
                .email(appProperties.getTestEmail())
                .password(appProperties.getTestPassword())
                .name(appProperties.getTestName())
                .address(appProperties.getTestAddress())
                .balance(appProperties.getTestBalance())
                .build();

        AccountResponseDto responseDto = modelMapper.map(requestDto, AccountResponseDto.class);

        assertEquals(responseDto.getEmail(), requestDto.getEmail());
        assertEquals(responseDto.getName(), requestDto.getName());
        assertEquals(responseDto.getAddress(), requestDto.getAddress());
        assertEquals(responseDto.getBalance(), requestDto.getBalance());
    }

    @DisplayName("PasswordEncoder 테스트")
    @Test
    void test_password_encoder()    {
        String password = appProperties.getTestPassword();
        String encodedPassword = passwordEncoder.encode(password);

        then(passwordEncoder.matches(password, encodedPassword)).isTrue();
    }

}
