package com.kiseok.pingmall.web.controller;

import com.kiseok.pingmall.api.service.ProductService;
import com.kiseok.pingmall.common.domain.resources.ModelResource;
import com.kiseok.pingmall.common.domain.account.Account;
import com.kiseok.pingmall.common.domain.account.CurrentUser;
import com.kiseok.pingmall.common.domain.product.Product;
import com.kiseok.pingmall.web.dto.product.ProductRequestDto;
import com.kiseok.pingmall.web.dto.product.ProductResponseDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import static com.kiseok.pingmall.common.domain.resources.RestDocsResource.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/products", produces = MediaTypes.HAL_JSON_VALUE)
public class ProductController {

    private final ModelMapper modelMapper;
    private final ModelResource modelResource;
    private final ProductService productService;

    @GetMapping
    ResponseEntity<?> loadAllProducts(Pageable pageable, PagedResourcesAssembler<Product> assembler) {
        Page<Product> products = productService.loadAllProducts(pageable);
        PagedModel<EntityModel<?>> productResources = assembler.toModel(products, product ->
        {
            ProductResponseDto responseDto = modelMapper.map(product, ProductResponseDto.class);
            WebMvcLinkBuilder selfLinkBuilder = linkTo(ProductController.class).slash(responseDto.getId());
            EntityModel<?> resource = modelResource.getEntityModel(responseDto, selfLinkBuilder, LOAD_PRODUCT.getProfile());
            resource.add(linkTo(ProductController.class).withRel(CREATE_PRODUCT.getRel()));
            resource.add(selfLinkBuilder.withRel(CREATE_PRODUCT_IMAGE.getRel()));
            resource.add(selfLinkBuilder.withRel(MODIFY_PRODUCT.getRel()));
            resource.add(selfLinkBuilder.withRel(DELETE_PRODUCT.getRel()));
            resource.add(linkTo(OrdersController.class).withRel(CREATE_ORDERS.getRel()));

            return resource;
        });
        productResources.add(linkTo(ProductController.class).withRel(CREATE_PRODUCT.getRel()));
        productResources.add(Link.of(LOAD_ALL_PRODUCT.getProfile()).withRel(PROFILE.getRel()));

        return ResponseEntity.ok(productResources);
    }

    @GetMapping("/{productId}")
    ResponseEntity<?> loadProduct(@PathVariable Long productId)  {
        ProductResponseDto responseDto = productService.loadProduct(productId);
        WebMvcLinkBuilder selfLinkBuilder = linkTo(ProductController.class).slash(responseDto.getId());
        EntityModel<?> resource = modelResource.getEntityModel(responseDto, selfLinkBuilder, LOAD_PRODUCT.getProfile());
        resource.add(linkTo(ProductController.class).withRel(LOAD_ALL_PRODUCT.getRel()));
        resource.add(linkTo(ProductController.class).withRel(CREATE_PRODUCT.getRel()));
        resource.add(selfLinkBuilder.withRel(CREATE_PRODUCT_IMAGE.getRel()));
        resource.add(selfLinkBuilder.withRel(MODIFY_PRODUCT.getRel()));
        resource.add(selfLinkBuilder.withRel(DELETE_PRODUCT.getRel()));
        resource.add(linkTo(OrdersController.class).withRel(CREATE_ORDERS.getRel()));

        return ResponseEntity.ok(resource);
    }

    @PostMapping
    ResponseEntity<?> saveProduct(@RequestBody @Valid ProductRequestDto requestDto, @CurrentUser Account currentUser)  {
        ProductResponseDto responseDto = productService.saveProduct(requestDto, currentUser);
        WebMvcLinkBuilder selfLinkBuilder = linkTo(ProductController.class).slash(responseDto.getId());
        EntityModel<?> resource = modelResource.getEntityModel(responseDto, selfLinkBuilder, CREATE_PRODUCT.getProfile());
        resource.add(linkTo(ProductController.class).withRel(LOAD_ALL_PRODUCT.getRel()));
        resource.add(selfLinkBuilder.withRel(LOAD_PRODUCT.getRel()));
        resource.add(selfLinkBuilder.withRel(CREATE_PRODUCT_IMAGE.getRel()));
        resource.add(selfLinkBuilder.withRel(MODIFY_PRODUCT.getRel()));
        resource.add(selfLinkBuilder.withRel(DELETE_PRODUCT.getRel()));
        resource.add(linkTo(OrdersController.class).withRel(CREATE_ORDERS.getRel()));

        return ResponseEntity.created(selfLinkBuilder.toUri()).body(resource);
    }

    @PutMapping("/{productId}")
    ResponseEntity<?> modifyProduct(@PathVariable Long productId, @RequestBody @Valid ProductRequestDto requestDto, @CurrentUser Account currentUser)    {
        ProductResponseDto responseDto = productService.modifyProduct(productId, requestDto, currentUser);
        WebMvcLinkBuilder selfLinkBuilder = linkTo(ProductController.class).slash(responseDto.getId());
        EntityModel<?> resource = modelResource.getEntityModel(responseDto, selfLinkBuilder, MODIFY_PRODUCT.getProfile());
        resource.add(linkTo(ProductController.class).withRel(LOAD_ALL_PRODUCT.getRel()));
        resource.add(selfLinkBuilder.withRel(LOAD_PRODUCT.getRel()));
        resource.add(linkTo(ProductController.class).withRel(CREATE_PRODUCT.getRel()));
        resource.add(selfLinkBuilder.withRel(CREATE_PRODUCT_IMAGE.getRel()));
        resource.add(selfLinkBuilder.withRel(DELETE_PRODUCT.getRel()));
        resource.add(linkTo(OrdersController.class).withRel(CREATE_ORDERS.getRel()));

        return ResponseEntity.ok(resource);
    }

    @DeleteMapping("/{productId}")
    ResponseEntity<?> deleteProduct(@PathVariable Long productId, @CurrentUser Account currentUser)    {
        ProductResponseDto responseDto = productService.deleteProduct(productId, currentUser);
        WebMvcLinkBuilder selfLinkBuilder = linkTo(ProductController.class).slash(responseDto.getId());
        EntityModel<?> resource = modelResource.getEntityModel(responseDto, selfLinkBuilder, DELETE_PRODUCT.getProfile());
        resource.add(linkTo(ProductController.class).withRel(LOAD_ALL_PRODUCT.getRel()));
        resource.add(linkTo(ProductController.class).withRel(CREATE_PRODUCT.getRel()));
        resource.add(selfLinkBuilder.withRel(CREATE_PRODUCT_IMAGE.getRel()));
        resource.add(linkTo(OrdersController.class).withRel(CREATE_ORDERS.getRel()));

        return ResponseEntity.ok(resource);
    }
}
