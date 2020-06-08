package com.kiseok.pingmall.web;

import com.kiseok.pingmall.api.service.LoginService;
import com.kiseok.pingmall.web.dto.LoginRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
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
    ResponseEntity<?> loginAccount(@RequestBody @Valid LoginRequestDto requestDto, Errors errors)   {
        if(errors.hasErrors())  {
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }

        return loginService.loginAccount(requestDto);
    }
}
