package com.kiseok.pingmall.web.controller;

import com.kiseok.pingmall.common.domain.comment.CommentType;
import com.kiseok.pingmall.web.common.BaseControllerTests;
import com.kiseok.pingmall.web.dto.account.AccountRequestDto;
import com.kiseok.pingmall.web.dto.comment.CommentModifyRequestDto;
import com.kiseok.pingmall.web.dto.comment.CommentRequestDto;
import com.kiseok.pingmall.web.dto.comment.CommentResponseDto;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CommentControllerTests extends BaseControllerTests {

    @AfterEach
    void tearDown() {
        commentRepository.deleteAll();
        productRepository.deleteAll();
        accountRepository.deleteAll();
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

    private static Stream<Arguments> validSaveComment() {
        return Stream.of(
                Arguments.of("", CommentType.EPILOGUE, 1L),
                Arguments.of(" ", CommentType.EPILOGUE, 1L),
                Arguments.of("new comment", CommentType.QUESTION, null)
        );
    }
}
