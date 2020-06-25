package com.kiseok.pingmall.web.controller;

import com.kiseok.pingmall.api.service.OrdersService;
import com.kiseok.pingmall.common.domain.account.Account;
import com.kiseok.pingmall.common.domain.account.CurrentUser;
import com.kiseok.pingmall.common.domain.order.OrdersValidator;
import com.kiseok.pingmall.web.dto.order.OrdersRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.validation.Valid;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping(value = "/api/orders", produces = MediaTypes.HAL_JSON_VALUE)
@RestController
public class OrdersController {

    private final OrdersValidator ordersValidator;
    private final OrdersService ordersService;

    @PostMapping
    ResponseEntity<?> saveOrders(@RequestBody @Valid List<OrdersRequestDto> requestDtoList, @CurrentUser Account currentUser, BindingResult bindingResult) throws BindException {
        validateOrdersList(requestDtoList, bindingResult);
        return ordersService.saveOrders(requestDtoList, currentUser);
    }

    private void validateOrdersList(@RequestBody @Valid List<OrdersRequestDto> requestDtoList, BindingResult bindingResult) throws BindException {
        ordersValidator.validate(requestDtoList, bindingResult);
        if(bindingResult.hasErrors())   {
            throw new BindException(bindingResult);
        }
    }

}
