package com.kiseok.pingmall.web.controller;

import com.kiseok.pingmall.common.domain.product.ProductCategory;
import com.kiseok.pingmall.web.common.BaseControllerTest;
import com.kiseok.pingmall.web.dto.product.ProductRequestDto;
import com.kiseok.pingmall.web.dto.product.ProductResponseDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import java.util.stream.Stream;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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

    @DisplayName("제품 등록 시 유효성 실패 -> 400 BAD_REQUEST")
    @ParameterizedTest(name = "{index} {displayName} message={0}")
    @MethodSource("validSaveProduct")
    void save_product_invalid_400(String name, String size, ProductCategory category) throws Exception  {
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
                .name(name)
                .size(size)
                .category(category)
                .build();

        this.mockMvc.perform(post(PRODUCT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .header(HttpHeaders.AUTHORIZATION, token))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("[*].code").exists())
                .andExpect(jsonPath("[*].defaultMessage").exists())
                .andExpect(jsonPath("[*].rejectedValue").exists())
                .andExpect(jsonPath("[*].field").exists())
                .andExpect(jsonPath("[*].objectName").exists())
        ;
    }

    @DisplayName("제품 등록 시 유저가 null -> 400 BAD_REQUEST")
    @Test
    void save_product_user_null_400() throws Exception  {
        ProductRequestDto requestDto = ProductRequestDto.builder()
                .name("롤렉스 서브마리너")
                .size("XL")
                .category(ProductCategory.ACCESSORY)
                .build();

        this.mockMvc.perform(post(PRODUCT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
        ;
    }

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

    @DisplayName("DB에 없는 제품 불러올 시 -> 404 NOT_FOUND")
    @Test
    void load_product_not_found_404() throws Exception  {
        this.mockMvc.perform(get(PRODUCT_URL + "-1")
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @DisplayName("정상적으로 제품 불러오기 -> 200 OK")
    @Test
    void load_product_200() throws Exception    {
        ResultActions actions = this.mockMvc.perform(post(ACCOUNT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createAccountRequestDto())))
                .andDo(print())
                .andExpect(status().isCreated())
        ;

        String token = generateToken(actions);
        ProductRequestDto requestDto = ProductRequestDto.builder()
                .name("롤렉스 서브마리너")
                .size("XL")
                .category(ProductCategory.ACCESSORY)
                .build();

        ResultActions actions2 = this.mockMvc.perform(post(PRODUCT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .header(HttpHeaders.AUTHORIZATION, token))
                .andDo(print())
                .andExpect(status().isCreated())
        ;

        String contentAsString = actions2.andReturn().getResponse().getContentAsString();
        ProductResponseDto responseDto = objectMapper.readValue(contentAsString, ProductResponseDto.class);

        this.mockMvc.perform(get(PRODUCT_URL + responseDto.getId())
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("name").value("롤렉스 서브마리너"))
                .andExpect(jsonPath("size").value("XL"))
                .andExpect(jsonPath("category").value(ProductCategory.ACCESSORY.name()))
                .andExpect(jsonPath("registeredAt").exists())
//                .andExpect(jsonPath("seller").exists())
        ;
    }

    private static Stream<Arguments> validSaveProduct() {
        return Stream.of(
                Arguments.of("", "265", ProductCategory.TOP, true),
                Arguments.of(" ", "265", ProductCategory.BOTTOMS, true),
                Arguments.of("testName", "", ProductCategory.SHOES, true),
                Arguments.of("testName", " ", ProductCategory.ACCESSORY, true)
        );
    }
}