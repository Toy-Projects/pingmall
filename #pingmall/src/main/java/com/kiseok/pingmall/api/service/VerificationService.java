package com.kiseok.pingmall.api.service;

import com.kiseok.pingmall.api.exception.InvalidVerificationCodeException;
import com.kiseok.pingmall.api.exception.account.UserDuplicatedException;
import com.kiseok.pingmall.api.exception.account.UserNotFoundException;
import com.kiseok.pingmall.common.domain.account.Account;
import com.kiseok.pingmall.common.domain.account.AccountRepository;
import com.kiseok.pingmall.common.domain.verification.Verification;
import com.kiseok.pingmall.common.domain.verification.VerificationRepository;
import com.kiseok.pingmall.web.dto.verification.VerificationCodeRequestDto;
import com.kiseok.pingmall.web.dto.verification.VerificationEmailRequestDto;
import com.kiseok.pingmall.web.dto.verification.VerificationResponseDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class VerificationService {

    private final JavaMailSender javaMailSender;
    private final ModelMapper modelMapper;
    private final AccountRepository accountRepository;
    private final VerificationRepository verificationRepository;
    private static final String PINGMALL = "#pingmall";

    public ResponseEntity<?> verifyEmail(VerificationEmailRequestDto requestDto) {
        isUserDuplicated(requestDto.getEmail());
        isAlreadySendCode(requestDto);
        String verificationCode = createVerificationCode();
        sendMail(requestDto.getEmail(), verificationCode);
        Verification verification = requestDto.toEntity(verificationCode);
        VerificationResponseDto responseDto = modelMapper.map(verificationRepository.save(verification), VerificationResponseDto.class);

        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    public ResponseEntity<?> verifyCode(VerificationCodeRequestDto requestDto) {
        Verification verification = isEqualVerificationCode(requestDto);
        verification.verified();
        VerificationResponseDto responseDto = modelMapper.map(verificationRepository.save(verification), VerificationResponseDto.class);

        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    private Verification isEqualVerificationCode(VerificationCodeRequestDto requestDto) {
        Verification verification = verificationRepository.findByEmail(requestDto.getEmail()).orElseThrow(UserNotFoundException::new);
        if(!requestDto.getVerificationCode().equals(verification.getVerificationCode()))    {
            throw new InvalidVerificationCodeException();
        }

        return verification;
    }

    private void isUserDuplicated(String email) {
        Optional<Account> optionalAccount = accountRepository.findByEmail(email);
        if(optionalAccount.isPresent()) {
            throw new UserDuplicatedException();
        }
    }

    private void isAlreadySendCode(VerificationEmailRequestDto requestDto) {
        Optional<Verification> optionalVerification = verificationRepository.findByEmail(requestDto.getEmail());
        optionalVerification.ifPresent(verificationRepository::delete);
    }

    private String createVerificationCode() {
        String code = UUID.randomUUID().toString();

        return code.substring(0, 6);
    }

    private void sendMail(String email, String verificationCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setFrom(PINGMALL);
        message.setSubject(PINGMALL + "에서 보낸 인증코드 입니다.");
        message.setText("인증코드는 " + verificationCode + " 입니다." + "\n"
                + "인증코드를 사용해 E-mail 인증을 완료해 주세요!");
        javaMailSender.send(message);
    }
}
