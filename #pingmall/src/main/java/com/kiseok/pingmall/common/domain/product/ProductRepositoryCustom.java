package com.kiseok.pingmall.common.domain.product;

import com.kiseok.pingmall.web.dto.product.ProductFilterRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepositoryCustom {
    Page<Product> findByFilter(ProductFilterRequestDto requestDto, Pageable pageable);
}
