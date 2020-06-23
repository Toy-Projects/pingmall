package com.kiseok.pingmall.web.controller;

import com.kiseok.pingmall.common.domain.account.AccountRole;
import com.kiseok.pingmall.common.domain.product.ProductCategory;
import com.kiseok.pingmall.web.BaseControllerTests;
import com.kiseok.pingmall.web.dto.product.ProductResponseDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.ResultActions;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ImageControllerTests extends BaseControllerTests {

    @AfterEach
    void tearDown()    {
        productRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @DisplayName("DB에 없는 제품의 디폴트 이미지 업로드 시 -> 404 NOT_FOUND")
    @Test
    void save_default_image_id_null_404() throws Exception  {
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

        String token = generateToken(actions);

        this.mockMvc.perform(multipart(IMAGE_URL + "products/-1")
                .header(HttpHeaders.AUTHORIZATION, token))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("status").exists())
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("code").exists())
        ;
    }

    @DisplayName("정상적으로 디폴트 이미지 저장 -> 200 OK")
    @Test
    void save_default_image_201() throws Exception  {
        ResultActions accountActions = this.mockMvc.perform(post(ACCOUNT_URL)
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

        String token = generateToken(accountActions);

        ResultActions productActions = this.mockMvc.perform(post(PRODUCT_URL)
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

        String productAsString = productActions.andReturn().getResponse().getContentAsString();
        ProductResponseDto responseDto = objectMapper.readValue(productAsString, ProductResponseDto.class);

        ResultActions imageActions = this.mockMvc.perform(multipart(IMAGE_URL + "products/" + responseDto.getId())
                .header(HttpHeaders.AUTHORIZATION, token))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("name").value(appProperties.getTestProductName()))
                .andExpect(jsonPath("image").exists())
                .andExpect(jsonPath("size").value(appProperties.getTestSize()))
                .andExpect(jsonPath("price").value(appProperties.getTestPrice()))
                .andExpect(jsonPath("stock").value(appProperties.getTestStock()))
                .andExpect(jsonPath("category").value(ProductCategory.ACCESSORY.name()))
                .andExpect(jsonPath("registeredAt").exists())
                .andExpect(jsonPath("seller").exists())
                ;

        String imageAsString = imageActions.andReturn().getResponse().getContentAsString();
        ProductResponseDto responseDto2 = objectMapper.readValue(imageAsString, ProductResponseDto.class);

        assertThat(responseDto2.getImage().contains(appProperties.getTestProductName()));
        assertThat(responseDto2.getImage().contains(appProperties.getTestEmail()));
        assertThat(responseDto2.getImage().contains("DefaultProduct.jpg"));
    }

    @DisplayName("DB에 없는 제품의 이미지 업로드 시 -> 404 NOT_FOUND")
    @Test
    void save_image_id_null_404() throws Exception  {
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

        String token = generateToken(actions);
        MockMultipartFile file =
                new MockMultipartFile("file","test.png" , MediaType.IMAGE_PNG_VALUE, "test image".getBytes());

        this.mockMvc.perform(multipart(IMAGE_URL + "products/-1")
                .file(file)
                .header(HttpHeaders.AUTHORIZATION, token))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("status").exists())
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("code").exists())
        ;
    }

    @DisplayName("파일 이름에 '..' 이 있을 경우 -> 400 BAD_REQUEST")
    @Test
    void save_image_invalid_400() throws Exception  {
        ResultActions accountActions = this.mockMvc.perform(post(ACCOUNT_URL)
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

        String token = generateToken(accountActions);

        ResultActions productActions = this.mockMvc.perform(post(PRODUCT_URL)
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

        String contentAsString = productActions.andReturn().getResponse().getContentAsString();
        ProductResponseDto responseDto = objectMapper.readValue(contentAsString, ProductResponseDto.class);
        MockMultipartFile file =
                new MockMultipartFile("file","..test.png" , MediaType.IMAGE_PNG_VALUE, "test image".getBytes());

        this.mockMvc.perform(multipart(IMAGE_URL + "products/" + responseDto.getId())
                .file(file)
                .header(HttpHeaders.AUTHORIZATION, token))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").exists())
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("code").exists())
        ;
    }

    @DisplayName("이미지가 아닌 파일 저장 시 -> 400 BAD_REQUEST")
    @ParameterizedTest(name = "{index} {displayName} message={0}")
    @ValueSource(strings = {".txt", ".gif", ".hwp", ".ppt"})
    void save_image_not_support_400(String extension) throws Exception  {
        ResultActions accountActions = this.mockMvc.perform(post(ACCOUNT_URL)
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

        String token = generateToken(accountActions);

        ResultActions productActions = this.mockMvc.perform(post(PRODUCT_URL)
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

        String contentAsString = productActions.andReturn().getResponse().getContentAsString();
        ProductResponseDto responseDto = objectMapper.readValue(contentAsString, ProductResponseDto.class);
        MockMultipartFile file =
                new MockMultipartFile("file","test" + extension , MediaType.IMAGE_PNG_VALUE, "test text".getBytes());

        this.mockMvc.perform(multipart(IMAGE_URL + "products/" + responseDto.getId())
                .file(file)
                .header(HttpHeaders.AUTHORIZATION, token))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").exists())
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("code").exists())
        ;
    }

    @DisplayName("정상적으로 이미지 저장 -> 200 OK")
    @ParameterizedTest(name = "{index} {displayName} message={0}")
    @ValueSource(strings = {".png", ".jpg", ".jpeg"})
    void save_image_201(String extension) throws Exception  {
        ResultActions accountActions = this.mockMvc.perform(post(ACCOUNT_URL)
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

        String token = generateToken(accountActions);

        ResultActions productActions = this.mockMvc.perform(post(PRODUCT_URL)
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

        String productAsString = productActions.andReturn().getResponse().getContentAsString();
        ProductResponseDto responseDto = objectMapper.readValue(productAsString, ProductResponseDto.class);
        MockMultipartFile file =
                new MockMultipartFile("file","test" + extension , MediaType.IMAGE_PNG_VALUE, "test image".getBytes());

        ResultActions imageActions = this.mockMvc.perform(multipart(IMAGE_URL + "products/" + responseDto.getId())
                .file(file)
                .header(HttpHeaders.AUTHORIZATION, token))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("name").value(appProperties.getTestProductName()))
                .andExpect(jsonPath("image").exists())
                .andExpect(jsonPath("size").value(appProperties.getTestSize()))
                .andExpect(jsonPath("price").value(appProperties.getTestPrice()))
                .andExpect(jsonPath("stock").value(appProperties.getTestStock()))
                .andExpect(jsonPath("category").value(ProductCategory.ACCESSORY.name()))
                .andExpect(jsonPath("registeredAt").exists())
                .andExpect(jsonPath("seller").exists())
        ;

        String imageAsString = imageActions.andReturn().getResponse().getContentAsString();
        ProductResponseDto responseDto2 = objectMapper.readValue(imageAsString, ProductResponseDto.class);

        assertThat(responseDto2.getImage().contains(appProperties.getTestProductName()));
        assertThat(responseDto2.getImage().contains(appProperties.getTestEmail()));
    }
}