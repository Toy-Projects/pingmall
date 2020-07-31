package com.kiseok.pingmall.web.controller;

import com.kiseok.pingmall.api.service.AccountService;
import com.kiseok.pingmall.common.resources.ModelResource;
import com.kiseok.pingmall.common.domain.account.Account;
import com.kiseok.pingmall.common.domain.account.CurrentUser;
import com.kiseok.pingmall.web.dto.account.AccountDepositRequestDto;
import com.kiseok.pingmall.web.dto.account.AccountModifyRequestDto;
import com.kiseok.pingmall.web.dto.account.AccountRequestDto;
import com.kiseok.pingmall.web.dto.account.AccountResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import static com.kiseok.pingmall.common.resources.RestDocsResource.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/accounts", produces = MediaTypes.HAL_JSON_VALUE)
public class AccountController {

    private final ModelResource modelResource;
    private final AccountService accountService;

    @GetMapping("/{accountId}")
    ResponseEntity<?> loadAccount(@PathVariable Long accountId)   {
        AccountResponseDto responseDto = accountService.loadAccount(accountId);
        WebMvcLinkBuilder selfLinkBuilder = linkTo(AccountController.class).slash(responseDto.getId());
        EntityModel<?> resource = modelResource.getEntityModel(responseDto, selfLinkBuilder, LOAD_ACCOUNT.getProfile());
        resource.add(linkTo(AccountController.class).withRel(CREATE_ACCOUNT.getRel()));
        resource.add(selfLinkBuilder.slash("balance").withRel(DEPOSIT_ACCOUNT.getRel()));
        resource.add(selfLinkBuilder.withRel(MODIFY_ACCOUNT.getRel()));
        resource.add(selfLinkBuilder.withRel(DELETE_ACCOUNT.getRel()));

        return ResponseEntity.ok(resource);
    }

    @PostMapping
    ResponseEntity<?> saveAccount(@RequestBody @Valid AccountRequestDto requestDto) {
        AccountResponseDto responseDto = accountService.saveAccount(requestDto);
        WebMvcLinkBuilder selfLinkBuilder = linkTo(AccountController.class).slash(responseDto.getId());
        EntityModel<?> resource = modelResource.getEntityModel(responseDto, selfLinkBuilder, CREATE_ACCOUNT.getProfile());
        resource.add(linkTo(LoginController.class).withRel(LOGIN_ACCOUNT.getRel()));

        return ResponseEntity.created(selfLinkBuilder.toUri()).body(resource);
    }

    @PutMapping("/{accountId}/balance")
    ResponseEntity<?> depositAccount(@PathVariable Long accountId, @RequestBody @Valid AccountDepositRequestDto requestDto, @CurrentUser Account currentUser)   {
        AccountResponseDto responseDto = accountService.depositAccount(accountId, requestDto, currentUser);
        WebMvcLinkBuilder selfLinkBuilder = linkTo(AccountController.class).slash(responseDto.getId());
        EntityModel<?> resource = modelResource.getEntityModel(responseDto, selfLinkBuilder, DEPOSIT_ACCOUNT.getProfile());
        resource.add(selfLinkBuilder.withRel(LOAD_ACCOUNT.getRel()));
        resource.add(selfLinkBuilder.withRel(MODIFY_ACCOUNT.getRel()));
        resource.add(selfLinkBuilder.withRel(DELETE_ACCOUNT.getRel()));

        return ResponseEntity.ok(resource);
    }

    @PutMapping("/{accountId}")
    ResponseEntity<?> modifyAccount(@PathVariable Long accountId, @RequestBody @Valid AccountModifyRequestDto requestDto, @CurrentUser Account currentUser)   {
        AccountResponseDto responseDto = accountService.modifyAccount(accountId, requestDto, currentUser);
        WebMvcLinkBuilder selfLinkBuilder = linkTo(AccountController.class).slash(responseDto.getId());
        EntityModel<?> resource = modelResource.getEntityModel(responseDto, selfLinkBuilder, MODIFY_ACCOUNT.getProfile());
        resource.add(selfLinkBuilder.withRel(LOAD_ACCOUNT.getRel()));
        resource.add(selfLinkBuilder.slash("balance").withRel(DEPOSIT_ACCOUNT.getRel()));
        resource.add(selfLinkBuilder.withRel(DELETE_ACCOUNT.getRel()));

        return ResponseEntity.ok(resource);
    }

    @DeleteMapping("/{accountId}")
    ResponseEntity<?> removeAccount(@PathVariable Long accountId, @CurrentUser Account currentUser)  {
        AccountResponseDto responseDto = accountService.removeAccount(accountId, currentUser);
        WebMvcLinkBuilder selfLinkBuilder = linkTo(AccountController.class).slash(responseDto.getId());
        EntityModel<?> resource = modelResource.getEntityModel(responseDto, selfLinkBuilder, DELETE_ACCOUNT.getProfile());
        resource.add(linkTo(LoginController.class).withRel(LOGIN_ACCOUNT.getRel()));
        resource.add(linkTo(AccountController.class).withRel(CREATE_ACCOUNT.getRel()));

        return ResponseEntity.ok(resource);
    }
}
