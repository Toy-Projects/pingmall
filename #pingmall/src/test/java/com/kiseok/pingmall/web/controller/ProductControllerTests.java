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
    @MethodSource("validProduct")
    void save_product_invalid_400(String name, String size, Long price, Long stock, ProductCategory category) throws Exception  {
        String token = createAccountAndToken();

        ProductRequestDto requestDto = ProductRequestDto.builder()
                .name(name)
                .size(size)
                .image(appProperties.getTestImage())
                .price(price)
                .stock(stock)
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
                .andExpect(jsonPath("[*].field").exists())
                .andExpect(jsonPath("[*].objectName").exists())
        ;
    }

    @DisplayName("제품 등록 시 유저가 null -> 400 BAD_REQUEST")
    @Test
    void save_product_user_null_400() throws Exception  {
        ProductRequestDto requestDto = createProductRequestDto();

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
        String token = createAccountAndToken();
        ProductRequestDto requestDto = createProductRequestDto();

        this.mockMvc.perform(post(PRODUCT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .header(HttpHeaders.AUTHORIZATION, token))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("name").value(appProperties.getTestProductName()))
                .andExpect(jsonPath("size").value(appProperties.getTestSize()))
                .andExpect(jsonPath("price").value(appProperties.getTestPrice()))
                .andExpect(jsonPath("stock").value(appProperties.getTestStock()))
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
        String token = createAccountAndToken();
        ProductRequestDto requestDto = createProductRequestDto();

        ResultActions actions = this.mockMvc.perform(post(PRODUCT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .header(HttpHeaders.AUTHORIZATION, token))
                .andDo(print())
                .andExpect(status().isCreated())
        ;

        String contentAsString = actions.andReturn().getResponse().getContentAsString();
        ProductResponseDto responseDto = objectMapper.readValue(contentAsString, ProductResponseDto.class);

        this.mockMvc.perform(get(PRODUCT_URL + responseDto.getId())
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("name").value(appProperties.getTestProductName()))
                .andExpect(jsonPath("size").value(appProperties.getTestSize()))
                .andExpect(jsonPath("price").value(appProperties.getTestPrice()))
                .andExpect(jsonPath("stock").value(appProperties.getTestStock()))
                .andExpect(jsonPath("category").value(ProductCategory.ACCESSORY.name()))
                .andExpect(jsonPath("registeredAt").exists())
        ;
    }

    @DisplayName("제품 수정 시 유효성 검사 실패 -> 400 BAD_REQUEST")
    @ParameterizedTest(name = "{index} {displayName} message={0}")
    @MethodSource("validProduct")
    void modify_product_invalid_400(String name, String size, Long price, Long stock, ProductCategory category) throws Exception {
        String token = createAccountAndToken();
        ProductRequestDto requestDto = createProductRequestDto();

        ResultActions actions = this.mockMvc.perform(post(PRODUCT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .header(HttpHeaders.AUTHORIZATION, token))
                .andDo(print())
                .andExpect(status().isCreated())
        ;

        String contentAsString = actions.andReturn().getResponse().getContentAsString();
        ProductResponseDto responseDto = objectMapper.readValue(contentAsString, ProductResponseDto.class);

        requestDto = ProductRequestDto.builder()
                .name(name)
                .size(size)
                .image(appProperties.getTestImage())
                .price(price)
                .stock(stock)
                .category(category)
                .build();

        this.mockMvc.perform(put(PRODUCT_URL + responseDto.getId())
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .header(HttpHeaders.AUTHORIZATION, token))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("[*].code").exists())
                .andExpect(jsonPath("[*].defaultMessage").exists())
                .andExpect(jsonPath("[*].field").exists())
                .andExpect(jsonPath("[*].objectName").exists())
        ;
    }

    @DisplayName("DB에 없는 제품 수정 시 -> 404 NOT_FOUND")
    @Test
    void modify_product_id_null() throws Exception  {
        String token = createAccountAndToken();
        ProductRequestDto requestDto = createProductRequestDto();

        this.mockMvc.perform(post(PRODUCT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .header(HttpHeaders.AUTHORIZATION, token))
                .andDo(print())
                .andExpect(status().isCreated())
        ;

        requestDto = createProductModifyRequestDto();

        this.mockMvc.perform(put(PRODUCT_URL + "-1")
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .header(HttpHeaders.AUTHORIZATION, token))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @DisplayName("제품 수정한 유저 ID와 제품 Seller의 ID가 다를 시 -> 400 BAD_REQUEST")
    @Test
    void modify_product_accountId_not_match_400() throws Exception {
        String token = createAccountAndToken();
        ProductRequestDto requestDto = createProductRequestDto();

        ResultActions actions = this.mockMvc.perform(post(PRODUCT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .header(HttpHeaders.AUTHORIZATION, token))
                .andDo(print())
                .andExpect(status().isCreated())
        ;

        String contentAsString = actions.andReturn().getResponse().getContentAsString();
        ProductResponseDto responseDto = objectMapper.readValue(contentAsString, ProductResponseDto.class);
        String anotherToken = createAnotherAccountAndToken();
        requestDto = createProductModifyRequestDto();

        this.mockMvc.perform(put(PRODUCT_URL + responseDto.getId())
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .header(HttpHeaders.AUTHORIZATION, anotherToken))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @DisplayName("정상적으로 제품 수정 -> 200 OK")
    @Test
    void modify_product_200() throws Exception  {
        String token = createAccountAndToken();
        ProductRequestDto requestDto = createProductRequestDto();

        ResultActions actions = this.mockMvc.perform(post(PRODUCT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .header(HttpHeaders.AUTHORIZATION, token))
                .andDo(print())
                .andExpect(status().isCreated())
        ;

        String contentAsString = actions.andReturn().getResponse().getContentAsString();
        ProductResponseDto responseDto = objectMapper.readValue(contentAsString, ProductResponseDto.class);
        requestDto = createProductModifyRequestDto();

        this.mockMvc.perform(put(PRODUCT_URL + responseDto.getId())
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .header(HttpHeaders.AUTHORIZATION, token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("name").value(appProperties.getTestModifiedProductName()))
                .andExpect(jsonPath("size").value(appProperties.getTestModifiedSize()))
                .andExpect(jsonPath("price").value(appProperties.getTestModifiedPrice()))
                .andExpect(jsonPath("stock").value(appProperties.getTestModifiedStock()))
                .andExpect(jsonPath("category").value(ProductCategory.TOP.name()))
        ;
    }

    private String createAccountAndToken() throws Exception {
        ResultActions actions = this.mockMvc.perform(post(ACCOUNT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createAccountRequestDto())))
                .andDo(print())
                .andExpect(status().isCreated());

        return generateToken(actions);
    }

    private String createAnotherAccountAndToken() throws Exception {
        ResultActions actions = this.mockMvc.perform(post(ACCOUNT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createAnotherAccountRequestDto())))
                .andDo(print())
                .andExpect(status().isCreated());

        return generateToken(actions);
    }

    private static Stream<Arguments> validProduct() {
        return Stream.of(
                Arguments.of("", "265", 100L, 1L, ProductCategory.TOP, true),
                Arguments.of(" ", "265", 100L, 1L, ProductCategory.BOTTOMS, true),
                Arguments.of("testName", "", 100L, 1L, ProductCategory.SHOES, true),
                Arguments.of("testName", " ", 100L, 1L, ProductCategory.ACCESSORY, true),
                Arguments.of("testName", "265", 0L, 1L, ProductCategory.ACCESSORY, true),
                Arguments.of("testName", "265", null, 1L, ProductCategory.ACCESSORY, true),
                Arguments.of("testName", "265", 100L, null, ProductCategory.ACCESSORY, true)
        );
    }
}