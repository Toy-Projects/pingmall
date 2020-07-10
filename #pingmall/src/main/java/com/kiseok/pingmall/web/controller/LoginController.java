package com.kiseok.pingmall.web.controller;

import com.kiseok.pingmall.api.service.LoginService;
import com.kiseok.pingmall.common.domain.resources.ModelResource;
import com.kiseok.pingmall.web.dto.LoginRequestDto;
import com.kiseok.pingmall.web.dto.jwt.JwtResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.validation.Valid;
import static com.kiseok.pingmall.common.domain.resources.RestDocsResource.*;
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
        EntityModel<?> resource = modelResource.getEntityModel(responseDto, linkTo(LoginController.class), LOGIN_ACCOUNT.getProfile());
        resource.add(linkTo(AccountController.class).withRel(CREATE_ACCOUNT.getRel()));
        resource.add(linkTo(ProductController.class).withRel(LOAD_ALL_PRODUCT.getRel()));

        return ResponseEntity.ok(resource);
    }
}
