package com.kiseok.pingmall.api.service;

import com.kiseok.pingmall.api.exception.account.UserNotFoundException;
import com.kiseok.pingmall.common.domain.account.Account;
import com.kiseok.pingmall.common.domain.account.AccountRepository;
import com.kiseok.pingmall.web.dto.find.FindEmailResponseDto;
import com.kiseok.pingmall.web.dto.find.FindPasswordRequestDto;
import com.kiseok.pingmall.web.dto.find.FindPasswordResponseDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class FindService {

    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final AccountRepository accountRepository;

    public ResponseEntity<?> findEmail(String name)  {
        Account account = accountRepository.findByName(name).orElseThrow(UserNotFoundException::new);
        FindEmailResponseDto responseDto = modelMapper.map(account, FindEmailResponseDto.class);

        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    public ResponseEntity<?> findPassword(FindPasswordRequestDto requestDto) {
        Account account = accountRepository.findByEmailAndName(requestDto.getEmail(), requestDto.getName()).orElseThrow(UserNotFoundException::new);
        account.updatePassword(createTemporaryPassword());
        FindPasswordResponseDto responseDto = modelMapper.map(accountRepository.save(account), FindPasswordResponseDto.class);

        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    private String createTemporaryPassword() {
        return passwordEncoder.encode(UUID.randomUUID().toString());
    }
}
