package com.kiseok.pingmall.web.controller;

import com.kiseok.pingmall.api.service.ProductService;
import com.kiseok.pingmall.common.domain.account.Account;
import com.kiseok.pingmall.common.domain.account.CurrentUser;
import com.kiseok.pingmall.web.dto.product.ProductRequestDto;
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
@RequestMapping(value = "/api/products", produces = MediaTypes.HAL_JSON_VALUE)
public class ProductController {

    private final ProductService productService;

    @PostMapping
    ResponseEntity<?> saveProduct(@RequestBody @Valid ProductRequestDto requestDto, Errors errors, @CurrentUser Account currentUser)   {
        if(errors.hasErrors())  {
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }

        return productService.saveProduct(requestDto, currentUser);
    }

}
