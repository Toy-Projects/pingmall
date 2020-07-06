package com.kiseok.pingmall.web.controller;

import com.kiseok.pingmall.api.service.ProductService;
import com.kiseok.pingmall.common.domain.account.Account;
import com.kiseok.pingmall.common.domain.account.CurrentUser;
import com.kiseok.pingmall.common.domain.product.Product;
import com.kiseok.pingmall.common.domain.product.ProductResource;
import com.kiseok.pingmall.web.dto.product.ProductRequestDto;
import com.kiseok.pingmall.web.dto.product.ProductResponseDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/products", produces = MediaTypes.HAL_JSON_VALUE)
public class ProductController {

    private final ModelMapper modelMapper;
    private final ProductService productService;

    @GetMapping
    ResponseEntity<?> loadAllProducts(Pageable pageable, PagedResourcesAssembler<Product> assembler) {
        Page<Product> products = productService.loadAllProducts(pageable);
        PagedModel<ProductResource> productResources = assembler.toModel(products, product ->
        {
            ProductResponseDto responseDto = modelMapper.map(product, ProductResponseDto.class);
            ProductResource resource = new ProductResource(responseDto);
            WebMvcLinkBuilder selfLinkBuilder = linkTo(ProductController.class).slash(responseDto.getId());
            resource.add(linkTo(ProductController.class).withRel("create-product"));
            resource.add(selfLinkBuilder.withRel("modify-product"));
            resource.add(selfLinkBuilder.withRel("delete-product"));
            resource.add(linkTo(OrdersController.class).withRel("create-orders"));
            resource.add(new Link("/docs/index.html#resources-product-load").withRel("profile"));

            return resource;
        });
        productResources.add(linkTo(ProductController.class).withRel("create-product"));
        productResources.add(new Link("/docs/index.html#resources-product-load-all").withRel("profile"));

        return ResponseEntity.ok(productResources);
    }

    @GetMapping("/{productId}")
    ResponseEntity<?> loadProduct(@PathVariable Long productId)  {
        ProductResponseDto responseDto = productService.loadProduct(productId);
        WebMvcLinkBuilder selfLinkBuilder = linkTo(ProductController.class).slash(responseDto.getId());
        ProductResource resource = new ProductResource(responseDto);
        resource.add(linkTo(ProductController.class).withRel("load-all-products"));
        resource.add(linkTo(ProductController.class).withRel("create-product"));
        resource.add(selfLinkBuilder.withRel("modify-product"));
        resource.add(selfLinkBuilder.withRel("delete-product"));
        resource.add(linkTo(OrdersController.class).withRel("create-orders"));
        resource.add(new Link("/docs/index.html#resources-product-load").withRel("profile"));

        return ResponseEntity.ok(resource);
    }

    @PostMapping
    ResponseEntity<?> saveProduct(@RequestBody @Valid ProductRequestDto requestDto, @CurrentUser Account currentUser)  {
        ProductResponseDto responseDto = productService.saveProduct(requestDto, currentUser);
        WebMvcLinkBuilder selfLinkBuilder = linkTo(ProductController.class).slash(responseDto.getId());
        ProductResource resource = new ProductResource(responseDto);
        resource.add(linkTo(ProductController.class).withRel("load-all-products"));
        resource.add(selfLinkBuilder.withRel("load-product"));
        resource.add(selfLinkBuilder.withRel("modify-product"));
        resource.add(selfLinkBuilder.withRel("delete-product"));
        resource.add(linkTo(OrdersController.class).withRel("create-orders"));
        resource.add(new Link("/docs/index.html#resources-product-create").withRel("profile"));

        return ResponseEntity.created(selfLinkBuilder.toUri()).body(resource);
    }

    @PutMapping("/{productId}")
    ResponseEntity<?> modifyProduct(@PathVariable Long productId, @RequestBody @Valid ProductRequestDto requestDto, @CurrentUser Account currentUser)    {
        ProductResponseDto responseDto = productService.modifyProduct(productId, requestDto, currentUser);
        WebMvcLinkBuilder selfLinkBuilder = linkTo(ProductController.class).slash(responseDto.getId());
        ProductResource resource = new ProductResource(responseDto);
        resource.add(linkTo(ProductController.class).withRel("load-all-products"));
        resource.add(selfLinkBuilder.withRel("load-product"));
        resource.add(linkTo(ProductController.class).withRel("create-product"));
        resource.add(selfLinkBuilder.withRel("delete-product"));
        resource.add(linkTo(OrdersController.class).withRel("create-orders"));
        resource.add(new Link("/docs/index.html#resources-product-modify").withRel("profile"));

        return ResponseEntity.ok(resource);
    }

    @DeleteMapping("/{productId}")
    ResponseEntity<?> deleteProduct(@PathVariable Long productId, @CurrentUser Account currentUser)    {
        ProductResponseDto responseDto = productService.deleteProduct(productId, currentUser);
        ProductResource resource = new ProductResource(responseDto);
        resource.add(linkTo(ProductController.class).withRel("load-all-products"));
        resource.add(linkTo(ProductController.class).withRel("create-product"));
        resource.add(linkTo(OrdersController.class).withRel("create-orders"));
        resource.add(new Link("/docs/index.html#resources-product-delete").withRel("profile"));

        return ResponseEntity.ok(resource);
    }

}
