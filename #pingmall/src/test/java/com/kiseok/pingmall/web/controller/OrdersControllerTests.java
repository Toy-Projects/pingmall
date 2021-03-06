package com.kiseok.pingmall.web.controller;

import com.kiseok.pingmall.common.domain.account.AccountRole;
import com.kiseok.pingmall.common.domain.product.ProductCategory;
import com.kiseok.pingmall.common.domain.verification.Verification;
import com.kiseok.pingmall.web.common.BaseControllerTests;
import com.kiseok.pingmall.web.dto.account.AccountRequestDto;
import com.kiseok.pingmall.web.dto.order.OrdersRequestDto;
import com.kiseok.pingmall.web.dto.product.ProductRequestDto;
import com.kiseok.pingmall.web.dto.product.ProductResponseDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.IntStream;
import static com.kiseok.pingmall.common.resources.RestDocsResource.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class OrdersControllerTests extends BaseControllerTests {

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
    void tearDown() {
        this.ordersRepository.deleteAll();
        this.productRepository.deleteAll();
        this.accountRepository.deleteAll();
        this.verificationRepository.deleteAll();
    }

    @DisplayName("주문 등록 시 유효성 검사 실패 -> 400 BAD_REQUEST")
    @Test
    void save_orders_invalid_400() throws Exception {
        String token = createAccountAndToken();
        String anotherToken = createAnotherAccountAndToken();

        List<Long> productIdList = collectProductIds(token);
        List<OrdersRequestDto> ordersRequestDtoList = new ArrayList<>();
        OrdersRequestDto requestDto = createOrdersRequestWithParam(0L, productIdList.get(1));
        OrdersRequestDto requestDto2 = createOrdersRequestWithParam(null, productIdList.get(2));

        ordersRequestDtoList.add(requestDto);
        ordersRequestDtoList.add(requestDto2);

        this.mockMvc.perform(post(ORDERS_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ordersRequestDtoList))
                .header(HttpHeaders.AUTHORIZATION, anotherToken))
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

    @DisplayName("DB에 없는 유저로 주문 시 -> 401 UNAUTHORIZED")
    @Test
    void save_orders_account_id_null__401() throws Exception  {
        String token = createAccountAndToken();

        List<Long> productIdList = collectProductIds(token);
        List<OrdersRequestDto> ordersRequestDtoList = new ArrayList<>();
        IntStream.rangeClosed(1, 10).forEach(i -> ordersRequestDtoList.add(createOrdersRequestDto(productIdList.get(i - 1))));

        this.mockMvc.perform(post(ORDERS_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ordersRequestDtoList)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
        ;
    }

    @DisplayName("DB에 없는 제품 주문 시 -> 404 NOT_FOUND")
    @Test
    void save_orders_product_id_null_404() throws Exception {
        String anotherToken = createAnotherAccountAndToken();

        List<OrdersRequestDto> ordersRequestDtoList = new ArrayList<>();
        IntStream.rangeClosed(1, 10).forEach(i -> ordersRequestDtoList.add(createOrdersRequestDto(0L)));

        this.mockMvc.perform(post(ORDERS_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ordersRequestDtoList))
                .header(HttpHeaders.AUTHORIZATION, anotherToken))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("status").exists())
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("code").exists())
                .andExpect(jsonPath("errors").exists())
                .andExpect(jsonPath("erroredAt").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.load-index").exists())
                .andExpect(jsonPath("_links.profile").exists())
        ;
    }

    @DisplayName("자신이 파는 제품을 구매할 시 -> 400 BAD_REQUEST")
    @Test
    void save_orders_account_id_seller_id_equals_400() throws Exception {
        String token = createAccountAndToken();

        List<Long> productIdList = collectProductIds(token);
        List<OrdersRequestDto> ordersRequestDtoList = new ArrayList<>();
        IntStream.rangeClosed(1, 10).forEach(i -> ordersRequestDtoList.add(createOrdersRequestDto(productIdList.get(i - 1))));

        this.mockMvc.perform(post(ORDERS_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ordersRequestDtoList))
                .header(HttpHeaders.AUTHORIZATION, token))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").exists())
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("code").exists())
                .andExpect(jsonPath("errors").exists())
                .andExpect(jsonPath("erroredAt").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.load-index").exists())
                .andExpect(jsonPath("_links.profile").exists())
        ;
    }

    @DisplayName("구매할 때 잔액이 부족할 시 -> 400 BAD_REQUEST")
    @Test
    void save_orders_balance_shortage_400() throws Exception    {
        String token = createAccountAndToken();
        String anotherToken = createAnotherAccountAndTokenWithShortageBalance(1000L);

        List<Long> productIdList = collectProductIds(token);
        List<OrdersRequestDto> ordersRequestDtoList = new ArrayList<>();
        IntStream.rangeClosed(1, 10).forEach(i -> ordersRequestDtoList.add(createOrdersRequestDto(productIdList.get(i - 1))));

        this.mockMvc.perform(post(ORDERS_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ordersRequestDtoList))
                .header(HttpHeaders.AUTHORIZATION, anotherToken))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").exists())
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("code").exists())
                .andExpect(jsonPath("errors").exists())
                .andExpect(jsonPath("erroredAt").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.load-index").exists())
                .andExpect(jsonPath("_links.profile").exists())
        ;
    }

    @DisplayName("구매할 때 잔액이 0원 이하일 시 -> 400 BAD_REQUEST")
    @Test
    void save_orders_balance_below_zero_400() throws Exception  {
        String token = createAccountAndToken();
        String anotherToken = createAnotherAccountAndTokenWithShortageBalance(40000L);

        ProductResponseDto productResponseDto = createProductWithShortageStock(token, 2L);
        List<OrdersRequestDto> ordersRequestDtoList = new ArrayList<>();
        OrdersRequestDto ordersRequestDto = createOrdersRequestDto(productResponseDto.getId());
        ordersRequestDto.setAmount(2L);
        ordersRequestDtoList.add(ordersRequestDto);

        this.mockMvc.perform(post(ORDERS_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ordersRequestDtoList))
                .header(HttpHeaders.AUTHORIZATION, anotherToken))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("_embedded.ordersResponseDtoList.[*].id").exists())
                .andExpect(jsonPath("_embedded.ordersResponseDtoList.[*].number").exists())
                .andExpect(jsonPath("_embedded.ordersResponseDtoList.[*].amount").exists())
                .andExpect(jsonPath("_embedded.ordersResponseDtoList.[*].buyer").exists())
                .andExpect(jsonPath("_embedded.ordersResponseDtoList.[*].product").exists())
                .andExpect(jsonPath("_embedded.ordersResponseDtoList.[*]._links.self").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
        ;

        ordersRequestDtoList = new ArrayList<>();
        ordersRequestDto = createOrdersRequestDto(productResponseDto.getId());
        ordersRequestDto.setAmount(1L);
        ordersRequestDtoList.add(ordersRequestDto);

        this.mockMvc.perform(post(ORDERS_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ordersRequestDtoList))
                .header(HttpHeaders.AUTHORIZATION, anotherToken))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").exists())
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("code").exists())
                .andExpect(jsonPath("errors").exists())
                .andExpect(jsonPath("erroredAt").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.load-index").exists())
                .andExpect(jsonPath("_links.profile").exists())
        ;
    }

    @DisplayName("구매할 때 재고가 부족할 시 -> 400 BAD_REQUEST")
    @Test
    void save_orders_stock_shortage_400() throws Exception  {
        String token = createAccountAndToken();
        String anotherToken = createAnotherAccountAndToken();

        ProductResponseDto responseDto = createProducts(token);
        List<OrdersRequestDto> ordersRequestDtoList = new ArrayList<>();
        IntStream.rangeClosed(1, 10).forEach(i -> ordersRequestDtoList.add(createOrdersRequestDto(responseDto.getId())));

        this.mockMvc.perform(post(ORDERS_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ordersRequestDtoList))
                .header(HttpHeaders.AUTHORIZATION, anotherToken))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").exists())
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("code").exists())
                .andExpect(jsonPath("errors").exists())
                .andExpect(jsonPath("erroredAt").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.load-index").exists())
                .andExpect(jsonPath("_links.profile").exists())
        ;
    }

    @DisplayName("구매할 때 재고가 0개 이하일 시 -> 400 BAD_REQUEST")
    @Test
    void save_orders_stock_below_zero() throws Exception    {
        String token = createAccountAndToken();
        String anotherToken = createAnotherAccountAndToken();

        ProductResponseDto responseDto = createProductWithShortageStock(token, 0L);
        List<OrdersRequestDto> ordersRequestDtoList = new ArrayList<>();
        IntStream.rangeClosed(1, 10).forEach(i -> ordersRequestDtoList.add(createOrdersRequestDto(responseDto.getId())));

        this.mockMvc.perform(post(ORDERS_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ordersRequestDtoList))
                .header(HttpHeaders.AUTHORIZATION, anotherToken))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").exists())
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("code").exists())
                .andExpect(jsonPath("errors").exists())
                .andExpect(jsonPath("erroredAt").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.load-index").exists())
                .andExpect(jsonPath("_links.profile").exists())
        ;
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
                .andExpect(jsonPath("_embedded.ordersResponseDtoList.[*].id").exists())
                .andExpect(jsonPath("_embedded.ordersResponseDtoList.[*].number").exists())
                .andExpect(jsonPath("_embedded.ordersResponseDtoList.[*].amount").exists())
                .andExpect(jsonPath("_embedded.ordersResponseDtoList.[*].buyer").exists())
                .andExpect(jsonPath("_embedded.ordersResponseDtoList.[*].product").exists())
                .andExpect(jsonPath("_embedded.ordersResponseDtoList.[*]._links.self").exists())
                .andExpect(jsonPath("_embedded.ordersResponseDtoList.[*]._links.create-product").exists())
                .andExpect(jsonPath("_embedded.ordersResponseDtoList.[*]._links.create-product-image").exists())
                .andExpect(jsonPath("_embedded.ordersResponseDtoList.[*]._links.modify-product").exists())
                .andExpect(jsonPath("_embedded.ordersResponseDtoList.[*]._links.delete-product").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document(CREATE_ORDERS.getRel(),
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel(PROFILE.getRel()).description("link to profile")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header"),
                                headerWithName(HttpHeaders.AUTHORIZATION).description("authorization header")
                        ),
                        requestFields(
                                fieldWithPath("[*].amount").description("Amount of new Orders"),
                                fieldWithPath("[*].productId").description("Product Id of new Orders")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        responseFields(
                                fieldWithPath("_embedded.ordersResponseDtoList.[*].id").description("Identifier of new Orders"),
                                fieldWithPath("_embedded.ordersResponseDtoList.[*].number").description("Name of new Orders"),
                                fieldWithPath("_embedded.ordersResponseDtoList.[*].amount").description("Size of new Orders"),
                                // buyer
                                fieldWithPath("_embedded.ordersResponseDtoList.[*].buyer").description("Buyer"),
                                fieldWithPath("_embedded.ordersResponseDtoList.[*].buyer.id").description("Identifier of Buyer"),
                                fieldWithPath("_embedded.ordersResponseDtoList.[*].buyer.createdAt").description("Created Date of Buyer"),
                                fieldWithPath("_embedded.ordersResponseDtoList.[*].buyer.modifiedAt").description("Modified Date of Buyer"),
                                fieldWithPath("_embedded.ordersResponseDtoList.[*].buyer.email").description("E-Mail of Buyer"),
                                fieldWithPath("_embedded.ordersResponseDtoList.[*].buyer.name").description("Name of Buyer"),
                                fieldWithPath("_embedded.ordersResponseDtoList.[*].buyer.address").description("Address of Buyer"),
                                fieldWithPath("_embedded.ordersResponseDtoList.[*].buyer.balance").description("Balance of Buyer"),
                                fieldWithPath("_embedded.ordersResponseDtoList.[*].buyer.accountRole").description("Role of Buyer"),
                                // product
                                fieldWithPath("_embedded.ordersResponseDtoList.[*].product").description("Product"),
                                fieldWithPath("_embedded.ordersResponseDtoList.[*].product.id").description("Identifier of Product"),
                                fieldWithPath("_embedded.ordersResponseDtoList.[*].product.createdAt").description("Created Date of Product"),
                                fieldWithPath("_embedded.ordersResponseDtoList.[*].product.modifiedAt").description("Modified Date of Product"),
                                fieldWithPath("_embedded.ordersResponseDtoList.[*].product.name").description("Name of Product"),
                                fieldWithPath("_embedded.ordersResponseDtoList.[*].product.size").description("Size of Product"),
                                fieldWithPath("_embedded.ordersResponseDtoList.[*].product.image").description("Image of Product"),
                                fieldWithPath("_embedded.ordersResponseDtoList.[*].product.price").description("Price of Product"),
                                fieldWithPath("_embedded.ordersResponseDtoList.[*].product.stock").description("Stock of Product"),
                                fieldWithPath("_embedded.ordersResponseDtoList.[*].product.category").description("Category of Product"),
                                fieldWithPath("_embedded.ordersResponseDtoList.[*].product.comment.[*]").description("Comments of Product"),
                                // product.orders
                                fieldWithPath("_embedded.ordersResponseDtoList.[*].product.orders.[*]").description("Orders"),
                                fieldWithPath("_embedded.ordersResponseDtoList.[*].product.orders.[*].id").description("Identifier of the Product's Orders"),
                                fieldWithPath("_embedded.ordersResponseDtoList.[*].product.orders.[*].createdAt").description("Created Date of the Product's Orders"),
                                fieldWithPath("_embedded.ordersResponseDtoList.[*].product.orders.[*].modifiedAt").description("Modified Date of the Product's Orders"),
                                fieldWithPath("_embedded.ordersResponseDtoList.[*].product.orders.[*].number").description("Number of the Product's Orders"),
                                fieldWithPath("_embedded.ordersResponseDtoList.[*].product.orders.[*].amount").description("Amount of the Product's Orders"),
                                fieldWithPath("_embedded.ordersResponseDtoList.[*].product.orders.[*].buyer").description("Buyer of the Product's Orders"),
                                fieldWithPath("_embedded.ordersResponseDtoList.[*].product.orders.[*].product").description("Product of the Product's Orders"),
                                // product.seller
                                fieldWithPath("_embedded.ordersResponseDtoList.[*].product.seller.id").description("Identifier of Seller"),
                                fieldWithPath("_embedded.ordersResponseDtoList.[*].product.seller.createdAt").description("Created Date of Seller"),
                                fieldWithPath("_embedded.ordersResponseDtoList.[*].product.seller.modifiedAt").description("Modified Date of Seller"),
                                fieldWithPath("_embedded.ordersResponseDtoList.[*].product.seller.email").description("E-Mail of Seller"),
                                fieldWithPath("_embedded.ordersResponseDtoList.[*].product.seller.name").description("Name of Seller"),
                                fieldWithPath("_embedded.ordersResponseDtoList.[*].product.seller.address").description("Address of Seller"),
                                fieldWithPath("_embedded.ordersResponseDtoList.[*].product.seller.balance").description("Balance of Seller"),
                                fieldWithPath("_embedded.ordersResponseDtoList.[*].product.seller.accountRole").description("Role of Seller"),
                                // _links
                                fieldWithPath("_embedded.ordersResponseDtoList.[*]._links.self.href").description("link to self"),
                                fieldWithPath("_embedded.ordersResponseDtoList.[*]._links.create-product.href").description("link to create product"),
                                fieldWithPath("_embedded.ordersResponseDtoList.[*]._links.create-product-image.href").description("link to create product image"),
                                fieldWithPath("_embedded.ordersResponseDtoList.[*]._links.modify-product.href").description("link to modify product"),
                                fieldWithPath("_embedded.ordersResponseDtoList.[*]._links.delete-product.href").description("link to delete product"),
                                fieldWithPath("_links.self.href").description("link to self"),
                                fieldWithPath("_links.profile.href").description("link to profile")
                        )
                ))
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

    private String createAnotherAccountAndTokenWithShortageBalance(Long balance) throws Exception {
        AccountRequestDto requestDto = createAnotherAccountRequestDto();
        requestDto.setBalance(balance);
        ResultActions actions = this.mockMvc.perform(post(ACCOUNT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").value(ANOTHER + appProperties.getTestEmail()))
                .andExpect(jsonPath("password").doesNotExist())
                .andExpect(jsonPath("name").value(ANOTHER + appProperties.getTestName()))
                .andExpect(jsonPath("address").value(ANOTHER + appProperties.getTestAddress()))
                .andExpect(jsonPath("balance").value(balance))
                .andExpect(jsonPath("accountRole").value(AccountRole.USER.name()))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.login-account").exists())
                .andExpect(jsonPath("_links.profile").exists())
        ;

        return generateToken(actions);
    }

    private List<Long> collectProductIds(String token)  {
        List<Long> productIdList = new ArrayList<>();

        IntStream.rangeClosed(1, 10).forEach(i -> {
            ProductResponseDto responseDto = null;
            try {
                responseDto = createProducts(token);
            } catch (Exception e) {
                e.printStackTrace();
            }
            productIdList.add(Objects.requireNonNull(responseDto).getId());
        });

        return productIdList;
    }

    private ProductResponseDto createProducts(String token) throws Exception {
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
                .andExpect(jsonPath("seller").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.load-all-products").exists())
                .andExpect(jsonPath("_links.load-product").exists())
                .andExpect(jsonPath("_links.modify-product").exists())
                .andExpect(jsonPath("_links.delete-product").exists())
                .andExpect(jsonPath("_links.create-orders").exists())
                .andExpect(jsonPath("_links.profile").exists())
        ;

        String productAsString = actions.andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(productAsString, ProductResponseDto.class);
    }

    private ProductResponseDto createProductWithShortageStock(String token, long stock) throws Exception {
        ProductRequestDto productRequestDto = createProductRequestDto();
        productRequestDto.setStock(stock);
        ResultActions actions = this.mockMvc.perform(post(PRODUCT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequestDto))
                .header(HttpHeaders.AUTHORIZATION, token))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("name").value(appProperties.getTestProductName()))
                .andExpect(jsonPath("image").value(appProperties.getTestImage()))
                .andExpect(jsonPath("size").value(appProperties.getTestSize()))
                .andExpect(jsonPath("price").value(appProperties.getTestPrice()))
                .andExpect(jsonPath("stock").value(stock))
                .andExpect(jsonPath("category").value(ProductCategory.ACCESSORY.name()))
                .andExpect(jsonPath("seller").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.load-all-products").exists())
                .andExpect(jsonPath("_links.load-product").exists())
                .andExpect(jsonPath("_links.modify-product").exists())
                .andExpect(jsonPath("_links.delete-product").exists())
                .andExpect(jsonPath("_links.create-orders").exists())
                .andExpect(jsonPath("_links.profile").exists())
        ;

        String productAsString = actions.andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(productAsString, ProductResponseDto.class);
    }
}
