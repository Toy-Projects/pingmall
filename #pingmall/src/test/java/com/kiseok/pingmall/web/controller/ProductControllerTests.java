package com.kiseok.pingmall.web.controller;

import com.kiseok.pingmall.common.domain.account.AccountRole;
import com.kiseok.pingmall.common.domain.product.ProductCategory;
import com.kiseok.pingmall.web.BaseControllerTests;
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

import java.util.stream.IntStream;
import java.util.stream.Stream;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductControllerTests extends BaseControllerTests {

    @AfterEach
    void tearDown()    {
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
                .andExpect(jsonPath("status").exists())
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("code").exists())
                .andExpect(jsonPath("erroredAt").exists())
                .andExpect(jsonPath("errors.[*].field").exists())
                .andExpect(jsonPath("errors.[*].value").exists())
                .andExpect(jsonPath("errors.[*].reason").exists())
        ;
    }

    @DisplayName("제품 등록 시 유저가 null -> 401 UNAUTHORIZED")
    @Test
    void save_product_user_null_401() throws Exception  {
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
                .andExpect(jsonPath("image").value(appProperties.getTestImage()))
                .andExpect(jsonPath("size").value(appProperties.getTestSize()))
                .andExpect(jsonPath("price").value(appProperties.getTestPrice()))
                .andExpect(jsonPath("stock").value(appProperties.getTestStock()))
                .andExpect(jsonPath("category").value(ProductCategory.ACCESSORY.name()))
                .andExpect(jsonPath("registeredAt").exists())
                .andExpect(jsonPath("seller").exists())
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
                .andExpect(jsonPath("status").exists())
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("code").exists())
                .andExpect(jsonPath("erroredAt").exists())
                .andExpect(jsonPath("errors").exists())
        ;
    }

    @DisplayName("정상적으로 모든 제품 불러오기 -> 200 OK")
    @Test
    void load_all_products_200() throws Exception   {
        String token = createAccountAndToken();

        IntStream.rangeClosed(1, 10).forEach(i -> {
            ProductRequestDto requestDto = createProductRequestDto();

            try {
                this.mockMvc.perform(post(PRODUCT_URL)
                        .accept(MediaTypes.HAL_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        this.mockMvc.perform(get(PRODUCT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
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

        String contentAsString = actions.andReturn().getResponse().getContentAsString();
        ProductResponseDto responseDto = objectMapper.readValue(contentAsString, ProductResponseDto.class);

        this.mockMvc.perform(get(PRODUCT_URL + responseDto.getId())
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("name").value(responseDto.getName()))
                .andExpect(jsonPath("image").value(responseDto.getImage()))
                .andExpect(jsonPath("size").value(responseDto.getSize()))
                .andExpect(jsonPath("price").value(responseDto.getPrice()))
                .andExpect(jsonPath("stock").value(responseDto.getStock()))
                .andExpect(jsonPath("category").value(responseDto.getCategory().name()))
                .andExpect(jsonPath("registeredAt").exists())
                .andExpect(jsonPath("seller").exists())
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
                .andExpect(jsonPath("status").exists())
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("code").exists())
                .andExpect(jsonPath("erroredAt").exists())
                .andExpect(jsonPath("errors.[*].field").exists())
                .andExpect(jsonPath("errors.[*].value").exists())
                .andExpect(jsonPath("errors.[*].reason").exists())
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

        requestDto = createProductModifyRequestDto();

        this.mockMvc.perform(put(PRODUCT_URL + "-1")
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .header(HttpHeaders.AUTHORIZATION, token))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("status").exists())
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("code").exists())
                .andExpect(jsonPath("erroredAt").exists())
                .andExpect(jsonPath("errors").exists())
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
                .andExpect(status().isCreated()).andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("name").value(appProperties.getTestProductName()))
                .andExpect(jsonPath("image").value(appProperties.getTestImage()))
                .andExpect(jsonPath("size").value(appProperties.getTestSize()))
                .andExpect(jsonPath("price").value(appProperties.getTestPrice()))
                .andExpect(jsonPath("stock").value(appProperties.getTestStock()))
                .andExpect(jsonPath("category").value(ProductCategory.ACCESSORY.name()))
                .andExpect(jsonPath("registeredAt").exists())
                .andExpect(jsonPath("seller").exists())
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
                .andExpect(jsonPath("status").exists())
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("code").exists())
                .andExpect(jsonPath("erroredAt").exists())
                .andExpect(jsonPath("errors").exists())
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
                .andExpect(jsonPath("image").value(appProperties.getTestModifiedImage()))
                .andExpect(jsonPath("size").value(appProperties.getTestModifiedSize()))
                .andExpect(jsonPath("price").value(appProperties.getTestModifiedPrice()))
                .andExpect(jsonPath("stock").value(appProperties.getTestModifiedStock()))
                .andExpect(jsonPath("category").value(ProductCategory.TOP.name()))
                .andExpect(jsonPath("registeredAt").exists())
                .andExpect(jsonPath("seller").exists())
        ;
    }

    @DisplayName("DB에 없는 제품 삭제시 -> 404 NOT_FOUND")
    @Test
    void delete_product_id_null_404() throws Exception  {
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
                .andExpect(jsonPath("image").value(appProperties.getTestImage()))
                .andExpect(jsonPath("size").value(appProperties.getTestSize()))
                .andExpect(jsonPath("price").value(appProperties.getTestPrice()))
                .andExpect(jsonPath("stock").value(appProperties.getTestStock()))
                .andExpect(jsonPath("category").value(ProductCategory.ACCESSORY.name()))
                .andExpect(jsonPath("registeredAt").exists())
                .andExpect(jsonPath("seller").exists())
        ;

        this.mockMvc.perform(delete(PRODUCT_URL + "-1")
                .header(HttpHeaders.AUTHORIZATION, token))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("status").exists())
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("code").exists())
                .andExpect(jsonPath("erroredAt").exists())
                .andExpect(jsonPath("errors").exists())
        ;
    }

    @DisplayName("제품 삭제한 유저 ID와 제품 Seller의 ID가 다를 시 -> 400 BAD_REQUEST")
    @Test
    void delete_product_accountId_not_match_400() throws Exception  {
        String token = createAccountAndToken();
        ProductRequestDto requestDto = createProductRequestDto();

        ResultActions actions = this.mockMvc.perform(post(PRODUCT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
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

        String contentAsString = actions.andReturn().getResponse().getContentAsString();
        ProductResponseDto responseDto = objectMapper.readValue(contentAsString, ProductResponseDto.class);
        String anotherToken = createAnotherAccountAndToken();

        this.mockMvc.perform(delete(PRODUCT_URL + responseDto.getId())
                .header(HttpHeaders.AUTHORIZATION, anotherToken))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").exists())
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("code").exists())
                .andExpect(jsonPath("erroredAt").exists())
                .andExpect(jsonPath("errors").exists())
        ;
    }

    @DisplayName("정상적으로 제품 삭제 -> 200 OK")
    @Test
    void delete_product_200() throws Exception  {
        String token = createAccountAndToken();
        ProductRequestDto requestDto = createProductRequestDto();

        ResultActions actions = this.mockMvc.perform(post(PRODUCT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
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

        String contentAsString = actions.andReturn().getResponse().getContentAsString();
        ProductResponseDto responseDto = objectMapper.readValue(contentAsString, ProductResponseDto.class);

        this.mockMvc.perform(delete(PRODUCT_URL + responseDto.getId())
                .header(HttpHeaders.AUTHORIZATION, token))
                .andDo(print())
                .andExpect(status().isOk())
        ;
    }

    @DisplayName("유저 삭제시 판매 제품과 함께 삭제 -> 200 OK")
    @Test
    void delete_account_cascade_product_200() throws Exception    {
        String token = createAccountAndToken();
        ProductRequestDto requestDto = createProductRequestDto();

        ResultActions actions = this.mockMvc.perform(post(PRODUCT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
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

        String contentAsString = actions.andReturn().getResponse().getContentAsString();
        ProductResponseDto responseDto = objectMapper.readValue(contentAsString, ProductResponseDto.class);
        Long accountId =  responseDto.getSeller().getId();

        this.mockMvc.perform(delete(ACCOUNT_URL + accountId)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, token))
                .andDo(print())
                .andExpect(status().isOk())
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