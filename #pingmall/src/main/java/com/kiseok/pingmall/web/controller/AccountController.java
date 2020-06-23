package com.kiseok.pingmall.web.controller;

import com.kiseok.pingmall.api.service.AccountService;
import com.kiseok.pingmall.common.domain.account.Account;
import com.kiseok.pingmall.common.domain.account.CurrentUser;
import com.kiseok.pingmall.web.dto.account.AccountDepositRequestDto;
import com.kiseok.pingmall.web.dto.account.AccountModifyRequestDto;
import com.kiseok.pingmall.web.dto.account.AccountRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/accounts", produces = MediaTypes.HAL_JSON_VALUE)
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/{accountId}")
    ResponseEntity<?> loadAccount(@PathVariable Long accountId)   {
        return accountService.loadAccount(accountId);
    }

    @PostMapping
    ResponseEntity<?> saveAccount(@RequestBody @Valid AccountRequestDto requestDto) {
        return accountService.saveAccount(requestDto);
    }

    @PutMapping("/{id}/balance")
    ResponseEntity<?> depositAccount(@PathVariable Long id, @RequestBody @Valid AccountDepositRequestDto requestDto, @CurrentUser Account currentUser)   {
        return accountService.depositAccount(id, requestDto, currentUser);
    }

    @PutMapping("/{accountId}")
    ResponseEntity<?> modifyAccount(@PathVariable Long accountId, @RequestBody @Valid AccountModifyRequestDto requestDto, @CurrentUser Account currentUser)   {
        return accountService.modifyAccount(accountId, requestDto, currentUser);
    }

    @DeleteMapping("/{accountId}")
    ResponseEntity<?> removeAccount(@PathVariable Long accountId, @CurrentUser Account currentUser)  {
        return accountService.removeAccount(accountId, currentUser);
    }

}
