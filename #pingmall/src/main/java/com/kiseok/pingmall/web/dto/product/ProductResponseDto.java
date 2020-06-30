package com.kiseok.pingmall.web.dto.product;

import com.kiseok.pingmall.common.domain.account.Account;
import com.kiseok.pingmall.common.domain.product.ProductCategory;
import lombok.*;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class ProductResponseDto {

    private Long id;
    private String name;
    private String size;
    private String image;
    private Long price;
    private Long stock;
    private ProductCategory category;
    private Account seller;
    private Account buyer;
}
