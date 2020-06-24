package com.kiseok.pingmall.api.service;

import com.kiseok.pingmall.api.exception.account.UserNotFoundException;
import com.kiseok.pingmall.api.exception.product.ProductNotFoundException;
import com.kiseok.pingmall.common.domain.account.Account;
import com.kiseok.pingmall.common.domain.account.AccountRepository;
import com.kiseok.pingmall.common.domain.order.Orders;
import com.kiseok.pingmall.common.domain.order.OrdersRepository;
import com.kiseok.pingmall.common.domain.product.Product;
import com.kiseok.pingmall.common.domain.product.ProductRepository;
import com.kiseok.pingmall.web.dto.order.OrdersRequestDto;
import com.kiseok.pingmall.web.dto.order.OrdersResponseDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.LinkedList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class OrdersService {

    private final ModelMapper modelMapper;
    private final ProductRepository productRepository;
    private final AccountRepository accountRepository;
    private final OrdersRepository ordersRepository;

    // TODO 1. ResponseDto로 매핑 안되는 문제
    // TODO 2. 다양한 예외 처리
    // TODO 3. DELETE 구현

    public ResponseEntity<?> saveOrders(List<OrdersRequestDto> requestDtoList, Account currentUser)    {
        List<OrdersResponseDto> responseDtoList = new LinkedList<>();
        Account account = accountRepository.findById(currentUser.getId()).orElseThrow(UserNotFoundException::new);
        requestDtoList.forEach(requestDto -> {
            Product product = productRepository.findById(requestDto.getProductId()).orElseThrow(ProductNotFoundException::new);
            account.reduceBalance(requestDto, product.getPrice());
            product.reduceStock(requestDto);
            Orders orders = ordersRepository.save(requestDto.toEntity(account, product));
            OrdersResponseDto responseDto = modelMapper.map(orders, OrdersResponseDto.class);
            responseDtoList.add(responseDto);
        });

        return new ResponseEntity<>(responseDtoList, HttpStatus.CREATED);
    }

}
