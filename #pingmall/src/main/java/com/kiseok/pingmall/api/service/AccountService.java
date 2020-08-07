package com.kiseok.pingmall.api.service;

import com.kiseok.pingmall.api.exception.account.UserDuplicatedException;
import com.kiseok.pingmall.api.exception.account.UserIdNotMatchException;
import com.kiseok.pingmall.api.exception.account.UserNotFoundException;
import com.kiseok.pingmall.api.exception.account.UserNotVerifiedException;
import com.kiseok.pingmall.common.domain.account.Account;
import com.kiseok.pingmall.common.domain.account.AccountAdapter;
import com.kiseok.pingmall.common.domain.account.AccountRepository;
import com.kiseok.pingmall.common.domain.comment.Comment;
import com.kiseok.pingmall.common.domain.comment.CommentRepository;
import com.kiseok.pingmall.common.domain.verification.Verification;
import com.kiseok.pingmall.common.domain.verification.VerificationRepository;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AccountService implements UserDetailsService {

    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final VerificationRepository verificationRepository;
    private final AccountRepository accountRepository;

    public AccountResponseDto loadAccount(Long accountId) {
        Account account = isUserExist(accountId);

        return modelMapper.map(account, AccountResponseDto.class);
    }

    public AccountResponseDto saveAccount(AccountRequestDto requestDto) {
        isUserDuplicated(requestDto);
        isVerifiedAccount(requestDto);
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
        modifyVerification(account.getEmail());
        accountRepository.delete(account);

        return modelMapper.map(account, AccountResponseDto.class);
    }

    private void modifyVerification(String email) {
        Verification verification = verificationRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
        verification.verified();
        verificationRepository.save(verification);
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

    private void isVerifiedAccount(AccountRequestDto requestDto) {
        Verification verification = verificationRepository.findByEmail(requestDto.getEmail()).orElseThrow(UserNotFoundException::new);
        if(!verification.getIsVerified())   {
            throw new UserNotVerifiedException();
        }
    }
}
