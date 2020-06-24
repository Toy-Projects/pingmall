package com.kiseok.pingmall.web.controller;

import com.kiseok.pingmall.api.service.OrdersService;
import com.kiseok.pingmall.common.domain.account.Account;
import com.kiseok.pingmall.common.domain.account.CurrentUser;
import com.kiseok.pingmall.web.dto.order.OrdersRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.ResponseEntity;
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

    private final OrdersService ordersService;

    @PostMapping
    ResponseEntity<?> saveOrders(@RequestBody List<@Valid OrdersRequestDto> ordersRequestDtoList, @CurrentUser Account currentUser)   {
        return ordersService.saveOrders(ordersRequestDtoList, currentUser);
    }
}
