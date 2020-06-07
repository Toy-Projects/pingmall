package com.kiseok.pingmall.api.service;

import com.kiseok.pingmall.common.domain.account.Account;
import com.kiseok.pingmall.common.domain.account.AccountRepository;
import com.kiseok.pingmall.web.dto.account.AccountModifyRequestDto;
import com.kiseok.pingmall.web.dto.account.AccountRequestDto;
import com.kiseok.pingmall.web.dto.account.AccountResponseDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AccountService {

    private final ModelMapper modelMapper;
    private final AccountRepository accountRepository;

    public ResponseEntity<?> loadAccount(Long id) {
        Optional<Account> optionalAccount = accountRepository.findById(id);
        if(!optionalAccount.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Account account = optionalAccount.get();
        AccountResponseDto responseDto = modelMapper.map(account, AccountResponseDto.class);

        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    public ResponseEntity<?> saveAccount(AccountRequestDto requestDto) {
        Optional<Account> optionalAccount = accountRepository.findByEmail(requestDto.getEmail());
        if(optionalAccount.isPresent()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Account savedAccount = accountRepository.save(requestDto.toEntity());
        AccountResponseDto responseDto = modelMapper.map(savedAccount, AccountResponseDto.class);

        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    public ResponseEntity<?> modifyAccount(Long id, AccountModifyRequestDto requestDto) {
        Optional<Account> optionalAccount = accountRepository.findById(id);
        if(!optionalAccount.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Account account = optionalAccount.get();
        modelMapper.map(requestDto, account);
        AccountResponseDto responseDto = modelMapper.map(accountRepository.save(account), AccountResponseDto.class);

        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    public ResponseEntity<?> removeAccount(Long id) {
        Optional<Account> optionalAccount = accountRepository.findById(id);
        if(!optionalAccount.isPresent())    {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        accountRepository.delete(optionalAccount.get());

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
