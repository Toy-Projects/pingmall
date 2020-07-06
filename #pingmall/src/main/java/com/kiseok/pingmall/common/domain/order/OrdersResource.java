package com.kiseok.pingmall.common.domain.order;

import com.kiseok.pingmall.web.controller.OrdersController;
import com.kiseok.pingmall.web.dto.order.OrdersResponseDto;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@NoArgsConstructor
public class OrdersResource extends CollectionModel<EntityModel<OrdersResponseDto>> {

    public OrdersResource(Iterable<EntityModel<OrdersResponseDto>> responseDto, Link... links) {
        super(responseDto, links);
        add(linkTo(OrdersController.class).withSelfRel());
    }
}
