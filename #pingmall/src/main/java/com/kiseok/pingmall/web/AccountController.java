package com.kiseok.pingmall.web;

import com.kiseok.pingmall.api.service.AccountService;
import com.kiseok.pingmall.web.dto.account.AccountModifyRequestDto;
import com.kiseok.pingmall.web.dto.account.AccountRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/accounts", produces = MediaTypes.HAL_JSON_VALUE)
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/{id}")
    ResponseEntity<?> loadAccount(@PathVariable Long id)    {
        return accountService.loadAccount(id);
    }

    @PostMapping
    ResponseEntity<?> saveAccount(@RequestBody @Valid AccountRequestDto requestDto, Errors errors) {
        if(errors.hasErrors())  {
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }

        return accountService.saveAccount(requestDto);
    }

    @PutMapping("/{id}")
    ResponseEntity<?> modifyAccount(@PathVariable Long id, @RequestBody @Valid AccountModifyRequestDto requestDto, Errors errors)   {
        if(errors.hasErrors())  {
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }

        return accountService.modifyAccount(id, requestDto);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<?> removeAccount(@PathVariable Long id)  {
        return accountService.removeAccount(id);
    }

}
