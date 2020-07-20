package com.kiseok.pingmall.web.dto.product;

import com.kiseok.pingmall.common.domain.product.ProductCategory;
import lombok.*;
import org.springframework.lang.Nullable;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class ProductFilterRequestDto {

    @Nullable
    private String name;

    @Nullable
    private String size;

    @Nullable
    private String price;

    @Nullable
    private ProductCategory category;

    @Nullable
    private String orderBy;
}
