package com.kiseok.pingmall.api.service;

import com.kiseok.pingmall.api.exception.account.UserDuplicatedException;
import com.kiseok.pingmall.api.exception.account.UserIdNotMatchException;
import com.kiseok.pingmall.api.exception.account.UserNotFoundException;
import com.kiseok.pingmall.common.domain.account.Account;
import com.kiseok.pingmall.common.domain.account.AccountAdapter;
import com.kiseok.pingmall.common.domain.account.AccountRepository;
import com.kiseok.pingmall.web.dto.account.AccountDepositRequestDto;
import com.kiseok.pingmall.web.dto.account.AccountModifyRequestDto;
import com.kiseok.pingmall.web.dto.account.AccountRequestDto;
import com.kiseok.pingmall.web.dto.account.AccountResponseDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
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

    public AccountResponseDto loadAccount(Long accountId) {
        Account account = isUserExist(accountId);

        return modelMapper.map(account, AccountResponseDto.class);
    }

    public AccountResponseDto saveAccount(AccountRequestDto requestDto) {
        isUserDuplicated(requestDto);
        requestDto.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        Account savedAccount = accountRepository.save(requestDto.toEntity());

        return modelMapper.map(savedAccount, AccountResponseDto.class);
    }

    public AccountResponseDto depositAccount(Long accountId, AccountDepositRequestDto requestDto, Account currentUser) {
        Account account = isUserExist(accountId);
        isUserIdMatch(currentUser, account);
        account.addBalance(requestDto);

        return modelMapper.map(accountRepository.save(account), AccountResponseDto.class);
    }

    public AccountResponseDto modifyAccount(Long accountId, AccountModifyRequestDto requestDto, Account currentUser) {
        Account account = isUserExist(accountId);
        isUserIdMatch(currentUser, account);
        requestDto.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        account.updateAccount(requestDto);

        return modelMapper.map(accountRepository.save(account), AccountResponseDto.class);
    }

    public AccountResponseDto removeAccount(Long accountId, Account currentUser) {
        Account account = isUserExist(accountId);
        isUserIdMatch(currentUser, account);
        accountRepository.delete(account);

        return modelMapper.map(account, AccountResponseDto.class);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(email));

        return new AccountAdapter(account);
    }

    private Account isUserExist(Long accountId) {
        return accountRepository.findById(accountId).orElseThrow(UserNotFoundException::new);
    }

    private void isUserIdMatch(Account currentUser, Account account) {
        if (!account.getId().equals(currentUser.getId())) {
            throw new UserIdNotMatchException();
        }
    }

    private void isUserDuplicated(AccountRequestDto requestDto) {
        Optional<Account> optionalAccount = accountRepository.findByEmail(requestDto.getEmail());
        if(optionalAccount.isPresent()) {
            throw new UserDuplicatedException();
        }
    }
}
