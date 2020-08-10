package com.kiseok.pingmall.api.service;

import com.kiseok.pingmall.api.exception.account.UserIdEqualsException;
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

    public List<OrdersResponseDto> saveOrders(List<OrdersRequestDto> requestDtoList, Account currentUser)    {
        List<OrdersResponseDto> responseDtoList = new LinkedList<>();
        Account account = accountRepository.findById(currentUser.getId()).orElseThrow(UserNotFoundException::new);
        isAvailableOrder(account, requestDtoList);
        requestDtoList.forEach(requestDto -> {
            Product product = productRepository.findById(requestDto.getProductId()).orElseThrow(ProductNotFoundException::new);
            isEqualsToUserId(account, product);
            account.reduceBalance(requestDto, product.getPrice());
            product.reduceStock(requestDto);
            Orders orders = requestDto.toEntity(account, product);
            orders = ordersRepository.save(orders);
            account.getOrders().add(orders);
            product.getOrders().add(orders);
            accountRepository.save(account);
            productRepository.save(product);
            OrdersResponseDto responseDto = modelMapper.map(orders, OrdersResponseDto.class);
            responseDtoList.add(responseDto);
        });

        return responseDtoList;
    }

    private void isAvailableOrder(Account account, List<OrdersRequestDto> requestDtoList) {
        long totalPrice = requestDtoList
                .stream()
                .map(requestDto -> productRepository.findById(requestDto.getProductId()).orElseThrow(ProductNotFoundException::new))
                .mapToLong(Product::getPrice)
                .sum();

        account.checkBalance(totalPrice);
    }

    private void isEqualsToUserId(Account account, Product product) {
        if(product.getSeller().getId().equals(account.getId())) {
            throw new UserIdEqualsException();
        }
    }
}
