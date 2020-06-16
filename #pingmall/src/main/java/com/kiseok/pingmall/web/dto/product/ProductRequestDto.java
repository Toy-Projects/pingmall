package com.kiseok.pingmall.web.dto.product;

import com.kiseok.pingmall.common.domain.account.Account;
import com.kiseok.pingmall.common.domain.product.Product;
import com.kiseok.pingmall.common.domain.product.ProductCategory;
import lombok.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class ProductRequestDto {

    @NotBlank(message = "제품명은 반드시 입력해야 합니다.")
    private String name;

    @NotBlank(message = "제품 사이즈는 반드시 입력해야 합니다.")
    private String size;

    private String image;

    @Min(value = 1, message = "가격은 0원보다 커야합니다.")
    @NotNull
    private Long price;

    @NotNull
    private Long stock;

    private ProductCategory category;

    public Product toEntity(Account currentUser) {
        return Product.builder()
                .name(name)
                .size(size)
                .image(image)
                .price(price)
                .stock(stock)
                .category(category)
                .seller(currentUser)
                .registeredAt(LocalDateTime.now())
                .build();
    }
}
