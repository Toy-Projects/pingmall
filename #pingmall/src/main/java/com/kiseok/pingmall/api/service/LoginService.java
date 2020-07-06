package com.kiseok.pingmall.api.service;

import com.kiseok.pingmall.api.exception.account.UserNotFoundException;
import com.kiseok.pingmall.api.exception.account.UserUnauthorizedException;
import com.kiseok.pingmall.common.config.jwt.JwtProvider;
import com.kiseok.pingmall.common.domain.account.Account;
import com.kiseok.pingmall.common.domain.account.AccountRepository;
import com.kiseok.pingmall.web.dto.jwt.JwtResponseDto;
import com.kiseok.pingmall.web.dto.LoginRequestDto;
import com.kiseok.pingmall.web.dto.jwt.JwtRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class LoginService {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final AccountRepository accountRepository;

    public JwtResponseDto loginAccount(LoginRequestDto loginRequestDto) {
        if(!authenticate(loginRequestDto.getEmail(), loginRequestDto.getPassword()))    {
            throw new UserUnauthorizedException();
        }

        Account account = isUserExist(loginRequestDto);
        String jwt = jwtProvider.generateToken(new JwtRequestDto(account.getId(), loginRequestDto.getEmail()));

        return new JwtResponseDto(jwt);
    }

    private boolean authenticate(String email, String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
            return true;
        } catch (DisabledException | BadCredentialsException e) {
            return false;
        }
    }

    private Account isUserExist(LoginRequestDto loginRequestDto) {
        return accountRepository.findByEmail(loginRequestDto.getEmail()).orElseThrow(UserNotFoundException::new);
    }
}
