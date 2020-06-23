package com.kiseok.pingmall.web.controller;

import com.kiseok.pingmall.api.service.LoginService;
import com.kiseok.pingmall.web.dto.LoginRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.validation.Valid;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/login", produces = MediaTypes.HAL_JSON_VALUE)
public class LoginController {

    private final LoginService loginService;

    @PostMapping
    ResponseEntity<?> loginAccount(@RequestBody @Valid LoginRequestDto requestDto)   {
        return loginService.loginAccount(requestDto);
    }
}
