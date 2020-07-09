package com.kiseok.pingmall.web.controller;

import com.kiseok.pingmall.api.service.LoginService;
import com.kiseok.pingmall.common.domain.ModelResource;
import com.kiseok.pingmall.web.dto.LoginRequestDto;
import com.kiseok.pingmall.web.dto.jwt.JwtResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
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

    private final ModelResource modelResource;
    private final LoginService loginService;

    @PostMapping
    ResponseEntity<?> loginAccount(@RequestBody @Valid LoginRequestDto requestDto)   {
        JwtResponseDto responseDto = loginService.loginAccount(requestDto);
        EntityModel<?> resource = modelResource.getEntityModelWithSelfRel(responseDto, linkTo(LoginController.class));
        resource.add(linkTo(AccountController.class).withRel("create-account"));
        resource.add(linkTo(ProductController.class).withRel("load-all-products"));
        resource.add(Link.of("/docs/index.html#resources-account-login").withRel("profile"));

        return ResponseEntity.ok(resource);
    }
}
