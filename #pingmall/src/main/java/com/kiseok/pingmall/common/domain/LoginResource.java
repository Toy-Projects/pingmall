package com.kiseok.pingmall.common.domain;

import com.kiseok.pingmall.web.controller.LoginController;
import com.kiseok.pingmall.web.dto.jwt.JwtResponseDto;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@NoArgsConstructor
public class LoginResource extends EntityModel<JwtResponseDto> {

    public LoginResource(JwtResponseDto responseDto, Link... links) {
        super(responseDto, links);
        add(linkTo(LoginController.class).withSelfRel());
    }
}
