package com.kiseok.pingmall.web.controller;

import com.kiseok.pingmall.api.service.ImageService;
import com.kiseok.pingmall.common.resources.ModelResource;
import com.kiseok.pingmall.common.domain.account.Account;
import com.kiseok.pingmall.common.domain.account.CurrentUser;
import com.kiseok.pingmall.web.dto.product.ProductResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import static com.kiseok.pingmall.common.resources.RestDocsResource.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RequiredArgsConstructor
@RequestMapping(value = "/api/images")
@RestController
public class ImageController {

    private final ModelResource modelResource;
    private final ImageService imageService;

    @GetMapping("/products/{productId}/{imagePath:.+}")
    ResponseEntity<?> loadImage(@PathVariable Long productId, @PathVariable String imagePath, HttpServletRequest request) {
        return imageService.loadImage(imagePath, request);
    }

    @PostMapping("/products/{productId}")
    ResponseEntity<?> saveProductImage(@PathVariable Long productId,
                                       @RequestParam(name = "file", required = false) MultipartFile file,
                                       HttpServletRequest request,
                                       @CurrentUser Account currentUser) throws IOException {
        ProductResponseDto responseDto;
        if(file == null)    {
            responseDto = imageService.saveDefaultProductImage(productId, request, currentUser);
        }
        else {
            responseDto = imageService.saveProductImage(productId, file, request, currentUser);
        }
        WebMvcLinkBuilder selfLinkBuilder = linkTo(ProductController.class).slash(responseDto.getId());
        EntityModel<?> resource = modelResource.getEntityModel(responseDto, selfLinkBuilder, CREATE_PRODUCT_IMAGE.getProfile());
        resource.add(linkTo(ProductController.class).withRel(LOAD_ALL_PRODUCT.getRel()));
        resource.add(selfLinkBuilder.withRel(LOAD_PRODUCT.getRel()));
        resource.add(selfLinkBuilder.withRel(MODIFY_PRODUCT.getRel()));
        resource.add(selfLinkBuilder.withRel(DELETE_PRODUCT.getRel()));
        resource.add(linkTo(OrdersController.class).withRel(CREATE_ORDERS.getRel()));

        return ResponseEntity.created(selfLinkBuilder.toUri()).body(resource);
    }
}
