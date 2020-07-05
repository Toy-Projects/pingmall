package com.kiseok.pingmall.common.domain.account;

import com.kiseok.pingmall.web.controller.AccountController;
import com.kiseok.pingmall.web.dto.account.AccountResponseDto;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@NoArgsConstructor
public class AccountResource extends EntityModel<AccountResponseDto> {

    public AccountResource(AccountResponseDto responseDto, Link ...links) {
        super(responseDto, links);
        add(linkTo(AccountController.class).slash(responseDto.getId()).withSelfRel());
    }
}
