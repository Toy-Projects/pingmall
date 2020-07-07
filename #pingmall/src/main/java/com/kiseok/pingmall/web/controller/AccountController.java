package com.kiseok.pingmall.web.controller;

import com.kiseok.pingmall.api.service.AccountService;
import com.kiseok.pingmall.common.domain.account.Account;
import com.kiseok.pingmall.common.domain.account.AccountResource;
import com.kiseok.pingmall.common.domain.account.CurrentUser;
import com.kiseok.pingmall.web.dto.account.AccountDepositRequestDto;
import com.kiseok.pingmall.web.dto.account.AccountModifyRequestDto;
import com.kiseok.pingmall.web.dto.account.AccountRequestDto;
import com.kiseok.pingmall.web.dto.account.AccountResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/accounts", produces = MediaTypes.HAL_JSON_VALUE)
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/{accountId}")
    ResponseEntity<?> loadAccount(@PathVariable Long accountId)   {
        AccountResponseDto responseDto = accountService.loadAccount(accountId);
        WebMvcLinkBuilder selfLinkBuilder = linkTo(AccountController.class).slash(responseDto.getId());
        AccountResource resource = new AccountResource(responseDto);
        resource.add(linkTo(AccountController.class).withRel("create-account"));
        resource.add(selfLinkBuilder.slash("balance").withRel("deposit-account"));
        resource.add(selfLinkBuilder.withRel("modify-account"));
        resource.add(selfLinkBuilder.withRel("delete-account"));
        resource.add(new Link("/docs/index.html#resources-account-load").withRel("profile"));

        return ResponseEntity.ok(resource);
    }

    @PostMapping
    ResponseEntity<?> saveAccount(@RequestBody @Valid AccountRequestDto requestDto) {
        AccountResponseDto responseDto = accountService.saveAccount(requestDto);
        WebMvcLinkBuilder selfLinkBuilder = linkTo(AccountController.class).slash(responseDto.getId());
        AccountResource resource = new AccountResource(responseDto);
        resource.add(linkTo(LoginController.class).withRel("login-account"));
        resource.add(new Link("/docs/index.html#resources-account-create").withRel("profile"));

        return ResponseEntity.created(selfLinkBuilder.toUri()).body(resource);
    }

    @PutMapping("/{accountId}/balance")
    ResponseEntity<?> depositAccount(@PathVariable Long accountId, @RequestBody @Valid AccountDepositRequestDto requestDto, @CurrentUser Account currentUser)   {
        AccountResponseDto responseDto = accountService.depositAccount(accountId, requestDto, currentUser);
        WebMvcLinkBuilder selfLinkBuilder = linkTo(AccountController.class).slash(responseDto.getId());
        AccountResource resource = new AccountResource(responseDto);
        resource.add(selfLinkBuilder.withRel("load-account"));
        resource.add(selfLinkBuilder.withRel("modify-account"));
        resource.add(selfLinkBuilder.withRel("delete-account"));
        resource.add(new Link("/docs/index.html#resources-account-deposit").withRel("profile"));

        return ResponseEntity.ok(resource);
    }

    @PutMapping("/{accountId}")
    ResponseEntity<?> modifyAccount(@PathVariable Long accountId, @RequestBody @Valid AccountModifyRequestDto requestDto, @CurrentUser Account currentUser)   {
        AccountResponseDto responseDto = accountService.modifyAccount(accountId, requestDto, currentUser);
        WebMvcLinkBuilder selfLinkBuilder = linkTo(AccountController.class).slash(responseDto.getId());
        AccountResource resource = new AccountResource(responseDto);
        resource.add(selfLinkBuilder.withRel("load-account"));
        resource.add(selfLinkBuilder.slash("balance").withRel("deposit-account"));
        resource.add(selfLinkBuilder.withRel("delete-account"));
        resource.add(new Link("/docs/index.html#resources-account-modify").withRel("profile"));

        return ResponseEntity.ok(resource);
    }

    @DeleteMapping("/{accountId}")
    ResponseEntity<?> removeAccount(@PathVariable Long accountId, @CurrentUser Account currentUser)  {
        AccountResponseDto responseDto = accountService.removeAccount(accountId, currentUser);
        AccountResource resource = new AccountResource(responseDto);
        resource.add(linkTo(LoginController.class).withRel("login-account"));
        resource.add(linkTo(AccountController.class).withRel("create-account"));
        resource.add(new Link("/docs/index.html#resources-account-delete").withRel("profile"));

        return ResponseEntity.ok(resource);
    }

}
