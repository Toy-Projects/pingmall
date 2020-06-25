package com.kiseok.pingmall.web.dto.order;

import com.kiseok.pingmall.common.domain.account.Account;
import com.kiseok.pingmall.common.domain.product.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class OrdersResponseDto {

    private Long id;
    private String number;
    private Long amount;
    private LocalDateTime orderedAt;
    private Account buyer;
    private Product product;
}
