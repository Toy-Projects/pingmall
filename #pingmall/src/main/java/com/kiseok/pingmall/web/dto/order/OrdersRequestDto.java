package com.kiseok.pingmall.web.dto.order;

import com.kiseok.pingmall.common.domain.account.Account;
import com.kiseok.pingmall.common.domain.order.Orders;
import com.kiseok.pingmall.common.domain.product.Product;
import lombok.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class OrdersRequestDto {

    @Min(value = 1, message = "1개 부터 수량을 선택할 수 있습니다.")
    @NotNull(message = "제품의 수량은 반드시 입력해야 합니다.")
    private Long amount;

    @NotNull(message = "제품을 반드시 선택해야 합니다.")
    private Long productId;

    public Orders toEntity(Account account, Product product) {
        return Orders.builder()
                .number(UUID.randomUUID().toString())
                .amount(amount)
                .buyer(account)
                .product(product)
                .build();
    }
}
