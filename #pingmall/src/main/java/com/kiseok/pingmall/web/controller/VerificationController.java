package com.kiseok.pingmall.web.controller;

import com.kiseok.pingmall.api.service.VerificationService;
import com.kiseok.pingmall.web.dto.verification.VerificationCodeRequestDto;
import com.kiseok.pingmall.web.dto.verification.VerificationEmailRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/verifications", produces = MediaTypes.HAL_JSON_VALUE)
public class VerificationController {

    private final VerificationService verificationService;

    @PostMapping
    ResponseEntity<?> verifyEmail(@RequestBody @Valid VerificationEmailRequestDto requestDto)  {
        return verificationService.verifyEmail(requestDto);
    }

    @PutMapping
    ResponseEntity<?> verifyCode(@RequestBody @Valid VerificationCodeRequestDto requestDto)  {
        return verificationService.verifyCode(requestDto);
    }

}
