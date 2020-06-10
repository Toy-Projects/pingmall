package com.kiseok.pingmall.web.dto.product;

import com.kiseok.pingmall.common.domain.account.Account;
import com.kiseok.pingmall.common.domain.account.CurrentUser;
import com.kiseok.pingmall.common.domain.product.Product;
import com.kiseok.pingmall.common.domain.product.ProductCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Getter @Builder
@NoArgsConstructor @AllArgsConstructor
public class ProductRequestDto {

    @NotBlank(message = "제품명은 반드시 입력해야 합니다.")
    private String name;

    @NotBlank(message = "제품 사이즈는 반드시 입력해야 합니다.")
    private String size;

    private String image;

    private ProductCategory category;

    public Product toEntity(@CurrentUser Account currentUser) {
        return Product.builder()
                .name(name)
                .size(size)
                .image(image)
                .category(category)
                .seller(currentUser)
                .registeredAt(LocalDateTime.now())
                .build();
    }
}
