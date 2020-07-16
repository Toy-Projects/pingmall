package com.kiseok.pingmall.api.service;

import com.kiseok.pingmall.api.exception.account.UserNotFoundException;
import com.kiseok.pingmall.common.domain.account.Account;
import com.kiseok.pingmall.common.domain.account.AccountRepository;
import com.kiseok.pingmall.web.dto.find.FindEmailResponseDto;
import com.kiseok.pingmall.web.dto.find.FindPasswordRequestDto;
import com.kiseok.pingmall.web.dto.find.FindPasswordResponseDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class FindService {

    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final AccountRepository accountRepository;

    public FindEmailResponseDto findEmail(String name)  {
        Account account = accountRepository.findByName(name).orElseThrow(UserNotFoundException::new);

        return modelMapper.map(account, FindEmailResponseDto.class);
    }

    public FindPasswordResponseDto findPassword(FindPasswordRequestDto requestDto) {
        Account account = accountRepository.findByEmailAndName(requestDto.getEmail(), requestDto.getName()).orElseThrow(UserNotFoundException::new);
        account.updatePassword(createTemporaryPassword());

        return modelMapper.map(accountRepository.save(account), FindPasswordResponseDto.class);
    }

    private String createTemporaryPassword() {
        return passwordEncoder.encode(UUID.randomUUID().toString());
    }
}
