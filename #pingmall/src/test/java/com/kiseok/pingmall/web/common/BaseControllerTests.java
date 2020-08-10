package com.kiseok.pingmall.web.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kiseok.pingmall.common.domain.comment.CommentRepository;
import com.kiseok.pingmall.common.domain.comment.CommentType;
import com.kiseok.pingmall.common.domain.order.OrdersRepository;
import com.kiseok.pingmall.common.domain.verification.VerificationRepository;
import com.kiseok.pingmall.common.properties.AppProperties;
import com.kiseok.pingmall.common.config.jwt.JwtProvider;
import com.kiseok.pingmall.common.domain.account.AccountRepository;
import com.kiseok.pingmall.common.domain.product.ProductCategory;
import com.kiseok.pingmall.common.domain.product.ProductRepository;
import com.kiseok.pingmall.web.dto.LoginRequestDto;
import com.kiseok.pingmall.web.dto.account.AccountModifyRequestDto;
import com.kiseok.pingmall.web.dto.account.AccountRequestDto;
import com.kiseok.pingmall.web.dto.account.AccountResponseDto;
import com.kiseok.pingmall.web.dto.comment.CommentModifyRequestDto;
import com.kiseok.pingmall.web.dto.comment.CommentRequestDto;
import com.kiseok.pingmall.web.dto.find.FindPasswordRequestDto;
import com.kiseok.pingmall.web.dto.jwt.JwtRequestDto;
import com.kiseok.pingmall.web.dto.order.OrdersRequestDto;
import com.kiseok.pingmall.web.dto.product.ProductRequestDto;
import com.kiseok.pingmall.web.dto.verification.VerificationCodeRequestDto;
import com.kiseok.pingmall.web.dto.verification.VerificationEmailRequestDto;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import java.util.UUID;
import static com.kiseok.pingmall.common.config.jwt.JwtConstants.PREFIX;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(RestDocsConfiguration.class)
public class BaseControllerTests {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected AccountRepository accountRepository;

    @Autowired
    protected ProductRepository productRepository;

    @Autowired
    protected OrdersRepository ordersRepository;

    @Autowired
    protected CommentRepository commentRepository;

    @Autowired
    protected VerificationRepository verificationRepository;

    @Autowired
    protected JwtProvider jwtProvider;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected ModelMapper modelMapper;

    @Autowired
    protected AppProperties appProperties;

    protected final String ACCOUNT_URL = "/api/accounts/";
    protected final String LOGIN_URL = "/api/login";
    protected final String PRODUCT_URL = "/api/products/";
    protected final String IMAGE_URL = "/api/images/";
    protected final String ORDERS_URL = "/api/orders/";
    protected final String FIND_EMAIL_URL = "/api/find/email";
    protected final String FIND_PASSWORD_URL = "/api/find/password";
    protected final String COMMENT_URL = "/api/comments/";
    protected final String VERIFICATION_URL = "/api/verifications";
    protected final String ANOTHER = "another_";

    protected String generateToken(ResultActions actions) throws Exception {
        String contentAsString = actions.andReturn().getResponse().getContentAsString();
        AccountResponseDto responseDto = objectMapper.readValue(contentAsString, AccountResponseDto.class);
        JwtRequestDto jwtRequestDto = modelMapper.map(responseDto, JwtRequestDto.class);

        return PREFIX + jwtProvider.generateToken(jwtRequestDto);
    }

    protected AccountRequestDto createAccountRequestDto()   {
        return AccountRequestDto.builder()
                .email(appProperties.getTestEmail())
                .password(appProperties.getTestPassword())
                .name(appProperties.getTestName())
                .address(appProperties.getTestAddress())
                .balance(appProperties.getTestBalance())
                .build();
    }

    protected AccountRequestDto createAnotherAccountRequestDto()   {
        return AccountRequestDto.builder()
                .email(ANOTHER + appProperties.getTestEmail())
                .password(ANOTHER + appProperties.getTestPassword())
                .name(ANOTHER + appProperties.getTestName())
                .address(ANOTHER + appProperties.getTestAddress())
                .balance(appProperties.getTestBalance())
                .build();
    }

    protected AccountModifyRequestDto createAccountModifyRequestDto()   {
        return AccountModifyRequestDto.builder()
                .password(appProperties.getTestModifiedPassword())
                .name(appProperties.getTestModifiedName())
                .address(appProperties.getTestModifiedAddress())
                .build();
    }

    protected LoginRequestDto createLoginRequestDto()   {
        return LoginRequestDto.builder()
                .email(appProperties.getTestEmail())
                .password(appProperties.getTestPassword())
                .build();
    }

    protected ProductRequestDto createProductRequestDto() {
        return ProductRequestDto.builder()
                .name(appProperties.getTestProductName())
                .size(appProperties.getTestSize())
                .image(appProperties.getTestImage())
                .price(appProperties.getTestPrice())
                .stock(appProperties.getTestStock())
                .category(ProductCategory.ACCESSORY)
                .build();
    }

    protected ProductRequestDto createProductRequestDto(String name, String size, Long price, ProductCategory category) {
        return ProductRequestDto.builder()
                .name(name)
                .size(size)
                .image(appProperties.getTestImage())
                .price(price)
                .stock(appProperties.getTestStock())
                .category(category)
                .build();
    }

    protected ProductRequestDto createProductModifyRequestDto() {
        return ProductRequestDto.builder()
                .name(appProperties.getTestModifiedProductName())
                .size(appProperties.getTestModifiedSize())
                .image(appProperties.getTestModifiedImage())
                .price(appProperties.getTestModifiedPrice())
                .stock(appProperties.getTestModifiedStock())
                .category(ProductCategory.TOP)
                .build();
    }

    protected OrdersRequestDto createOrdersRequestDto(Long productId) {
        return OrdersRequestDto.builder()
                .amount(appProperties.getTestAmount())
                .productId(productId)
                .build();
    }

    protected OrdersRequestDto createOrdersRequestWithParam(Long amount, Long productId) {
        return OrdersRequestDto.builder()
                .amount(amount)
                .productId(productId)
                .build();
    }

    protected FindPasswordRequestDto createFindPasswordRequestDto(String email, String name) {
        return FindPasswordRequestDto.builder()
                .email(email)
                .name(name)
                .build();
    }

    protected CommentRequestDto createCommentRequestDto(Long productId)   {
        return CommentRequestDto.builder()
                .content(appProperties.getTestContent())
                .commentType(CommentType.QUESTION)
                .productId(productId)
                .build();
    }

    protected CommentModifyRequestDto createCommentModifyRequestDto()   {
        return CommentModifyRequestDto.builder()
                .content(appProperties.getTestModifiedContent())
                .commentType(CommentType.EPILOGUE)
                .build();
    }

    protected VerificationEmailRequestDto createVerificationEmailRequestDto()   {
        return VerificationEmailRequestDto.builder()
                .email(appProperties.getMyEmail())
                .build();
    }

    protected VerificationCodeRequestDto createVerificationCodeRequestDto() {
        return VerificationCodeRequestDto.builder()
                .email(appProperties.getMyEmail())
                .verificationCode(UUID.randomUUID().toString().substring(0, 6))
                .build();
    }

}
