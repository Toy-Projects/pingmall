package com.kiseok.pingmall.web.controller;

import com.kiseok.pingmall.api.service.LoginService;
import com.kiseok.pingmall.common.domain.LoginResource;
import com.kiseok.pingmall.web.dto.LoginRequestDto;
import com.kiseok.pingmall.web.dto.jwt.JwtResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.validation.Valid;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/login", produces = MediaTypes.HAL_JSON_VALUE)
public class LoginController {

    private final LoginService loginService;

    @PostMapping
    ResponseEntity<?> loginAccount(@RequestBody @Valid LoginRequestDto requestDto)   {
        JwtResponseDto responseDto = loginService.loginAccount(requestDto);
        LoginResource resource = new LoginResource(responseDto);
        resource.add(linkTo(AccountController.class).withRel("create-account"));
        resource.add(linkTo(ProductController.class).withRel("load-all-products"));
        resource.add(new Link("/docs/index.html#resources-account-login").withRel("profile"));

        return ResponseEntity.ok(resource);
    }
}
