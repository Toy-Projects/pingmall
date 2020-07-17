package com.kiseok.pingmall.api.service;

import com.kiseok.pingmall.api.exception.account.UserNotFoundException;
import com.kiseok.pingmall.common.domain.account.Account;
import com.kiseok.pingmall.common.domain.account.AccountRepository;
import com.kiseok.pingmall.web.dto.find.FindEmailResponseDto;
import com.kiseok.pingmall.web.dto.find.FindPasswordRequestDto;
import com.kiseok.pingmall.web.dto.find.FindPasswordResponseDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class FindService {

    private final JavaMailSender javaMailSender;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final AccountRepository accountRepository;
    private static final String PINGMALL = "#pingmall";

    public FindEmailResponseDto findEmail(String name)  {
        Account account = accountRepository.findByName(name).orElseThrow(UserNotFoundException::new);

        return modelMapper.map(account, FindEmailResponseDto.class);
    }

    public FindPasswordResponseDto findPassword(FindPasswordRequestDto requestDto) {
        Account account = accountRepository.findByEmailAndName(requestDto.getEmail(), requestDto.getName()).orElseThrow(UserNotFoundException::new);
        String newPassword = UUID.randomUUID().toString();
        account.updatePassword(createTemporaryPassword(newPassword));
        sendMail(account, newPassword);

        return modelMapper.map(accountRepository.save(account), FindPasswordResponseDto.class);
    }

    private void sendMail(Account account, String newPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(account.getEmail());
        message.setFrom(PINGMALL);
        message.setSubject(PINGMALL + "에서 보낸 임시비밀번호 입니다.");
        message.setText("임시 비밀번호는 " + newPassword + "입니다." + "\n"
                + "임시 비밀번호로 로그인 후, 즉시 비밀번호를 변경해 주세요!");
        javaMailSender.send(message);
    }

    private String createTemporaryPassword(String newPassword) {
        return passwordEncoder.encode(newPassword);
    }
}
