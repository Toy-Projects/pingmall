package com.kiseok.pingmall.web.controller;

import com.kiseok.pingmall.api.service.OrdersService;
import com.kiseok.pingmall.common.domain.ModelResource;
import com.kiseok.pingmall.common.domain.account.Account;
import com.kiseok.pingmall.common.domain.account.CurrentUser;
import com.kiseok.pingmall.common.domain.order.OrdersValidator;
import com.kiseok.pingmall.web.dto.order.OrdersRequestDto;
import com.kiseok.pingmall.web.dto.order.OrdersResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RequiredArgsConstructor
@RequestMapping(value = "/api/orders", produces = MediaTypes.HAL_JSON_VALUE)
@RestController
public class OrdersController {

    private final ModelResource modelResource;
    private final OrdersValidator ordersValidator;
    private final OrdersService ordersService;

    @PostMapping
    ResponseEntity<?> saveOrders(@RequestBody @Valid List<OrdersRequestDto> requestDtoList, @CurrentUser Account currentUser, BindingResult bindingResult) throws BindException {
        validateOrdersList(requestDtoList, bindingResult);
        List<OrdersResponseDto> responseDtoList = ordersService.saveOrders(requestDtoList, currentUser);
        List<EntityModel<?>> entityModelList = responseDtoList.stream().map(responseDto -> {
            EntityModel<?> entityModel = modelResource.getEntityModel(responseDto);
            entityModel.add(linkTo(ProductController.class).slash(responseDto.getProduct().getId()).withRel("load-product"));
            return entityModel;
        }).collect(Collectors.toList());

        CollectionModel<EntityModel<?>> resource = modelResource.getCollectionModelWithSelfRel(entityModelList, linkTo(OrdersController.class));
        resource.add(Link.of("/docs/index.html#resources-orders-create").withRel("profile"));

        return new ResponseEntity<>(resource, HttpStatus.CREATED);
    }

    private void validateOrdersList(List<OrdersRequestDto> requestDtoList, BindingResult bindingResult) throws BindException {
        ordersValidator.validate(requestDtoList, bindingResult);
        if(bindingResult.hasErrors())   {
            throw new BindException(bindingResult);
        }
    }

}
