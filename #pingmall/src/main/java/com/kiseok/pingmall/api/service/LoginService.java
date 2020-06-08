package com.kiseok.pingmall.api.service;

import com.kiseok.pingmall.common.config.jwt.JwtProvider;
import com.kiseok.pingmall.common.domain.account.Account;
import com.kiseok.pingmall.common.domain.account.AccountRepository;
import com.kiseok.pingmall.web.dto.jwt.JwtResponseDto;
import com.kiseok.pingmall.web.dto.LoginRequestDto;
import com.kiseok.pingmall.web.dto.jwt.JwtRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class LoginService {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final AccountRepository accountRepository;

    public ResponseEntity<?> loginAccount(LoginRequestDto loginRequestDto) {
        if(!authenticate(loginRequestDto.getEmail(), loginRequestDto.getPassword()))    {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Optional<Account> optionalAccount = accountRepository.findByEmail(loginRequestDto.getEmail());
        if(!optionalAccount.isPresent())  {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        String jwt = jwtProvider.generateToken(new JwtRequestDto(
                optionalAccount.get().getId(), loginRequestDto.getEmail()));

        return new ResponseEntity<>(new JwtResponseDto(jwt), HttpStatus.OK);
    }

    private boolean authenticate(String email, String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
            return true;
        } catch (DisabledException | BadCredentialsException e) {
            return false;
        }
    }
}
