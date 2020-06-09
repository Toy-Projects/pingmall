package com.kiseok.pingmall.web.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kiseok.pingmall.common.AppProperties;
import com.kiseok.pingmall.common.config.jwt.JwtProvider;
import com.kiseok.pingmall.common.domain.account.AccountRepository;
import com.kiseok.pingmall.web.dto.LoginRequestDto;
import com.kiseok.pingmall.web.dto.account.AccountModifyRequestDto;
import com.kiseok.pingmall.web.dto.account.AccountRequestDto;
import com.kiseok.pingmall.web.dto.account.AccountResponseDto;
import com.kiseok.pingmall.web.dto.jwt.JwtRequestDto;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import static com.kiseok.pingmall.common.config.jwt.JwtConstants.PREFIX;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class BaseControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected AccountRepository accountRepository;

    @Autowired
    protected JwtProvider jwtProvider;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected ModelMapper modelMapper;

    @Autowired
    protected AppProperties appProperties;

    protected final String ACCOUNT_URL = "/api/accounts/";
    protected final String LOGIN_URL = "/api/login";

    protected String generateToken(ResultActions actions) throws Exception {
        String contentAsString = actions.andReturn().getResponse().getContentAsString();
        AccountResponseDto responseDto = objectMapper.readValue(contentAsString, AccountResponseDto.class);
        JwtRequestDto jwtRequestDto = modelMapper.map(responseDto, JwtRequestDto.class);

        return PREFIX + jwtProvider.generateToken(jwtRequestDto);
    }

    protected AccountRequestDto createAccountRequestDto()   {
        System.out.println(appProperties.getTestAddress());
        return AccountRequestDto.builder()
                .email(appProperties.getTestEmail())
                .password(appProperties.getTestPassword())
                .name(appProperties.getTestName())
                .address(appProperties.getTestAddress())
                .build();
    }

    protected AccountModifyRequestDto createAccountModifyRequestDto()   {
        return AccountModifyRequestDto.builder()
                .password(appProperties.getTestModifiedPassword())
                .name(appProperties.getTestModifiedName())
                .address(appProperties.getTestModifiedAddress())
                .build();
    }

    protected LoginRequestDto createLoginRequestDto()   {
        return LoginRequestDto.builder()
                .email(appProperties.getTestEmail())
                .password(appProperties.getTestPassword())
                .build();
    }

}
