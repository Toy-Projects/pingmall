package com.kiseok.pingmall.web.controller;

import com.kiseok.pingmall.api.service.ProductService;
import com.kiseok.pingmall.common.domain.account.Account;
import com.kiseok.pingmall.common.domain.account.CurrentUser;
import com.kiseok.pingmall.common.domain.product.Product;
import com.kiseok.pingmall.web.dto.product.ProductRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/products", produces = MediaTypes.HAL_JSON_VALUE)
public class ProductController {

    private final ProductService productService;

    @GetMapping
    ResponseEntity<?> loadAllProducts() {
        return productService.loadAllProducts();
    }

    @GetMapping("/{productId}")
    ResponseEntity<?> loadProduct(@PathVariable Long productId)  {
        return productService.loadProduct(productId);
    }

    @PostMapping
    ResponseEntity<?> saveProduct(@RequestBody @Valid ProductRequestDto requestDto, @CurrentUser Account currentUser)  {
        return productService.saveProduct(requestDto, currentUser);
    }

    @PutMapping("/{productId}")
    ResponseEntity<?> modifyProduct(@PathVariable Long productId, @RequestBody @Valid ProductRequestDto requestDto, @CurrentUser Account currentUser)    {
        return productService.modifyProduct(productId, requestDto, currentUser);
    }

    @DeleteMapping("/{productId}")
    ResponseEntity<?> deleteProduct(@PathVariable Long productId, @CurrentUser Account currentUser)    {
        return productService.deleteProduct(productId, currentUser);
    }

}
