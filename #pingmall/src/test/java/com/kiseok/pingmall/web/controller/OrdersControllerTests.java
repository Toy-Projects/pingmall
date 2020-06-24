package com.kiseok.pingmall.web.controller;

import com.kiseok.pingmall.common.domain.account.AccountRole;
import com.kiseok.pingmall.common.domain.product.ProductCategory;
import com.kiseok.pingmall.web.BaseControllerTests;
import com.kiseok.pingmall.web.dto.order.OrdersRequestDto;
import com.kiseok.pingmall.web.dto.product.ProductResponseDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class OrdersControllerTests extends BaseControllerTests {

    @AfterEach
    void tearDown() {
        this.ordersRepository.deleteAll();
        this.productRepository.deleteAll();
        this.accountRepository.deleteAll();
    }

    @DisplayName("정상적으로 주문하기")
    @Test
    void save_orders_201() throws Exception {
        String token = createAccountAndToken();
        String anotherToken = createAnotherAccountAndToken();

        List<Long> productIdList = collectProductIds(token);
        List<OrdersRequestDto> ordersRequestDtoList = new ArrayList<>();
        IntStream.rangeClosed(1, 10).forEach(i -> ordersRequestDtoList.add(createOrdersRequestDto(productIdList.get(i - 1))));

        this.mockMvc.perform(post(ORDERS_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ordersRequestDtoList))
                .header(HttpHeaders.AUTHORIZATION, anotherToken))
                .andDo(print())
                .andExpect(status().isCreated())
        ;
    }

    private String createAccountAndToken() throws Exception {
        ResultActions actions = this.mockMvc.perform(post(ACCOUNT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createAccountRequestDto())))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").value(appProperties.getTestEmail()))
                .andExpect(jsonPath("password").doesNotExist())
                .andExpect(jsonPath("name").value(appProperties.getTestName()))
                .andExpect(jsonPath("address").value(appProperties.getTestAddress()))
                .andExpect(jsonPath("balance").value(appProperties.getTestBalance()))
                .andExpect(jsonPath("accountRole").value(AccountRole.USER.name()))
                .andExpect(jsonPath("createdAt").exists())
        ;

        return generateToken(actions);
    }

    private String createAnotherAccountAndToken() throws Exception {
        ResultActions actions = this.mockMvc.perform(post(ACCOUNT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createAnotherAccountRequestDto())))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").value(ANOTHER + appProperties.getTestEmail()))
                .andExpect(jsonPath("password").doesNotExist())
                .andExpect(jsonPath("name").value(ANOTHER + appProperties.getTestName()))
                .andExpect(jsonPath("address").value(ANOTHER + appProperties.getTestAddress()))
                .andExpect(jsonPath("balance").value(appProperties.getTestBalance()))
                .andExpect(jsonPath("accountRole").value(AccountRole.USER.name()))
                .andExpect(jsonPath("createdAt").exists())
                ;

        return generateToken(actions);
    }

    private List<Long> collectProductIds(String token)  {
        List<Long> productIdList = new ArrayList<>();

        IntStream.rangeClosed(1, 10).forEach(i -> {
            ProductResponseDto responseDto = null;
            try {
                responseDto = createProjects(token);
            } catch (Exception e) {
                e.printStackTrace();
            }
            productIdList.add(Objects.requireNonNull(responseDto).getId());
        });

        return productIdList;
    }

    private ProductResponseDto createProjects(String token) throws Exception {
        ResultActions actions = this.mockMvc.perform(post(PRODUCT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createProductRequestDto()))
                .header(HttpHeaders.AUTHORIZATION, token))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("name").value(appProperties.getTestProductName()))
                .andExpect(jsonPath("image").value(appProperties.getTestImage()))
                .andExpect(jsonPath("size").value(appProperties.getTestSize()))
                .andExpect(jsonPath("price").value(appProperties.getTestPrice()))
                .andExpect(jsonPath("stock").value(appProperties.getTestStock()))
                .andExpect(jsonPath("category").value(ProductCategory.ACCESSORY.name()))
                .andExpect(jsonPath("registeredAt").exists())
                .andExpect(jsonPath("seller").exists())
        ;

        String productAsString = actions.andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(productAsString, ProductResponseDto.class);
    }
}
