package com.kiseok.pingmall.web.controller;

import com.kiseok.pingmall.common.domain.comment.CommentType;
import com.kiseok.pingmall.common.domain.verification.Verification;
import com.kiseok.pingmall.web.common.BaseControllerTests;
import com.kiseok.pingmall.web.dto.account.AccountRequestDto;
import com.kiseok.pingmall.web.dto.comment.CommentModifyRequestDto;
import com.kiseok.pingmall.web.dto.comment.CommentRequestDto;
import com.kiseok.pingmall.web.dto.comment.CommentResponseDto;
import com.kiseok.pingmall.web.dto.product.ProductRequestDto;
import com.kiseok.pingmall.web.dto.product.ProductResponseDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
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
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class CommentControllerTests extends BaseControllerTests {

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
        this.commentRepository.deleteAll();
        this.productRepository.deleteAll();
        this.accountRepository.deleteAll();
        this.verificationRepository.deleteAll();
    }

    @DisplayName("댓글 저장시 유효성 검사 실패 -> 400 BAD_REQUEST")
    @ParameterizedTest(name = "{index} {displayName} message={0}")
    @MethodSource("validSaveComment")
    void save_comment_invalid_400(String content, CommentType commentType, Long productId) throws Exception    {
        String jwt = createAccountAndJwt(createAnotherAccountRequestDto());
        CommentRequestDto commentRequestDto = CommentRequestDto.builder()
                .content(content)
                .commentType(commentType)
                .productId(productId)
                .build();

        this.mockMvc.perform(post(COMMENT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .content(objectMapper.writeValueAsString(commentRequestDto)))
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

    @DisplayName("댓글을 달 제품이 DB에 없을 시 -> 404 NOT_FOUND")
    @Test
    void save_comment_product_id_null_404() throws Exception    {
        String jwt = createAccountAndJwt(createAccountRequestDto());
        ProductResponseDto productResponseDto = getProductResponseDto(jwt);
        jwt = createAccountAndJwt(createAnotherAccountRequestDto());
        CommentRequestDto commentRequestDto = createCommentRequestDto(productResponseDto.getId());
        commentRequestDto.setProductId(-1L);

        this.mockMvc.perform(post(COMMENT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .content(objectMapper.writeValueAsString(commentRequestDto)))
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

    @DisplayName("정상적으로 댓글 저장 -> 201 CREATED")
    @Test
    void save_comment_201() throws Exception    {
        String jwt = createAccountAndJwt(createAccountRequestDto());
        ProductResponseDto productResponseDto = getProductResponseDto(jwt);
        jwt = createAccountAndJwt(createAnotherAccountRequestDto());
        CommentRequestDto commentRequestDto = createCommentRequestDto(productResponseDto.getId());

        this.mockMvc.perform(post(COMMENT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .content(objectMapper.writeValueAsString(commentRequestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("content").exists())
                .andExpect(jsonPath("writer").exists())
                .andExpect(jsonPath("product").exists())
                .andDo(document(CREATE_COMMENT.getRel(),
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel(PROFILE.getRel()).description("link to profile"),
                                linkWithRel(LOAD_PRODUCT.getRel()).description("link to load product"),
                                linkWithRel(LOAD_ACCOUNT.getRel()).description("link to load account"),
                                linkWithRel(LOAD_COMMENT.getRel()).description("link to load comment"),
                                linkWithRel(LOAD_ALL_COMMENTS.getRel()).description("link to load all comments"),
                                linkWithRel(MODIFY_COMMENT.getRel()).description("link to modify comment"),
                                linkWithRel(DELETE_COMMENT.getRel()).description("link to delete comment")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header"),
                                headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                headerWithName(HttpHeaders.AUTHORIZATION).description("authorization header")
                        ),
                        requestFields(
                                fieldWithPath("content").description("Content of New Comment"),
                                fieldWithPath("commentType").description("CommentType of new Comment"),
                                fieldWithPath("productId").description("Product Id of new Comment")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.LOCATION).description("location header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        responseFields(
                                fieldWithPath("id").description("Identifier of New Comment"),
                                fieldWithPath("content").description("Content of New Comment"),
                                fieldWithPath("commentType").description("CommentType of New Comment"),
                                fieldWithPath("writer").description("Writer of New Comment"),
                                fieldWithPath("writer.id").description("Writer's Identifier"),
                                fieldWithPath("writer.createdAt").description("Writer's Created Date"),
                                fieldWithPath("writer.modifiedAt").description("Writer's Modified Date"),
                                fieldWithPath("writer.email").description("Writer's Email"),
                                fieldWithPath("writer.name").description("Writer's Name"),
                                fieldWithPath("writer.address").description("Writer's Address"),
                                fieldWithPath("writer.balance").description("Writer's Balance"),
                                fieldWithPath("writer.accountRole").description("Writer's Role"),
                                fieldWithPath("product").description("Product of New Comment"),
                                fieldWithPath("product.id").description("Product's Identifier"),
                                fieldWithPath("product.createdAt").description("Product's Created Date"),
                                fieldWithPath("product.modifiedAt").description("Product's Modified Date"),
                                fieldWithPath("product.name").description("Product's Name"),
                                fieldWithPath("product.size").description("Product's Size"),
                                fieldWithPath("product.image").description("Product's Image"),
                                fieldWithPath("product.price").description("Product's Price"),
                                fieldWithPath("product.stock").description("Product's Stock"),
                                fieldWithPath("product.category").description("Product's Category"),
                                fieldWithPath("product.orders").description("Ordered List of Product"),
                                fieldWithPath("product.seller").description("Seller of Product"),
                                fieldWithPath("product.seller.id").description("Product Seller's Identifier"),
                                fieldWithPath("product.seller.createdAt").description("Product Seller's Created Date"),
                                fieldWithPath("product.seller.modifiedAt").description("Product Seller's Modified Date"),
                                fieldWithPath("product.seller.email").description("Product Seller's Email"),
                                fieldWithPath("product.seller.name").description("Product Seller's Name"),
                                fieldWithPath("product.seller.address").description("Product Seller's Address"),
                                fieldWithPath("product.seller.balance").description("Product Seller's Balance"),
                                fieldWithPath("product.seller.accountRole").description("Product Seller's Role"),
                                fieldWithPath("product.comment").description("Product's Comment"),
                                fieldWithPath("product.comment.[*].id").description("Product Comment's Identifier"),
                                fieldWithPath("product.comment.[*].createdAt").description("Product Comment's Created Date"),
                                fieldWithPath("product.comment.[*].modifiedAt").description("Product Comment's Modified Date"),
                                fieldWithPath("product.comment.[*].content").description("Product Comment's Content"),
                                fieldWithPath("product.comment.[*].commentType").description("Product Comment's CommentType"),
                                fieldWithPath("product.comment.[*].writer").description("Product Comment's Writer"),
                                fieldWithPath("product.comment.[*].product").description("Product Comment's Product"),
                                fieldWithPath("_links.self.href").description("link to self"),
                                fieldWithPath("_links.profile.href").description("link to profile"),
                                fieldWithPath("_links.load-product.href").description("link to load product"),
                                fieldWithPath("_links.load-account.href").description("link to load account"),
                                fieldWithPath("_links.load-comment.href").description("link to load comment"),
                                fieldWithPath("_links.load-all-comments.href").description("link to load all comments"),
                                fieldWithPath("_links.modify-comment.href").description("link to modify comment"),
                                fieldWithPath("_links.delete-comment.href").description("link to delete comment")
                        )
                ))
        ;
    }

    @DisplayName("DB에 없는 댓글 불러오기 -> 404 NOT_FOUND")
    @Test
    void load_comment_not_found_404() throws Exception  {
        String jwt = createAccountAndJwt(createAccountRequestDto());
        ProductResponseDto productResponseDto = getProductResponseDto(jwt);
        jwt = createAccountAndJwt(createAnotherAccountRequestDto());
        CommentRequestDto commentRequestDto = createCommentRequestDto(productResponseDto.getId());

        this.mockMvc.perform(post(COMMENT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .content(objectMapper.writeValueAsString(commentRequestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("content").exists())
                .andExpect(jsonPath("writer").exists())
                .andExpect(jsonPath("product").exists())
        ;

        this.mockMvc.perform(get(COMMENT_URL + "-1")
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

    @DisplayName("정상적으로 댓글 불러오기 -> 200 OK")
    @Test
    void load_comment_200() throws Exception    {
        String jwt = createAccountAndJwt(createAccountRequestDto());
        ProductResponseDto productResponseDto = getProductResponseDto(jwt);
        jwt = createAccountAndJwt(createAnotherAccountRequestDto());
        CommentRequestDto commentRequestDto = createCommentRequestDto(productResponseDto.getId());

        ResultActions actions = this.mockMvc.perform(post(COMMENT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .content(objectMapper.writeValueAsString(commentRequestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("content").exists())
                .andExpect(jsonPath("writer").exists())
                .andExpect(jsonPath("product").exists());

        String contentAsString = actions.andReturn().getResponse().getContentAsString();
        CommentResponseDto responseDto = objectMapper.readValue(contentAsString, CommentResponseDto.class);

        this.mockMvc.perform(get(COMMENT_URL + responseDto.getId())
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("content").exists())
                .andExpect(jsonPath("writer").exists())
                .andExpect(jsonPath("product").exists())
                .andDo(document(LOAD_COMMENT.getRel(),
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel(PROFILE.getRel()).description("link to profile"),
                                linkWithRel(LOAD_PRODUCT.getRel()).description("link to load product"),
                                linkWithRel(LOAD_ACCOUNT.getRel()).description("link to load account"),
                                linkWithRel(LOAD_ALL_COMMENTS.getRel()).description("link to load all comments"),
                                linkWithRel(CREATE_COMMENT.getRel()).description("link to create comment"),
                                linkWithRel(MODIFY_COMMENT.getRel()).description("link to modify comment"),
                                linkWithRel(DELETE_COMMENT.getRel()).description("link to delete comment")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header"),
                                headerWithName(HttpHeaders.ACCEPT).description("accept header")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        responseFields(
                                fieldWithPath("id").description("Identifier of New Comment"),
                                fieldWithPath("content").description("Content of New Comment"),
                                fieldWithPath("commentType").description("CommentType of New Comment"),
                                fieldWithPath("writer").description("Writer of New Comment"),
                                fieldWithPath("writer.id").description("Writer's Identifier"),
                                fieldWithPath("writer.createdAt").description("Writer's Created Date"),
                                fieldWithPath("writer.modifiedAt").description("Writer's Modified Date"),
                                fieldWithPath("writer.email").description("Writer's Email"),
                                fieldWithPath("writer.name").description("Writer's Name"),
                                fieldWithPath("writer.address").description("Writer's Address"),
                                fieldWithPath("writer.balance").description("Writer's Balance"),
                                fieldWithPath("writer.accountRole").description("Writer's Role"),
                                fieldWithPath("product").description("Product of New Comment"),
                                fieldWithPath("product.id").description("Product's Identifier"),
                                fieldWithPath("product.createdAt").description("Product's Created Date"),
                                fieldWithPath("product.modifiedAt").description("Product's Modified Date"),
                                fieldWithPath("product.name").description("Product's Name"),
                                fieldWithPath("product.size").description("Product's Size"),
                                fieldWithPath("product.image").description("Product's Image"),
                                fieldWithPath("product.price").description("Product's Price"),
                                fieldWithPath("product.stock").description("Product's Stock"),
                                fieldWithPath("product.category").description("Product's Category"),
                                fieldWithPath("product.orders").description("Ordered List of Product"),
                                fieldWithPath("product.seller").description("Seller of Product"),
                                fieldWithPath("product.seller.id").description("Product Seller's Identifier"),
                                fieldWithPath("product.seller.createdAt").description("Product Seller's Created Date"),
                                fieldWithPath("product.seller.modifiedAt").description("Product Seller's Modified Date"),
                                fieldWithPath("product.seller.email").description("Product Seller's Email"),
                                fieldWithPath("product.seller.name").description("Product Seller's Name"),
                                fieldWithPath("product.seller.address").description("Product Seller's Address"),
                                fieldWithPath("product.seller.balance").description("Product Seller's Balance"),
                                fieldWithPath("product.seller.accountRole").description("Product Seller's Role"),
                                fieldWithPath("product.comment").description("Product's Comment"),
                                fieldWithPath("product.comment.[*].id").description("Product Comment's Identifier"),
                                fieldWithPath("product.comment.[*].createdAt").description("Product Comment's Created Date"),
                                fieldWithPath("product.comment.[*].modifiedAt").description("Product Comment's Modified Date"),
                                fieldWithPath("product.comment.[*].content").description("Product Comment's Content"),
                                fieldWithPath("product.comment.[*].commentType").description("Product Comment's CommentType"),
                                fieldWithPath("product.comment.[*].writer").description("Product Comment's Writer"),
                                fieldWithPath("product.comment.[*].product").description("Product Comment's Product"),
                                fieldWithPath("_links.self.href").description("link to self"),
                                fieldWithPath("_links.profile.href").description("link to profile"),
                                fieldWithPath("_links.load-product.href").description("link to load product"),
                                fieldWithPath("_links.load-account.href").description("link to load account"),
                                fieldWithPath("_links.load-all-comments.href").description("link to load all comments"),
                                fieldWithPath("_links.create-comment.href").description("link to create comment"),
                                fieldWithPath("_links.modify-comment.href").description("link to modify comment"),
                                fieldWithPath("_links.delete-comment.href").description("link to delete comment")
                        )
                ))
        ;
    }

    @DisplayName("제품 아이디가 null인 채 모든 댓글 불러오기 -> 404 NOT_FOUND")
    @Test
    void load_all_comments_product_id_not_found_404() throws Exception  {
        String jwt = createAccountAndJwt(createAccountRequestDto());
        ProductResponseDto productResponseDto = getProductResponseDto(jwt);
        jwt = createAccountAndJwt(createAnotherAccountRequestDto());
        saveCustomComment(jwt, productResponseDto.getId());

        this.mockMvc.perform(get(COMMENT_URL)
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

    @DisplayName("정상적으로 필터링된 모든 댓글 불러오기 -> 200 OK")
    @Test
    void load_all_comments_200() throws Exception   {
        String jwt = createAccountAndJwt(createAccountRequestDto());
        ProductResponseDto productResponseDto = getProductResponseDto(jwt);
        jwt = createAccountAndJwt(createAnotherAccountRequestDto());
        saveCustomComment(jwt, productResponseDto.getId());

        this.mockMvc.perform(get(COMMENT_URL)
                .queryParam("commentType", CommentType.EPILOGUE.name())
                .queryParam("productId", productResponseDto.getId().toString())
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(LOAD_ALL_COMMENTS.getRel(),
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel(PROFILE.getRel()).description("link to profile"),
                                linkWithRel(CREATE_COMMENT.getRel()).description("link to create comment")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header"),
                                headerWithName(HttpHeaders.ACCEPT).description("accept header")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        responseFields(
                                fieldWithPath("_embedded.commentResponseDtoList.[*].id").description("Identifier of Comment"),
                                fieldWithPath("_embedded.commentResponseDtoList.[*].content").description("Content of Comment"),
                                fieldWithPath("_embedded.commentResponseDtoList.[*].commentType").description("CommentType of Comment"),
                                fieldWithPath("_embedded.commentResponseDtoList.[*].writer").description("Writer of Comment"),
                                fieldWithPath("_embedded.commentResponseDtoList.[*].writer.id").description("Writer's Identifier"),
                                fieldWithPath("_embedded.commentResponseDtoList.[*].writer.createdAt").description("Writer's Created Date"),
                                fieldWithPath("_embedded.commentResponseDtoList.[*].writer.modifiedAt").description("Writer's Modified Date"),
                                fieldWithPath("_embedded.commentResponseDtoList.[*].writer.email").description("Writer's Email"),
                                fieldWithPath("_embedded.commentResponseDtoList.[*].writer.name").description("Writer's Name"),
                                fieldWithPath("_embedded.commentResponseDtoList.[*].writer.address").description("Writer's Address"),
                                fieldWithPath("_embedded.commentResponseDtoList.[*].writer.balance").description("Writer's Balance"),
                                fieldWithPath("_embedded.commentResponseDtoList.[*].writer.accountRole").description("Writer's Role"),
                                fieldWithPath("_embedded.commentResponseDtoList.[*].product").description("Product of New Comment"),
                                fieldWithPath("_embedded.commentResponseDtoList.[*].product.id").description("Product's Identifier"),
                                fieldWithPath("_embedded.commentResponseDtoList.[*].product.createdAt").description("Product's Created Date"),
                                fieldWithPath("_embedded.commentResponseDtoList.[*].product.modifiedAt").description("Product's Modified Date"),
                                fieldWithPath("_embedded.commentResponseDtoList.[*].product.name").description("Product's Name"),
                                fieldWithPath("_embedded.commentResponseDtoList.[*].product.size").description("Product's Size"),
                                fieldWithPath("_embedded.commentResponseDtoList.[*].product.image").description("Product's Image"),
                                fieldWithPath("_embedded.commentResponseDtoList.[*].product.price").description("Product's Price"),
                                fieldWithPath("_embedded.commentResponseDtoList.[*].product.stock").description("Product's Stock"),
                                fieldWithPath("_embedded.commentResponseDtoList.[*].product.category").description("Product's Category"),
                                fieldWithPath("_embedded.commentResponseDtoList.[*].product.orders").description("Ordered List of Product"),
                                fieldWithPath("_embedded.commentResponseDtoList.[*].product.seller").description("Seller of Product"),
                                fieldWithPath("_embedded.commentResponseDtoList.[*].product.seller.id").description("Product Seller's Identifier"),
                                fieldWithPath("_embedded.commentResponseDtoList.[*].product.seller.createdAt").description("Product Seller's Created Date"),
                                fieldWithPath("_embedded.commentResponseDtoList.[*].product.seller.modifiedAt").description("Product Seller's Modified Date"),
                                fieldWithPath("_embedded.commentResponseDtoList.[*].product.seller.email").description("Product Seller's Email"),
                                fieldWithPath("_embedded.commentResponseDtoList.[*].product.seller.name").description("Product Seller's Name"),
                                fieldWithPath("_embedded.commentResponseDtoList.[*].product.seller.address").description("Product Seller's Address"),
                                fieldWithPath("_embedded.commentResponseDtoList.[*].product.seller.balance").description("Product Seller's Balance"),
                                fieldWithPath("_embedded.commentResponseDtoList.[*].product.seller.accountRole").description("Product Seller's Role"),
                                fieldWithPath("_embedded.commentResponseDtoList.[*].product.comment").description("Product's Comment"),
                                fieldWithPath("_embedded.commentResponseDtoList.[*].product.comment.[*].id").description("Product Comment's Identifier"),
                                fieldWithPath("_embedded.commentResponseDtoList.[*].product.comment.[*].createdAt").description("Product Comment's Created Date"),
                                fieldWithPath("_embedded.commentResponseDtoList.[*].product.comment.[*].modifiedAt").description("Product Comment's Modified Date"),
                                fieldWithPath("_embedded.commentResponseDtoList.[*].product.comment.[*].content").description("Product Comment's Content"),
                                fieldWithPath("_embedded.commentResponseDtoList.[*].product.comment.[*].commentType").description("Product Comment's CommentType"),
                                fieldWithPath("_embedded.commentResponseDtoList.[*].product.comment.[*].writer").description("Product Comment's Writer"),
                                fieldWithPath("_embedded.commentResponseDtoList.[*].product.comment.[*].product").description("Product Comment's Product"),
                                fieldWithPath("_embedded.commentResponseDtoList.[*]._links.self.href").description("link to self"),
                                fieldWithPath("_embedded.commentResponseDtoList.[*]._links.profile.href").description("link to profile"),
                                fieldWithPath("_embedded.commentResponseDtoList.[*]._links.load-product.href").description("link to load product"),
                                fieldWithPath("_embedded.commentResponseDtoList.[*]._links.load-account.href").description("link to load account"),
                                fieldWithPath("_embedded.commentResponseDtoList.[*]._links.create-comment.href").description("link to create comment"),
                                fieldWithPath("_embedded.commentResponseDtoList.[*]._links.modify-comment.href").description("link to modify comment"),
                                fieldWithPath("_embedded.commentResponseDtoList.[*]._links.delete-comment.href").description("link to delete comment"),
                                fieldWithPath("_links.self.href").description("link to self"),
                                fieldWithPath("_links.profile.href").description("link to profile"),
                                fieldWithPath("_links.create-comment.href").description("link to create comment"),
                                fieldWithPath("page.size").description("size of page"),
                                fieldWithPath("page.totalElements").description("total elements of page"),
                                fieldWithPath("page.totalPages").description("total number of page"),
                                fieldWithPath("page.number").description("number of page")
                        )
                ))
        ;
    }

    @DisplayName("댓글 수정 시 유효성 검사 실패 -> 400 BAD_REQUEST")
    @ParameterizedTest(name = "{index} {displayName} message={0}")
    @CsvSource({", EPILOGUE", " , EPILOGUE"})
    void modify_comment_invalid_400(String content, CommentType commentType) throws Exception  {
        String jwt = createAccountAndJwt(createAccountRequestDto());
        ProductResponseDto productResponseDto = getProductResponseDto(jwt);
        jwt = createAccountAndJwt(createAnotherAccountRequestDto());
        CommentRequestDto commentRequestDto = createCommentRequestDto(productResponseDto.getId());

        ResultActions actions = this.mockMvc.perform(post(COMMENT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .content(objectMapper.writeValueAsString(commentRequestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("content").exists())
                .andExpect(jsonPath("writer").exists())
                .andExpect(jsonPath("product").exists());

        String contentAsString = actions.andReturn().getResponse().getContentAsString();
        CommentResponseDto responseDto = objectMapper.readValue(contentAsString, CommentResponseDto.class);
        CommentModifyRequestDto requestDto = CommentModifyRequestDto.builder()
                .content(content)
                .commentType(commentType)
                .build();

        this.mockMvc.perform(put(COMMENT_URL + responseDto.getId())
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .content(objectMapper.writeValueAsString(requestDto)))
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

    @DisplayName("DB에 없는 댓글 수정 시 -> 404 NOT_FOUND")
    @Test
    void modify_comment_not_found_404() throws Exception    {
        String jwt = createAccountAndJwt(createAccountRequestDto());
        ProductResponseDto productResponseDto = getProductResponseDto(jwt);
        jwt = createAccountAndJwt(createAnotherAccountRequestDto());
        CommentRequestDto commentRequestDto = createCommentRequestDto(productResponseDto.getId());

        this.mockMvc.perform(post(COMMENT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .content(objectMapper.writeValueAsString(commentRequestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("content").exists())
                .andExpect(jsonPath("writer").exists())
                .andExpect(jsonPath("product").exists())
        ;

        CommentModifyRequestDto requestDto = createCommentModifyRequestDto();

        this.mockMvc.perform(put(COMMENT_URL + "-1")
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .content(objectMapper.writeValueAsString(requestDto)))
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

    @DisplayName("댓글 수정 시도한 유저의 ID와 댓글을 쓴 유저의 ID가 다를 때 -> 400 BAD_REQUEST")
    @Test
    void modify_comment_account_id_not_match_400() throws Exception {
        String jwt = createAccountAndJwt(createAccountRequestDto());
        ProductResponseDto productResponseDto = getProductResponseDto(jwt);
        String anotherJwt = createAccountAndJwt(createAnotherAccountRequestDto());
        CommentRequestDto commentRequestDto = createCommentRequestDto(productResponseDto.getId());

        ResultActions actions = this.mockMvc.perform(post(COMMENT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, anotherJwt)
                .content(objectMapper.writeValueAsString(commentRequestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("content").exists())
                .andExpect(jsonPath("writer").exists())
                .andExpect(jsonPath("product").exists());

        String contentAsString = actions.andReturn().getResponse().getContentAsString();
        CommentResponseDto responseDto = objectMapper.readValue(contentAsString, CommentResponseDto.class);
        CommentModifyRequestDto requestDto = createCommentModifyRequestDto();

        this.mockMvc.perform(put(COMMENT_URL + responseDto.getId())
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .content(objectMapper.writeValueAsString(requestDto)))
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

    @DisplayName("정상적으로 댓글 수정 -> 200 OK")
    @Test
    void modify_comment_200() throws Exception  {
        String jwt = createAccountAndJwt(createAccountRequestDto());
        ProductResponseDto productResponseDto = getProductResponseDto(jwt);
        jwt = createAccountAndJwt(createAnotherAccountRequestDto());
        CommentRequestDto commentRequestDto = createCommentRequestDto(productResponseDto.getId());

        ResultActions actions = this.mockMvc.perform(post(COMMENT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .content(objectMapper.writeValueAsString(commentRequestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("content").exists())
                .andExpect(jsonPath("writer").exists())
                .andExpect(jsonPath("product").exists());

        String contentAsString = actions.andReturn().getResponse().getContentAsString();
        CommentResponseDto responseDto = objectMapper.readValue(contentAsString, CommentResponseDto.class);
        CommentModifyRequestDto requestDto = createCommentModifyRequestDto();

        this.mockMvc.perform(put(COMMENT_URL + responseDto.getId())
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("content").exists())
                .andExpect(jsonPath("writer").exists())
                .andExpect(jsonPath("product").exists())
                .andDo(document(MODIFY_COMMENT.getRel(),
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel(PROFILE.getRel()).description("link to profile"),
                                linkWithRel(LOAD_PRODUCT.getRel()).description("link to load product"),
                                linkWithRel(LOAD_ACCOUNT.getRel()).description("link to load account"),
                                linkWithRel(LOAD_COMMENT.getRel()).description("link to load comment"),
                                linkWithRel(LOAD_ALL_COMMENTS.getRel()).description("link to load all comments"),
                                linkWithRel(CREATE_COMMENT.getRel()).description("link to create comment"),
                                linkWithRel(DELETE_COMMENT.getRel()).description("link to delete comment")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header"),
                                headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                headerWithName(HttpHeaders.AUTHORIZATION).description("authorization header")
                        ),
                        requestFields(
                                fieldWithPath("content").description("Content of Modified Comment"),
                                fieldWithPath("commentType").description("Comment Type of Modified Comment")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        responseFields(
                                fieldWithPath("id").description("Identifier of Modified Comment"),
                                fieldWithPath("content").description("Content of Modified Comment"),
                                fieldWithPath("commentType").description("CommentType of Modified Comment"),
                                fieldWithPath("writer").description("Writer of Modified Comment"),
                                fieldWithPath("writer.id").description("Writer's Identifier"),
                                fieldWithPath("writer.createdAt").description("Writer's Created Date"),
                                fieldWithPath("writer.modifiedAt").description("Writer's Modified Date"),
                                fieldWithPath("writer.email").description("Writer's Email"),
                                fieldWithPath("writer.name").description("Writer's Name"),
                                fieldWithPath("writer.address").description("Writer's Address"),
                                fieldWithPath("writer.balance").description("Writer's Balance"),
                                fieldWithPath("writer.accountRole").description("Writer's Role"),
                                fieldWithPath("product").description("Product of New Comment"),
                                fieldWithPath("product.id").description("Product's Identifier"),
                                fieldWithPath("product.createdAt").description("Product's Created Date"),
                                fieldWithPath("product.modifiedAt").description("Product's Modified Date"),
                                fieldWithPath("product.name").description("Product's Name"),
                                fieldWithPath("product.size").description("Product's Size"),
                                fieldWithPath("product.image").description("Product's Image"),
                                fieldWithPath("product.price").description("Product's Price"),
                                fieldWithPath("product.stock").description("Product's Stock"),
                                fieldWithPath("product.category").description("Product's Category"),
                                fieldWithPath("product.orders").description("Ordered List of Product"),
                                fieldWithPath("product.seller").description("Seller of Product"),
                                fieldWithPath("product.seller.id").description("Product Seller's Identifier"),
                                fieldWithPath("product.seller.createdAt").description("Product Seller's Created Date"),
                                fieldWithPath("product.seller.modifiedAt").description("Product Seller's Modified Date"),
                                fieldWithPath("product.seller.email").description("Product Seller's Email"),
                                fieldWithPath("product.seller.name").description("Product Seller's Name"),
                                fieldWithPath("product.seller.address").description("Product Seller's Address"),
                                fieldWithPath("product.seller.balance").description("Product Seller's Balance"),
                                fieldWithPath("product.seller.accountRole").description("Product Seller's Role"),
                                fieldWithPath("product.comment").description("Product's Comment"),
                                fieldWithPath("product.comment.[*].id").description("Product Comment's Identifier"),
                                fieldWithPath("product.comment.[*].createdAt").description("Product Comment's Created Date"),
                                fieldWithPath("product.comment.[*].modifiedAt").description("Product Comment's Modified Date"),
                                fieldWithPath("product.comment.[*].content").description("Product Comment's Content"),
                                fieldWithPath("product.comment.[*].commentType").description("Product Comment's CommentType"),
                                fieldWithPath("product.comment.[*].writer").description("Product Comment's Writer"),
                                fieldWithPath("product.comment.[*].product").description("Product Comment's Product"),
                                fieldWithPath("_links.self.href").description("link to self"),
                                fieldWithPath("_links.profile.href").description("link to profile"),
                                fieldWithPath("_links.load-product.href").description("link to load product"),
                                fieldWithPath("_links.load-account.href").description("link to load account"),
                                fieldWithPath("_links.load-comment.href").description("link to load comment"),
                                fieldWithPath("_links.load-all-comments.href").description("link to load all comments"),
                                fieldWithPath("_links.create-comment.href").description("link to create comment"),
                                fieldWithPath("_links.delete-comment.href").description("link to delete comment")
                        )
                ))
        ;
    }

    @DisplayName("DB에 없는 댓글 삭제 -> 404 NOT_FOUND")
    @Test
    void delete_comment_not_found_404() throws Exception  {
        String jwt = createAccountAndJwt(createAccountRequestDto());
        ProductResponseDto productResponseDto = getProductResponseDto(jwt);
        jwt = createAccountAndJwt(createAnotherAccountRequestDto());
        CommentRequestDto commentRequestDto = createCommentRequestDto(productResponseDto.getId());

        this.mockMvc.perform(post(COMMENT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .content(objectMapper.writeValueAsString(commentRequestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("content").exists())
                .andExpect(jsonPath("writer").exists())
                .andExpect(jsonPath("product").exists());

        this.mockMvc.perform(delete(COMMENT_URL + "-1")
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, jwt))
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

    @DisplayName("댓글 삭제 시도한 유저의 ID와 댓글을 쓴 유저의 ID가 다를 때 -> 400 BAD_REQUEST")
    @Test
    void delete_comment_account_id_not_match_400() throws Exception {
        String jwt = createAccountAndJwt(createAccountRequestDto());
        ProductResponseDto productResponseDto = getProductResponseDto(jwt);
        String anotherJwt = createAccountAndJwt(createAnotherAccountRequestDto());
        CommentRequestDto commentRequestDto = createCommentRequestDto(productResponseDto.getId());

        ResultActions actions = this.mockMvc.perform(post(COMMENT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, anotherJwt)
                .content(objectMapper.writeValueAsString(commentRequestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("content").exists())
                .andExpect(jsonPath("writer").exists())
                .andExpect(jsonPath("product").exists());

        String contentAsString = actions.andReturn().getResponse().getContentAsString();
        CommentResponseDto responseDto = objectMapper.readValue(contentAsString, CommentResponseDto.class);

        this.mockMvc.perform(delete(COMMENT_URL + responseDto.getId())
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, jwt))
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

    @DisplayName("정상적으로 댓글 삭제 -> 200 OK")
    @Test
    void delete_comment_200() throws Exception {
        String jwt = createAccountAndJwt(createAccountRequestDto());
        ProductResponseDto productResponseDto = getProductResponseDto(jwt);
        jwt = createAccountAndJwt(createAnotherAccountRequestDto());
        CommentRequestDto commentRequestDto = createCommentRequestDto(productResponseDto.getId());

        ResultActions actions = this.mockMvc.perform(post(COMMENT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .content(objectMapper.writeValueAsString(commentRequestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("content").exists())
                .andExpect(jsonPath("writer").exists())
                .andExpect(jsonPath("product").exists());

        String contentAsString = actions.andReturn().getResponse().getContentAsString();
        CommentResponseDto responseDto = objectMapper.readValue(contentAsString, CommentResponseDto.class);

        this.mockMvc.perform(delete(COMMENT_URL + responseDto.getId())
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, jwt))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("content").exists())
                .andExpect(jsonPath("writer").exists())
                .andExpect(jsonPath("product").exists())
                .andDo(document(DELETE_COMMENT.getRel(),
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel(PROFILE.getRel()).description("link to profile"),
                                linkWithRel(LOAD_PRODUCT.getRel()).description("link to load product"),
                                linkWithRel(LOAD_ACCOUNT.getRel()).description("link to load account"),
                                linkWithRel(LOAD_COMMENT.getRel()).description("link to load comment"),
                                linkWithRel(LOAD_ALL_COMMENTS.getRel()).description("link to load all comments"),
                                linkWithRel(CREATE_COMMENT.getRel()).description("link to create comment"),
                                linkWithRel(MODIFY_COMMENT.getRel()).description("link to modify comment")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header"),
                                headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                headerWithName(HttpHeaders.AUTHORIZATION).description("authorization header")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        responseFields(
                                fieldWithPath("id").description("Identifier of Deleted Comment"),
                                fieldWithPath("content").description("Content of Deleted Comment"),
                                fieldWithPath("commentType").description("CommentType of Deleted Comment"),
                                fieldWithPath("writer").description("Writer of Deleted Comment"),
                                fieldWithPath("writer.id").description("Writer's Identifier"),
                                fieldWithPath("writer.createdAt").description("Writer's Created Date"),
                                fieldWithPath("writer.modifiedAt").description("Writer's Modified Date"),
                                fieldWithPath("writer.email").description("Writer's Email"),
                                fieldWithPath("writer.name").description("Writer's Name"),
                                fieldWithPath("writer.address").description("Writer's Address"),
                                fieldWithPath("writer.balance").description("Writer's Balance"),
                                fieldWithPath("writer.accountRole").description("Writer's Role"),
                                fieldWithPath("product").description("Product of New Comment"),
                                fieldWithPath("product.id").description("Product's Identifier"),
                                fieldWithPath("product.createdAt").description("Product's Created Date"),
                                fieldWithPath("product.modifiedAt").description("Product's Modified Date"),
                                fieldWithPath("product.name").description("Product's Name"),
                                fieldWithPath("product.size").description("Product's Size"),
                                fieldWithPath("product.image").description("Product's Image"),
                                fieldWithPath("product.price").description("Product's Price"),
                                fieldWithPath("product.stock").description("Product's Stock"),
                                fieldWithPath("product.category").description("Product's Category"),
                                fieldWithPath("product.orders").description("Ordered List of Product"),
                                fieldWithPath("product.seller").description("Seller of Product"),
                                fieldWithPath("product.seller.id").description("Product Seller's Identifier"),
                                fieldWithPath("product.seller.createdAt").description("Product Seller's Created Date"),
                                fieldWithPath("product.seller.modifiedAt").description("Product Seller's Modified Date"),
                                fieldWithPath("product.seller.email").description("Product Seller's Email"),
                                fieldWithPath("product.seller.name").description("Product Seller's Name"),
                                fieldWithPath("product.seller.address").description("Product Seller's Address"),
                                fieldWithPath("product.seller.balance").description("Product Seller's Balance"),
                                fieldWithPath("product.seller.accountRole").description("Product Seller's Role"),
                                fieldWithPath("product.comment").description("Product's Comment"),
                                fieldWithPath("product.comment.[*].id").description("Product Comment's Identifier"),
                                fieldWithPath("product.comment.[*].createdAt").description("Product Comment's Created Date"),
                                fieldWithPath("product.comment.[*].modifiedAt").description("Product Comment's Modified Date"),
                                fieldWithPath("product.comment.[*].content").description("Product Comment's Content"),
                                fieldWithPath("product.comment.[*].commentType").description("Product Comment's CommentType"),
                                fieldWithPath("product.comment.[*].writer").description("Product Comment's Writer"),
                                fieldWithPath("product.comment.[*].product").description("Product Comment's Product"),
                                fieldWithPath("_links.self.href").description("link to self"),
                                fieldWithPath("_links.profile.href").description("link to profile"),
                                fieldWithPath("_links.load-product.href").description("link to load product"),
                                fieldWithPath("_links.load-account.href").description("link to load account"),
                                fieldWithPath("_links.load-comment.href").description("link to load comment"),
                                fieldWithPath("_links.load-all-comments.href").description("link to load all comments"),
                                fieldWithPath("_links.create-comment.href").description("link to create comment"),
                                fieldWithPath("_links.modify-comment.href").description("link to modify comment")
                        )
                ))
        ;
    }

    private String createAccountAndJwt(AccountRequestDto requestDto) throws Exception {
        ResultActions actions = this.mockMvc.perform(post(ACCOUNT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isCreated());

        return generateToken(actions);
    }

    private ProductResponseDto getProductResponseDto(String jwt) throws Exception {
        ProductRequestDto productRequestDto = createProductRequestDto();
        ResultActions productActions = this.mockMvc.perform(post(PRODUCT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .content(objectMapper.writeValueAsString(productRequestDto)))
                .andDo(print())
                .andExpect(status().isCreated());

        String productContentAsString = productActions.andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(productContentAsString, ProductResponseDto.class);
    }

    private void saveCustomComment(String jwt, Long productId) {
        List<String> contents = Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H", "I", "J");
        List<CommentType> commentTypes = Arrays.asList(CommentType.EPILOGUE, CommentType.QUESTION);
        IntStream.rangeClosed(1, 10).forEach(i -> {
            CommentRequestDto requestDto = CommentRequestDto.builder()
                    .content(contents.get(i - 1))
                    .commentType(commentTypes.get((i - 1) % 2))
                    .productId(productId)
                    .build();

            try {
                this.mockMvc.perform(post(COMMENT_URL)
                        .accept(MediaTypes.HAL_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, jwt)
                        .content(objectMapper.writeValueAsString(requestDto)))
                        .andDo(print())
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("id").exists())
                        .andExpect(jsonPath("content").exists())
                        .andExpect(jsonPath("writer").exists())
                        .andExpect(jsonPath("product").exists());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static Stream<Arguments> validSaveComment() {
        return Stream.of(
                Arguments.of("", CommentType.EPILOGUE, 1L),
                Arguments.of(" ", CommentType.EPILOGUE, 1L),
                Arguments.of("new comment", CommentType.QUESTION, null)
        );
    }
}
