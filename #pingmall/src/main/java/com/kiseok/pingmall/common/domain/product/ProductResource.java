package com.kiseok.pingmall.common.domain.product;

import com.kiseok.pingmall.web.controller.ProductController;
import com.kiseok.pingmall.web.dto.product.ProductResponseDto;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@NoArgsConstructor
public class ProductResource extends EntityModel<ProductResponseDto> {

    public ProductResource(ProductResponseDto responseDto, Link... links) {
        super(responseDto, links);
        add(linkTo(ProductController.class).slash(responseDto.getId()).withSelfRel());
    }
}
