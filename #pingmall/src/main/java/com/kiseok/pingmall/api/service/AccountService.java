package com.kiseok.pingmall.api.service;

import com.kiseok.pingmall.common.domain.account.Account;
import com.kiseok.pingmall.common.domain.account.AccountAdapter;
import com.kiseok.pingmall.common.domain.account.AccountRepository;
import com.kiseok.pingmall.web.dto.account.AccountDepositRequestDto;
import com.kiseok.pingmall.web.dto.account.AccountModifyRequestDto;
import com.kiseok.pingmall.web.dto.account.AccountRequestDto;
import com.kiseok.pingmall.web.dto.account.AccountResponseDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AccountService implements UserDetailsService {

    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final AccountRepository accountRepository;

    public ResponseEntity<?> loadAccount(Long accountId) {
        Optional<Account> optionalAccount = accountRepository.findById(accountId);
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
        requestDto.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        Account savedAccount = accountRepository.save(requestDto.toEntity());
        AccountResponseDto responseDto = modelMapper.map(savedAccount, AccountResponseDto.class);

        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    public ResponseEntity<?> depositAccount(Long id, AccountDepositRequestDto requestDto) {
        Optional<Account> optionalAccount = accountRepository.findById(id);
        if(!optionalAccount.isPresent())    {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Account account = optionalAccount.get();
        account.addBalance(requestDto);
        AccountResponseDto responseDto = modelMapper.map(accountRepository.save(account), AccountResponseDto.class);

        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    public ResponseEntity<?> modifyAccount(Long accountId, AccountModifyRequestDto requestDto) {
        Optional<Account> optionalAccount = accountRepository.findById(accountId);
        if(!optionalAccount.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Account account = optionalAccount.get();
        modelMapper.map(requestDto, account);
        AccountResponseDto responseDto = modelMapper.map(accountRepository.save(account), AccountResponseDto.class);

        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    public ResponseEntity<?> removeAccount(Long accountId) {
        Optional<Account> optionalAccount = accountRepository.findById(accountId);
        if(!optionalAccount.isPresent())    {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        accountRepository.delete(optionalAccount.get());

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(email).orElseThrow(() ->
                new UsernameNotFoundException(email));

        return new AccountAdapter(account);
    }
}
