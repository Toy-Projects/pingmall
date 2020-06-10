package com.kiseok.pingmall.web.controller;

import com.kiseok.pingmall.common.domain.product.ProductCategory;
import com.kiseok.pingmall.web.common.BaseControllerTest;
import com.kiseok.pingmall.web.dto.product.ProductRequestDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductControllerTests extends BaseControllerTest {

    @AfterEach
    void deleteAll()    {
        productRepository.deleteAll();
        accountRepository.deleteAll();
    }

    // TODO 제품 등록 유효성 실패 -> 400 BAD_REQUEST

    // TODO 제품 등록 시 유저 null -> 400 BAD_REQUEST


   @DisplayName("제품 등록 성공 -> 201 CREATED")
    @Test
    void save_product_201() throws Exception    {
        ResultActions actions = this.mockMvc.perform(post(ACCOUNT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createAccountRequestDto())))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").value(createAccountRequestDto().getEmail()))
                .andExpect(jsonPath("password").doesNotExist())
                .andExpect(jsonPath("address").exists())
                .andExpect(jsonPath("accountRole").exists())
                .andExpect(jsonPath("createdAt").exists());

        String token = generateToken(actions);

        ProductRequestDto requestDto = ProductRequestDto.builder()
                .name("롤렉스 서브마리너")
                .size("XL")
                .category(ProductCategory.ACCESSORY)
                .build();

        this.mockMvc.perform(post(PRODUCT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .header(HttpHeaders.AUTHORIZATION, token))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("name").value("롤렉스 서브마리너"))
                .andExpect(jsonPath("size").value("XL"))
                .andExpect(jsonPath("category").value(ProductCategory.ACCESSORY.name()))
        ;
    }
}