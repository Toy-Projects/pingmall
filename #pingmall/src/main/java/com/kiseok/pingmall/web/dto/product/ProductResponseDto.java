package com.kiseok.pingmall.web.dto.product;

import com.kiseok.pingmall.common.domain.account.Account;
import com.kiseok.pingmall.common.domain.product.ProductCategory;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class ProductResponseDto {

    private Long id;
    private String name;
    private String size;
    private String image;
    private ProductCategory category;
    private LocalDateTime registeredAt;
    private Account seller;
//    private Account buyer
}
