package com.kiseok.pingmall.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kiseok.pingmall.common.config.jwt.JwtProvider;
import com.kiseok.pingmall.common.domain.account.Account;
import com.kiseok.pingmall.common.domain.account.AccountRepository;
import com.kiseok.pingmall.web.dto.account.AccountModifyRequestDto;
import com.kiseok.pingmall.web.dto.account.AccountRequestDto;
import com.kiseok.pingmall.web.dto.account.AccountResponseDto;
import com.kiseok.pingmall.web.dto.jwt.JwtRequestDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import java.util.List;
import java.util.stream.Stream;
import static com.kiseok.pingmall.common.config.jwt.JwtConstants.PREFIX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    private final String ACCOUNT_URL = "/api/accounts/";

    @AfterEach
    void deleteAll()    {
        this.accountRepository.deleteAll();
    }

    @DisplayName("유저 생성 시 유효성 검사 실패 -> 400 BAD_REQUEST")
    @ParameterizedTest(name = "{index} {displayName} message={0}")
    @MethodSource("validSaveAccount")
    void save_account_invalid_400(String email, String password, String name, String address) throws Exception    {
        AccountRequestDto requestDto = AccountRequestDto.builder()
                .email(email)
                .password(password)
                .name(name)
                .address(address)
                .build();

        this.mockMvc.perform(post(ACCOUNT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("[*].code").exists())
                .andExpect(jsonPath("[*].defaultMessage").exists())
                .andExpect(jsonPath("[*].rejectedValue").exists())
                .andExpect(jsonPath("[*].field").exists())
                .andExpect(jsonPath("[*].objectName").exists())

        ;
    }

    @DisplayName("유저 생성 시 중복 -> 400 BAD_REQUEST")
    @Test
    void save_account_duplicate_400() throws Exception  {
        String email = "test@email.com";
        String password = "testPassword";
        String name = "testName";
        String address = "testAddress";

        AccountRequestDto requestDto = AccountRequestDto.builder()
                .email(email)
                .password(password)
                .name(name)
                .address(address)
                .build();

        this.mockMvc.perform(post(ACCOUNT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").exists())
                .andExpect(jsonPath("password").doesNotExist())
                .andExpect(jsonPath("address").exists())
                .andExpect(jsonPath("accountRole").exists())
                .andExpect(jsonPath("createdAt").exists())
        ;

        this.mockMvc.perform(post(ACCOUNT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @DisplayName("정상적으로 유저 생성 -> 201 CREATED")
    @Test
    void save_account_201() throws Exception  {
        String email = "test@email.com";
        String password = "testPassword";
        String name = "testName";
        String address = "testAddress";

        AccountRequestDto requestDto = AccountRequestDto.builder()
                .email(email)
                .password(password)
                .name(name)
                .address(address)
                .build();

        this.mockMvc.perform(post(ACCOUNT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").value(requestDto.getEmail()))
                .andExpect(jsonPath("password").doesNotExist())
                .andExpect(jsonPath("address").exists())
                .andExpect(jsonPath("accountRole").exists())
                .andExpect(jsonPath("createdAt").exists())
        ;

        List<Account> accountList = accountRepository.findAll();
        assertEquals(email, accountList.get(0).getEmail());
        assertEquals(name, accountList.get(0).getName());
        assertEquals(address, accountList.get(0).getAddress());
    }

    @DisplayName("디비에 없는 유저 불러오기 -> 404 NOT_FOUND")
    @Test
    void loadAccount_not_exist_404() throws Exception {
        AccountRequestDto requestDto = AccountRequestDto.builder()
                .email("test@email.com")
                .password("testPW123!")
                .name("testName")
                .address("testAddress")
                .build();

        ResultActions actions = this.mockMvc.perform(post(ACCOUNT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").value(requestDto.getEmail()))
                .andExpect(jsonPath("password").doesNotExist())
                .andExpect(jsonPath("address").exists())
                .andExpect(jsonPath("accountRole").exists())
                .andExpect(jsonPath("createdAt").exists());

        String token = generateToken(actions);

        this.mockMvc.perform(get(ACCOUNT_URL + "-1")
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, token))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @DisplayName("정상적으로 유저 불러오기 -> 200 OK")
    @Test
    void load_account_200() throws Exception {
        AccountRequestDto requestDto = AccountRequestDto.builder()
                .email("test@email.com")
                .password("testPW123!")
                .name("testName")
                .address("testAddress")
                .build();

        ResultActions actions = this.mockMvc.perform(post(ACCOUNT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").value(requestDto.getEmail()))
                .andExpect(jsonPath("password").doesNotExist())
                .andExpect(jsonPath("address").exists())
                .andExpect(jsonPath("accountRole").exists())
                .andExpect(jsonPath("createdAt").exists());

        String contentAsString = actions.andReturn().getResponse().getContentAsString();
        AccountResponseDto responseDto = objectMapper.readValue(contentAsString, AccountResponseDto.class);
        String token = generateToken(actions);

        this.mockMvc.perform(get(ACCOUNT_URL + responseDto.getId())
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").value(responseDto.getEmail()))
                .andExpect(jsonPath("password").doesNotExist())
                .andExpect(jsonPath("name").value(responseDto.getName()))
                .andExpect(jsonPath("address").value(responseDto.getAddress()))
                .andExpect(jsonPath("accountRole").value(responseDto.getAccountRole().name()))
                .andExpect(jsonPath("createdAt").exists())
        ;
    }

    @DisplayName("유저 수정 유효성 검사 -> 400 BAD_REQUEST")
    @ParameterizedTest(name = "{index} {displayName} message={0}")
    @MethodSource("validModifyAccount")
    void modify_account_invalid_400(String password, String name, String address) throws Exception  {
        AccountRequestDto requestDto = AccountRequestDto.builder()
                .email("test@email.com")
                .password("testPW123!")
                .name("testName")
                .address("testAddress")
                .build();

        ResultActions actions = this.mockMvc.perform(post(ACCOUNT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").value(requestDto.getEmail()))
                .andExpect(jsonPath("password").doesNotExist())
                .andExpect(jsonPath("address").exists())
                .andExpect(jsonPath("accountRole").exists())
                .andExpect(jsonPath("createdAt").exists());

        String contentAsString = actions.andReturn().getResponse().getContentAsString();
        AccountResponseDto responseDto = objectMapper.readValue(contentAsString, AccountResponseDto.class);
        String token = generateToken(actions);

        AccountModifyRequestDto modifyRequestDto = AccountModifyRequestDto.builder()
                .password(password)
                .name(name)
                .address(address)
                .build();

        this.mockMvc.perform(put(ACCOUNT_URL + responseDto.getId())
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(modifyRequestDto))
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

    @DisplayName("디비에 없는 유저 수정 -> 404 NOT_FOUND")
    @Test
    void modify_account_not_exist_404() throws Exception    {
        AccountRequestDto requestDto = AccountRequestDto.builder()
                .email("test@email.com")
                .password("testPW123!")
                .name("testName")
                .address("testAddress")
                .build();

        ResultActions actions = this.mockMvc.perform(post(ACCOUNT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").value(requestDto.getEmail()))
                .andExpect(jsonPath("password").doesNotExist())
                .andExpect(jsonPath("address").exists())
                .andExpect(jsonPath("accountRole").exists())
                .andExpect(jsonPath("createdAt").exists());

        String token = generateToken(actions);
        AccountModifyRequestDto modifyRequestDto = AccountModifyRequestDto.builder()
                .password("modifiedPassword")
                .name("modifiedName")
                .address("modifiedAddress")
                .build();

        this.mockMvc.perform(put(ACCOUNT_URL + "-1")
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(modifyRequestDto))
                .header(HttpHeaders.AUTHORIZATION, token))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    // TODO 정상적으로 유저 수정
    @DisplayName("정상적으로 유저 수정 -> 200 OK")
    @Test
    void modify_account_200() throws Exception  {
        AccountRequestDto requestDto = AccountRequestDto.builder()
                .email("test@email.com")
                .password("testPW123!")
                .name("testName")
                .address("testAddress")
                .build();

        ResultActions actions = this.mockMvc.perform(post(ACCOUNT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").value(requestDto.getEmail()))
                .andExpect(jsonPath("password").doesNotExist())
                .andExpect(jsonPath("address").exists())
                .andExpect(jsonPath("accountRole").exists())
                .andExpect(jsonPath("createdAt").exists());

        String contentAsString = actions.andReturn().getResponse().getContentAsString();
        AccountResponseDto responseDto = objectMapper.readValue(contentAsString, AccountResponseDto.class);
        String token = generateToken(actions);

        String modifiedPassword = "modifiedPassword";
        String modifiedName = "modifiedName";
        String modifiedAddress = "modifiedAddress";

        AccountModifyRequestDto modifyRequestDto = AccountModifyRequestDto.builder()
                .password(modifiedPassword)
                .name(modifiedName)
                .address(modifiedAddress)
                .build();

        this.mockMvc.perform(put(ACCOUNT_URL + responseDto.getId())
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(modifyRequestDto))
                .header(HttpHeaders.AUTHORIZATION, token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").value(responseDto.getEmail()))
                .andExpect(jsonPath("password").doesNotExist())
                .andExpect(jsonPath("name").value(modifyRequestDto.getName()))
                .andExpect(jsonPath("address").value(modifyRequestDto.getAddress()))
                .andExpect(jsonPath("accountRole").value(responseDto.getAccountRole().name()))
                .andExpect(jsonPath("createdAt").exists())
        ;

        List<Account> accountList = accountRepository.findAll();
        assertEquals(requestDto.getEmail(), accountList.get(0).getEmail());
        assertEquals(modifiedName, accountList.get(0).getName());
        assertEquals(modifiedAddress, accountList.get(0).getAddress());
    }

    @DisplayName("디비에 없는 유저 삭제 -> 404 NOT_FOUND")
    @Test
    void remove_account_not_exist_404() throws Exception    {
        AccountRequestDto requestDto = AccountRequestDto.builder()
                .email("test@email.com")
                .password("testPW123!")
                .name("testName")
                .address("testAddress")
                .build();

        ResultActions actions = this.mockMvc.perform(post(ACCOUNT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").value(requestDto.getEmail()))
                .andExpect(jsonPath("password").doesNotExist())
                .andExpect(jsonPath("address").exists())
                .andExpect(jsonPath("accountRole").exists())
                .andExpect(jsonPath("createdAt").exists());

        String token = generateToken(actions);

        this.mockMvc.perform(delete(ACCOUNT_URL + "-1")
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, token))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @DisplayName("정상적으로 유저 삭제")
    @Test
    void remove_account_200() throws Exception  {
        AccountRequestDto requestDto = AccountRequestDto.builder()
                .email("test@email.com")
                .password("testPW123!")
                .name("testName")
                .address("testAddress")
                .build();

        ResultActions actions = this.mockMvc.perform(post(ACCOUNT_URL)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").value(requestDto.getEmail()))
                .andExpect(jsonPath("password").doesNotExist())
                .andExpect(jsonPath("address").exists())
                .andExpect(jsonPath("accountRole").exists())
                .andExpect(jsonPath("createdAt").exists());

        String contentAsString = actions.andReturn().getResponse().getContentAsString();
        AccountResponseDto responseDto = objectMapper.readValue(contentAsString, AccountResponseDto.class);
        String token = generateToken(actions);

        this.mockMvc.perform(delete(ACCOUNT_URL + responseDto.getId())
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, token))
                .andDo(print())
                .andExpect(status().isOk())
        ;
    }

    private static Stream<Arguments> validSaveAccount()   {
        return Stream.of(
                Arguments.of("", "testPassword", "testName", "testAddress"),
                Arguments.of(" ", "testPassword", "testName", "testAddress"),
                Arguments.of("test@email.com", "", "testName", "testAddress"),
                Arguments.of("test@email.com", " ", "testName", "testAddress"),
                Arguments.of("test@email.com", "testPassword", "", "testAddress"),
                Arguments.of("test@email.com", "testPassword", " ", "testAddress"),
                Arguments.of("test@email.com", "testPassword", "testName", ""),
                Arguments.of("test@email.com", "testPassword", "testNAme", " ")
        );
    }

    private static Stream<Arguments> validModifyAccount()   {
        return Stream.of(
                Arguments.of("", "modifiedTestName", "modifiedTestAddress"),
                Arguments.of(" ", "modifiedTestName", "modifiedTestAddress"),
                Arguments.of("modifiedTestPassword", "", "modifiedTestAddress"),
                Arguments.of("modifiedTestPassword", " ", "modifiedTestAddress"),
                Arguments.of("modifiedTestPassword", "modifiedTestName", ""),
                Arguments.of("modifiedTestPassword", "modifiedTestName", " ")
        );
    }

    private String generateToken(ResultActions actions) throws Exception {
        String contentAsString = actions.andReturn().getResponse().getContentAsString();
        AccountResponseDto responseDto = objectMapper.readValue(contentAsString, AccountResponseDto.class);
        JwtRequestDto jwtRequestDto = modelMapper.map(responseDto, JwtRequestDto.class);

        return PREFIX + jwtProvider.generateToken(jwtRequestDto);
    }

}