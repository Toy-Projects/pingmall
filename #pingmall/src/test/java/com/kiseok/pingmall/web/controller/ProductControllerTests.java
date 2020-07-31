package com.kiseok.pingmall.web.controller;

import com.kiseok.pingmall.common.domain.account.AccountRole;
import com.kiseok.pingmall.common.domain.product.ProductCategory;
import com.kiseok.pingmall.common.domain.verification.Verification;
import com.kiseok.pingmall.web.common.BaseControllerTests;
import com.kiseok.pingmall.web.dto.product.ProductRequestDto;
import com.kiseok.pingmall.web.dto.product.ProductResponseDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import static com.kiseok.pingmall.common.resources.RestDocsResource.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductControllerTests extends BaseControllerTests {

    @BeforeEach
    void setUp() {
        Verification verification = Verification.builder()
                .email(appProperties.getTestEmail())
                .verificationCode(UUID.randomUUID().toString().substring(0, 6))
                .isVerified(true)
                .build();

        Verification anotherVerification = Verification.builder()
                .email(ANOTHER + appProperties.getTestEmail())
                .verificationCode(UUID.randomUUID().toString().substring(0, 6))
                .isVerified(true)
                .build();

        verificationRepository.save(verification);
        verificationRepository.save(anotherVerification);
    }

    @AfterEach
    void tearDown()    {
        this.commentRepository.deleteAll();
        this.productRepository.deleteAll();
        this.accountRepository.deleteAll();
        this.verificationRepository.deleteAll();
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
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.load-index").exists())
                .andExpect(jsonPath("_links.profile").exists())
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
                .andExpect(jsonPath("seller").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.load-all-products").exists())
                .andExpect(jsonPath("_links.load-product").exists())
                .andExpect(jsonPath("_links.create-product-image").exists())
                .andExpect(jsonPath("_links.modify-product").exists())
                .andExpect(jsonPath("_links.delete-product").exists())
                .andExpect(jsonPath("_links.create-orders").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document(CREATE_PRODUCT.getRel(),
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel(PROFILE.getRel()).description("link to profile"),
                                linkWithRel(LOAD_ALL_PRODUCT.getRel()).description("link to load all products"),
                                linkWithRel(LOAD_PRODUCT.getRel()).description("link to load product"),
                                linkWithRel(CREATE_PRODUCT_IMAGE.getRel()).description("link to create product image"),
                                linkWithRel(MODIFY_PRODUCT.getRel()).description("link to modify product"),
                                linkWithRel(DELETE_PRODUCT.getRel()).description("link to delete product"),
                                linkWithRel(CREATE_ORDERS.getRel()).description("link to create orders")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header"),
                                headerWithName(HttpHeaders.AUTHORIZATION).description("authorization header")
                        ),
                        requestFields(
                                fieldWithPath("name").description("Name of new Product"),
                                fieldWithPath("size").description("Size of new Product"),
                                fieldWithPath("image").description("Image of new Product"),
                                fieldWithPath("price").description("Price of new Product"),
                                fieldWithPath("stock").description("Stock of new Product"),
                                fieldWithPath("category").description("Category of new Product")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.LOCATION).description("location header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        responseFields(
                                fieldWithPath("id").description("Identifier of new Product"),
                                fieldWithPath("name").description("Name of new Product"),
                                fieldWithPath("size").description("Size of new Product"),
                                fieldWithPath("image").description("Image of new Product"),
                                fieldWithPath("price").description("Price of new Product"),
                                fieldWithPath("stock").description("Stock of new Product"),
                                fieldWithPath("category").description("Category of new Product"),
                                fieldWithPath("seller.id").description("Identifier of Seller"),
                                fieldWithPath("seller.createdAt").description("Created Date of Seller"),
                                fieldWithPath("seller.modifiedAt").description("Modified Date of Seller"),
                                fieldWithPath("seller.email").description("E-Mail of Seller"),
                                fieldWithPath("seller.name").description("Name of Seller"),
                                fieldWithPath("seller.address").description("Address of Seller"),
                                fieldWithPath("seller.balance").description("Balance of Seller"),
                                fieldWithPath("seller.accountRole").description("Role of Seller"),
                                fieldWithPath("buyer").description("Buyer"),
                                fieldWithPath("_links.self.href").description("link to self"),
                                fieldWithPath("_links.profile.href").description("link to profile"),
                                fieldWithPath("_links.load-all-products.href").description("link to load all products"),
                                fieldWithPath("_links.load-product.href").description("link to load product"),
                                fieldWithPath("_links.create-product-image.href").description("link to create product image"),
                                fieldWithPath("_links.modify-product.href").description("link to modify product"),
                                fieldWithPath("_links.delete-product.href").description("link to delete product"),
                                fieldWithPath("_links.create-orders.href").description("link to create orders")
                        )
                ))
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
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.load-index").exists())
                .andExpect(jsonPath("_links.profile").exists())
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
                        .andExpect(jsonPath("seller").exists())
                        .andExpect(jsonPath("_links.self").exists())
                        .andExpect(jsonPath("_links.load-all-products").exists())
                        .andExpect(jsonPath("_links.load-product").exists())
                        .andExpect(jsonPath("_links.create-product-image").exists())
                        .andExpect(jsonPath("_links.modify-product").exists())
                        .andExpect(jsonPath("_links.delete-product").exists())
                        .andExpect(jsonPath("_links.create-orders").exists())
                        .andExpect(jsonPath("_links.profile").exists())
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
                .andExpect(jsonPath("_embedded.productResponseDtoList.[*]._links.self").exists())
                .andExpect(jsonPath("_embedded.productResponseDtoList.[*]._links.create-product-image").exists())
                .andExpect(jsonPath("_embedded.productResponseDtoList.[*]._links.modify-product").exists())
                .andExpect(jsonPath("_embedded.productResponseDtoList.[*]._links.delete-product").exists())
                .andExpect(jsonPath("_embedded.productResponseDtoList.[*]._links.create-orders").exists())
                .andExpect(jsonPath("_embedded.productResponseDtoList.[*]._links.profile").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.create-product").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("page.size").exists())
                .andExpect(jsonPath("page.totalElements").exists())
                .andExpect(jsonPath("page.totalPages").exists())
                .andExpect(jsonPath("page.number").exists())
                .andDo(document(LOAD_ALL_PRODUCT.getRel(),
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel(PROFILE.getRel()).description("link to profile"),
                                linkWithRel(CREATE_PRODUCT.getRel()).description("link to create product")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        responseFields(
                                fieldWithPath("_embedded.productResponseDtoList.[*].id").description("Identifier of Product"),
                                fieldWithPath("_embedded.productResponseDtoList.[*].name").description("Name of Product"),
                                fieldWithPath("_embedded.productResponseDtoList.[*].size").description("Size of Product"),
                                fieldWithPath("_embedded.productResponseDtoList.[*].image").description("Image of Product"),
                                fieldWithPath("_embedded.productResponseDtoList.[*].price").description("Price of Product"),
                                fieldWithPath("_embedded.productResponseDtoList.[*].stock").description("Stock of Product"),
                                fieldWithPath("_embedded.productResponseDtoList.[*].category").description("Category of Product"),
                                fieldWithPath("_embedded.productResponseDtoList.[*].seller").description("Seller"),
                                fieldWithPath("_embedded.productResponseDtoList.[*].seller.id").description("Identifier of Seller"),
                                fieldWithPath("_embedded.productResponseDtoList.[*].seller.createdAt").description("Created Date of Seller"),
                                fieldWithPath("_embedded.productResponseDtoList.[*].seller.modifiedAt").description("Modified Date of Seller"),
                                fieldWithPath("_embedded.productResponseDtoList.[*].seller.email").description("E-Mail of Seller"),
                                fieldWithPath("_embedded.productResponseDtoList.[*].seller.name").description("Name of Seller"),
                                fieldWithPath("_embedded.productResponseDtoList.[*].seller.address").description("Address of Seller"),
                                fieldWithPath("_embedded.productResponseDtoList.[*].seller.balance").description("Balance of Seller"),
                                fieldWithPath("_embedded.productResponseDtoList.[*].seller.accountRole").description("Role of Seller"),
                                fieldWithPath("_embedded.productResponseDtoList.[*].buyer").description("Buyer"),
                                fieldWithPath("_embedded.productResponseDtoList.[*]._links.self.href").description("link to self"),
                                fieldWithPath("_embedded.productResponseDtoList.[*]._links.profile.href").description("link to profile"),
                                fieldWithPath("_embedded.productResponseDtoList.[*]._links.create-product.href").description("link to create product"),
                                fieldWithPath("_embedded.productResponseDtoList.[*]._links.create-product-image.href").description("link to create product image"),
                                fieldWithPath("_embedded.productResponseDtoList.[*]._links.modify-product.href").description("link to modify product"),
                                fieldWithPath("_embedded.productResponseDtoList.[*]._links.delete-product.href").description("link to delete product"),
                                fieldWithPath("_embedded.productResponseDtoList.[*]._links.create-orders.href").description("link to create orders"),
                                fieldWithPath("_links.self.href").description("link to self"),
                                fieldWithPath("_links.profile.href").description("link to profile"),
                                fieldWithPath("_links.create-product.href").description("link to create product"),
                                fieldWithPath("page.size").description("size of page"),
                                fieldWithPath("page.totalElements").description("total elements of page"),
                                fieldWithPath("page.totalPages").description("total number of page"),
                                fieldWithPath("page.number").description("number of page")
                        )
                ))
        ;
    }

    @DisplayName("정상적으로 필터링 된 모든 제품 불러오기 -> 200 OK")
    @Test
    void load_all_filtered_products_200() throws Exception   {
        saveCustomProduct(createAccountAndToken());

        this.mockMvc.perform(get(PRODUCT_URL)
//                .queryParam("name", "Adidas")
//                .queryParam("size", "L")
//                .queryParam("price", "80000")
//                .queryParam("category", ProductCategory.BOTTOMS.name())
//                .queryParam("orderBy", "price")
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.productResponseDtoList.[*]._links.self").exists())
                .andExpect(jsonPath("_embedded.productResponseDtoList.[*]._links.create-product-image").exists())
                .andExpect(jsonPath("_embedded.productResponseDtoList.[*]._links.modify-product").exists())
                .andExpect(jsonPath("_embedded.productResponseDtoList.[*]._links.delete-product").exists())
                .andExpect(jsonPath("_embedded.productResponseDtoList.[*]._links.create-orders").exists())
                .andExpect(jsonPath("_embedded.productResponseDtoList.[*]._links.profile").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.create-product").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("page.size").exists())
                .andExpect(jsonPath("page.totalElements").exists())
                .andExpect(jsonPath("page.totalPages").exists())
                .andExpect(jsonPath("page.number").exists())
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
                .andExpect(jsonPath("seller").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.load-all-products").exists())
                .andExpect(jsonPath("_links.load-product").exists())
                .andExpect(jsonPath("_links.create-product-image").exists())
                .andExpect(jsonPath("_links.modify-product").exists())
                .andExpect(jsonPath("_links.delete-product").exists())
                .andExpect(jsonPath("_links.create-orders").exists())
                .andExpect(jsonPath("_links.profile").exists())
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
                .andExpect(jsonPath("seller").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.load-all-products").exists())
                .andExpect(jsonPath("_links.create-product").exists())
                .andExpect(jsonPath("_links.create-product-image").exists())
                .andExpect(jsonPath("_links.modify-product").exists())
                .andExpect(jsonPath("_links.delete-product").exists())
                .andExpect(jsonPath("_links.create-orders").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document(LOAD_PRODUCT.getRel(),
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel(PROFILE.getRel()).description("link to profile"),
                                linkWithRel(LOAD_ALL_PRODUCT.getRel()).description("link to load all products"),
                                linkWithRel(CREATE_PRODUCT.getRel()).description("link to create product"),
                                linkWithRel(CREATE_PRODUCT_IMAGE.getRel()).description("link to create product image"),
                                linkWithRel(MODIFY_PRODUCT.getRel()).description("link to modify product"),
                                linkWithRel(DELETE_PRODUCT.getRel()).description("link to delete product"),
                                linkWithRel(CREATE_ORDERS.getRel()).description("link to create orders")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        responseFields(
                                fieldWithPath("id").description("Identifier of new Product"),
                                fieldWithPath("name").description("Name of new Product"),
                                fieldWithPath("size").description("Size of new Product"),
                                fieldWithPath("image").description("Image of new Product"),
                                fieldWithPath("price").description("Price of new Product"),
                                fieldWithPath("stock").description("Stock of new Product"),
                                fieldWithPath("category").description("Category of new Product"),
                                fieldWithPath("seller.id").description("Identifier of Seller"),
                                fieldWithPath("seller.createdAt").description("Created Date of Seller"),
                                fieldWithPath("seller.modifiedAt").description("Modified Date of Seller"),
                                fieldWithPath("seller.email").description("E-Mail of Seller"),
                                fieldWithPath("seller.name").description("Name of Seller"),
                                fieldWithPath("seller.address").description("Address of Seller"),
                                fieldWithPath("seller.balance").description("Balance of Seller"),
                                fieldWithPath("seller.accountRole").description("Role of Seller"),
                                fieldWithPath("buyer").description("Buyer"),
                                fieldWithPath("_links.self.href").description("link to self"),
                                fieldWithPath("_links.profile.href").description("link to profile"),
                                fieldWithPath("_links.load-all-products.href").description("link to load all products"),
                                fieldWithPath("_links.create-product.href").description("link to create product"),
                                fieldWithPath("_links.create-product-image.href").description("link to create product image"),
                                fieldWithPath("_links.modify-product.href").description("link to modify product"),
                                fieldWithPath("_links.delete-product.href").description("link to delete product"),
                                fieldWithPath("_links.create-orders.href").description("link to create orders")
                        )
                ))
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
                .andExpect(jsonPath("seller").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.load-all-products").exists())
                .andExpect(jsonPath("_links.load-product").exists())
                .andExpect(jsonPath("_links.create-product-image").exists())
                .andExpect(jsonPath("_links.modify-product").exists())
                .andExpect(jsonPath("_links.delete-product").exists())
                .andExpect(jsonPath("_links.create-orders").exists())
                .andExpect(jsonPath("_links.profile").exists())
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
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.load-index").exists())
                .andExpect(jsonPath("_links.profile").exists())
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
                .andExpect(jsonPath("seller").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.load-all-products").exists())
                .andExpect(jsonPath("_links.load-product").exists())
                .andExpect(jsonPath("_links.create-product-image").exists())
                .andExpect(jsonPath("_links.modify-product").exists())
                .andExpect(jsonPath("_links.delete-product").exists())
                .andExpect(jsonPath("_links.create-orders").exists())
                .andExpect(jsonPath("_links.profile").exists())
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
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.load-index").exists())
                .andExpect(jsonPath("_links.profile").exists())
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
                .andExpect(jsonPath("seller").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.load-all-products").exists())
                .andExpect(jsonPath("_links.load-product").exists())
                .andExpect(jsonPath("_links.create-product-image").exists())
                .andExpect(jsonPath("_links.modify-product").exists())
                .andExpect(jsonPath("_links.delete-product").exists())
                .andExpect(jsonPath("_links.create-orders").exists())
                .andExpect(jsonPath("_links.profile").exists())
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
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.load-index").exists())
                .andExpect(jsonPath("_links.profile").exists())
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
                .andExpect(jsonPath("seller").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.load-all-products").exists())
                .andExpect(jsonPath("_links.load-product").exists())
                .andExpect(jsonPath("_links.create-product-image").exists())
                .andExpect(jsonPath("_links.modify-product").exists())
                .andExpect(jsonPath("_links.delete-product").exists())
                .andExpect(jsonPath("_links.create-orders").exists())
                .andExpect(jsonPath("_links.profile").exists())
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
                .andExpect(jsonPath("seller").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.load-all-products").exists())
                .andExpect(jsonPath("_links.load-product").exists())
                .andExpect(jsonPath("_links.create-product").exists())
                .andExpect(jsonPath("_links.create-product-image").exists())
                .andExpect(jsonPath("_links.delete-product").exists())
                .andExpect(jsonPath("_links.create-orders").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document(MODIFY_PRODUCT.getRel(),
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel(PROFILE.getRel()).description("link to profile"),
                                linkWithRel(LOAD_ALL_PRODUCT.getRel()).description("link to load all products"),
                                linkWithRel(LOAD_PRODUCT.getRel()).description("link to load product"),
                                linkWithRel(CREATE_PRODUCT.getRel()).description("link to create product"),
                                linkWithRel(CREATE_PRODUCT_IMAGE.getRel()).description("link to create product image"),
                                linkWithRel(DELETE_PRODUCT.getRel()).description("link to delete product"),
                                linkWithRel(CREATE_ORDERS.getRel()).description("link to create orders")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header"),
                                headerWithName(HttpHeaders.AUTHORIZATION).description("authorization header")
                        ),
                        requestFields(
                                fieldWithPath("name").description("Name of Modified Product"),
                                fieldWithPath("size").description("Size of Modified Product"),
                                fieldWithPath("image").description("Image of Modified Product"),
                                fieldWithPath("price").description("Price of Modified Product"),
                                fieldWithPath("stock").description("Stock of Modified Product"),
                                fieldWithPath("category").description("Category of Modified Product")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        responseFields(
                                fieldWithPath("id").description("Identifier of Modified Product"),
                                fieldWithPath("name").description("Name of Modified Product"),
                                fieldWithPath("size").description("Size of Modified Product"),
                                fieldWithPath("image").description("Image of Modified Product"),
                                fieldWithPath("price").description("Price of Modified Product"),
                                fieldWithPath("stock").description("Stock of Modified Product"),
                                fieldWithPath("category").description("Category of Modified Product"),
                                fieldWithPath("seller.id").description("Identifier of Seller"),
                                fieldWithPath("seller.createdAt").description("Created Date of Seller"),
                                fieldWithPath("seller.modifiedAt").description("Modified Date of Seller"),
                                fieldWithPath("seller.email").description("E-Mail of Seller"),
                                fieldWithPath("seller.name").description("Name of Seller"),
                                fieldWithPath("seller.address").description("Address of Seller"),
                                fieldWithPath("seller.balance").description("Balance of Seller"),
                                fieldWithPath("seller.accountRole").description("Role of Seller"),
                                fieldWithPath("buyer").description("Buyer"),
                                fieldWithPath("_links.self.href").description("link to self"),
                                fieldWithPath("_links.profile.href").description("link to profile"),
                                fieldWithPath("_links.load-all-products.href").description("link to load all products"),
                                fieldWithPath("_links.load-product.href").description("link to load product"),
                                fieldWithPath("_links.create-product.href").description("link to create product"),
                                fieldWithPath("_links.create-product-image.href").description("link to create product image"),
                                fieldWithPath("_links.create-product.href").description("link to create product"),
                                fieldWithPath("_links.delete-product.href").description("link to delete product"),
                                fieldWithPath("_links.create-orders.href").description("link to create orders")
                        )
                ))
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
                .andExpect(jsonPath("seller").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.load-all-products").exists())
                .andExpect(jsonPath("_links.load-product").exists())
                .andExpect(jsonPath("_links.create-product-image").exists())
                .andExpect(jsonPath("_links.modify-product").exists())
                .andExpect(jsonPath("_links.delete-product").exists())
                .andExpect(jsonPath("_links.create-orders").exists())
                .andExpect(jsonPath("_links.profile").exists())
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
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.load-index").exists())
                .andExpect(jsonPath("_links.profile").exists())
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
                .andExpect(jsonPath("seller").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.load-all-products").exists())
                .andExpect(jsonPath("_links.load-product").exists())
                .andExpect(jsonPath("_links.create-product-image").exists())
                .andExpect(jsonPath("_links.modify-product").exists())
                .andExpect(jsonPath("_links.delete-product").exists())
                .andExpect(jsonPath("_links.create-orders").exists())
                .andExpect(jsonPath("_links.profile").exists())
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
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.load-index").exists())
                .andExpect(jsonPath("_links.profile").exists())
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
                .andExpect(jsonPath("seller").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.load-all-products").exists())
                .andExpect(jsonPath("_links.load-product").exists())
                .andExpect(jsonPath("_links.create-product-image").exists())
                .andExpect(jsonPath("_links.modify-product").exists())
                .andExpect(jsonPath("_links.delete-product").exists())
                .andExpect(jsonPath("_links.create-orders").exists())
                .andExpect(jsonPath("_links.profile").exists())
        ;

        String contentAsString = actions.andReturn().getResponse().getContentAsString();
        ProductResponseDto responseDto = objectMapper.readValue(contentAsString, ProductResponseDto.class);

        this.mockMvc.perform(delete(PRODUCT_URL + responseDto.getId())
                .header(HttpHeaders.AUTHORIZATION, token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("name").value(appProperties.getTestProductName()))
                .andExpect(jsonPath("image").value(appProperties.getTestImage()))
                .andExpect(jsonPath("size").value(appProperties.getTestSize()))
                .andExpect(jsonPath("price").value(appProperties.getTestPrice()))
                .andExpect(jsonPath("stock").value(appProperties.getTestStock()))
                .andExpect(jsonPath("category").value(ProductCategory.ACCESSORY.name()))
                .andExpect(jsonPath("seller").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.load-all-products").exists())
                .andExpect(jsonPath("_links.create-product").exists())
                .andExpect(jsonPath("_links.create-product-image").exists())
                .andExpect(jsonPath("_links.create-orders").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document(DELETE_PRODUCT.getRel(),
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel(PROFILE.getRel()).description("link to profile"),
                                linkWithRel(LOAD_ALL_PRODUCT.getRel()).description("link to load all products"),
                                linkWithRel(CREATE_PRODUCT.getRel()).description("link to create product"),
                                linkWithRel(CREATE_PRODUCT_IMAGE.getRel()).description("link to create product image"),
                                linkWithRel(CREATE_ORDERS.getRel()).description("link to create orders")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("authorization header")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        responseFields(
                                fieldWithPath("id").description("Identifier of Deleted Product"),
                                fieldWithPath("name").description("Name of Deleted Product"),
                                fieldWithPath("size").description("Size of Deleted Product"),
                                fieldWithPath("image").description("Image of Deleted Product"),
                                fieldWithPath("price").description("Price of Deleted Product"),
                                fieldWithPath("stock").description("Stock of Deleted Product"),
                                fieldWithPath("category").description("Category of Deleted Product"),
                                fieldWithPath("seller.id").description("Identifier of Seller"),
                                fieldWithPath("seller.createdAt").description("Created Date of Seller"),
                                fieldWithPath("seller.modifiedAt").description("Modified Date of Seller"),
                                fieldWithPath("seller.email").description("E-Mail of Seller"),
                                fieldWithPath("seller.name").description("Name of Seller"),
                                fieldWithPath("seller.address").description("Address of Seller"),
                                fieldWithPath("seller.balance").description("Balance of Seller"),
                                fieldWithPath("seller.accountRole").description("Role of Seller"),
                                fieldWithPath("buyer").description("Buyer"),
                                fieldWithPath("_links.self.href").description("link to self"),
                                fieldWithPath("_links.profile.href").description("link to profile"),
                                fieldWithPath("_links.load-all-products.href").description("link to load all products"),
                                fieldWithPath("_links.create-product.href").description("link to create product"),
                                fieldWithPath("_links.create-product-image.href").description("link to create product image"),
                                fieldWithPath("_links.create-orders.href").description("link to create orders")
                        )
                ))
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
                .andExpect(jsonPath("seller").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.load-all-products").exists())
                .andExpect(jsonPath("_links.load-product").exists())
                .andExpect(jsonPath("_links.create-product-image").exists())
                .andExpect(jsonPath("_links.modify-product").exists())
                .andExpect(jsonPath("_links.delete-product").exists())
                .andExpect(jsonPath("_links.create-orders").exists())
                .andExpect(jsonPath("_links.profile").exists())
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
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.create-account").exists())
                .andExpect(jsonPath("_links.login-account").exists())
                .andExpect(jsonPath("_links.profile").exists())
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
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.login-account").exists())
                .andExpect(jsonPath("_links.profile").exists())
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
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.login-account").exists())
                .andExpect(jsonPath("_links.profile").exists())
        ;

        return generateToken(actions);
    }

    private void saveCustomProduct(String token) {
        List<String> names = Arrays.asList("Nike", "Nike Shoes", "Nike Hood", "Nike Wind", "Adidas", "Adidas Cap", "Adidas Socks", "Carhartt Jeans", "Carhartt Jeans", "Carhartt Wind");
        List<String> sizes = Arrays.asList("L", "265", "XL", "M", "32", "S", "270", "32", "34", "XL");
        List<Long> prices = Arrays.asList(10000L, 40000L, 30000L, 20000L, 80000L, 90000L, 70000L, 50000L, 20000L, 10000L);
        List<ProductCategory> categories = Arrays.asList(ProductCategory.TOP, ProductCategory.SHOES, ProductCategory.TOP, ProductCategory.TOP, ProductCategory.BOTTOMS, ProductCategory.ACCESSORY, ProductCategory.SHOES, ProductCategory.BOTTOMS, ProductCategory.BOTTOMS, ProductCategory.TOP);
        IntStream.rangeClosed(1, 10).forEach(i -> {
            ProductRequestDto requestDto = createProductRequestDto(names.get(i - 1), sizes.get(i - 1), prices.get(i - 1), categories.get(i - 1));

            try {
                this.mockMvc.perform(post(PRODUCT_URL)
                        .accept(MediaTypes.HAL_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .header(HttpHeaders.AUTHORIZATION, token))
                        .andDo(print())
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("id").exists())
                        .andExpect(jsonPath("name").value(requestDto.getName()))
                        .andExpect(jsonPath("image").value(appProperties.getTestImage()))
                        .andExpect(jsonPath("size").value(requestDto.getSize()))
                        .andExpect(jsonPath("price").value(requestDto.getPrice()))
                        .andExpect(jsonPath("stock").value(appProperties.getTestStock()))
                        .andExpect(jsonPath("category").value(requestDto.getCategory().name()))
                        .andExpect(jsonPath("seller").exists())
                        .andExpect(jsonPath("_links.self").exists())
                        .andExpect(jsonPath("_links.load-all-products").exists())
                        .andExpect(jsonPath("_links.load-product").exists())
                        .andExpect(jsonPath("_links.create-product-image").exists())
                        .andExpect(jsonPath("_links.modify-product").exists())
                        .andExpect(jsonPath("_links.delete-product").exists())
                        .andExpect(jsonPath("_links.create-orders").exists())
                        .andExpect(jsonPath("_links.profile").exists())
                ;
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
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