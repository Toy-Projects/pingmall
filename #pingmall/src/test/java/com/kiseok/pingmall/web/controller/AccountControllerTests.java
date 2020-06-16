package com.kiseok.pingmall.web.controller;

import com.kiseok.pingmall.common.domain.account.Account;
import com.kiseok.pingmall.web.common.BaseControllerTest;
import com.kiseok.pingmall.web.dto.account.AccountDepositRequestDto;
import com.kiseok.pingmall.web.dto.account.AccountModifyRequestDto;
import com.kiseok.pingmall.web.dto.account.AccountRequestDto;
import com.kiseok.pingmall.web.dto.account.AccountResponseDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import java.util.List;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AccountControllerTests extends BaseControllerTest {

    @AfterEach
    void deleteAll()    {
        accountRepository.deleteAll();
    }

    @DisplayName("유저 생성 시 유효성 검사 실패 -> 400 BAD_REQUEST")
    @ParameterizedTest(name = "{index} {displayName} message={0}")
    @MethodSource("validSaveAccount")
    void save_account_invalid_400(String email, String password, String name, String address, Long balance) throws Exception    {
        AccountRequestDto requestDto = AccountRequestDto.builder()
                .email(email)
                .password(password)
                .name(name)
                .address(address)
                .balance(balance)
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
        AccountRequestDto requestDto = createAccountRequestDto();

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
                .andExpect(jsonPath("balance").exists())
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
        AccountRequestDto requestDto = createAccountRequestDto();

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
                .andExpect(jsonPath("balance").exists())
                .andExpect(jsonPath("accountRole").exists())
                .andExpect(jsonPath("createdAt").exists())
        ;

        List<Account> accountList = accountRepository.findAll();
        assertEquals(appProperties.getTestEmail(), accountList.get(0).getEmail());
        assertEquals(appProperties.getTestName(), accountList.get(0).getName());
        assertEquals(appProperties.getTestAddress(), accountList.get(0).getAddress());
        assertEquals(appProperties.getTestBalance(), accountList.get(0).getBalance());
    }

    @DisplayName("디비에 없는 유저 불러오기 -> 404 NOT_FOUND")
    @Test
    void loadAccount_not_exist_404() throws Exception {
        AccountRequestDto requestDto = createAccountRequestDto();

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
                .andExpect(jsonPath("balance").exists())
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
        AccountRequestDto requestDto = createAccountRequestDto();

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
                .andExpect(jsonPath("balance").exists())
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
                .andExpect(jsonPath("balance").value(responseDto.getBalance()))
                .andExpect(jsonPath("accountRole").value(responseDto.getAccountRole().name()))
                .andExpect(jsonPath("createdAt").exists())
        ;
    }

    @DisplayName("유저 잔액 추가 유효성 검사 -> 400 BAD_REQUEST")
    @ParameterizedTest(name = "{index} {displayName} message={0}")
    @ValueSource(longs = {0L, 10000000L})
    void deposit_account_invalid_400(Long balance) throws Exception {
        AccountRequestDto requestDto = createAccountRequestDto();

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
                .andExpect(jsonPath("balance").exists())
                .andExpect(jsonPath("accountRole").exists())
                .andExpect(jsonPath("createdAt").exists());

        String jwt = generateToken(actions);
        String contentAsString = actions.andReturn().getResponse().getContentAsString();
        AccountResponseDto responseDto = objectMapper.readValue(contentAsString, AccountResponseDto.class);
        AccountDepositRequestDto depositRequestDto = AccountDepositRequestDto.builder()
                .balance(balance)
                .build();

        this.mockMvc.perform(put(ACCOUNT_URL + responseDto.getId() + "/balance")
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(depositRequestDto))
                .header(HttpHeaders.AUTHORIZATION, jwt))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("[*].code").exists())
                .andExpect(jsonPath("[*].defaultMessage").exists())
                .andExpect(jsonPath("[*].rejectedValue").exists())
                .andExpect(jsonPath("[*].field").exists())
                .andExpect(jsonPath("[*].objectName").exists())
        ;
    }

    @DisplayName("유저 잔액 추가 시 유저 null -> 400 BAD_REQUEST")
    @Test
    void deposit_account_user_null_404() throws Exception   {
        AccountRequestDto requestDto = createAccountRequestDto();

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
                .andExpect(jsonPath("balance").exists())
                .andExpect(jsonPath("accountRole").exists())
                .andExpect(jsonPath("createdAt").exists());

        String jwt = generateToken(actions);
        AccountDepositRequestDto depositRequestDto = AccountDepositRequestDto.builder()
                .balance(100L)
                .build();

        this.mockMvc.perform(put(ACCOUNT_URL + "-1/balance")
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(depositRequestDto))
                .header(HttpHeaders.AUTHORIZATION, jwt))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @DisplayName("인증 없이 예금 시 -> 401 UNATHORIZED")
    @Test
    void deposit_account_unauthoized_401() throws Exception   {
        AccountRequestDto requestDto = createAccountRequestDto();

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
                .andExpect(jsonPath("balance").exists())
                .andExpect(jsonPath("accountRole").exists())
                .andExpect(jsonPath("createdAt").exists())
        ;

        AccountDepositRequestDto depositRequestDto = AccountDepositRequestDto.builder()
                .balance(100L)
                .build();

        this.mockMvc.perform(put(ACCOUNT_URL + "-1/balance")
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(depositRequestDto)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
        ;
    }

    @DisplayName("정상적으로 유저 잔액 추가 -> 200 OK")
    @Test
    void deposit_account_200() throws Exception {
        AccountRequestDto requestDto = createAccountRequestDto();

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
                .andExpect(jsonPath("balance").exists())
                .andExpect(jsonPath("accountRole").exists())
                .andExpect(jsonPath("createdAt").exists());

        String jwt = generateToken(actions);
        String contentAsString = actions.andReturn().getResponse().getContentAsString();
        AccountResponseDto responseDto = objectMapper.readValue(contentAsString, AccountResponseDto.class);
        AccountDepositRequestDto depositRequestDto = AccountDepositRequestDto.builder()
                .balance(100L)
                .build();

        this.mockMvc.perform(put(ACCOUNT_URL + responseDto.getId() + "/balance")
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(depositRequestDto))
                .header(HttpHeaders.AUTHORIZATION, jwt))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").value(requestDto.getEmail()))
                .andExpect(jsonPath("password").doesNotExist())
                .andExpect(jsonPath("address").exists())
                .andExpect(jsonPath("balance").value(300L))
                .andExpect(jsonPath("accountRole").exists())
                .andExpect(jsonPath("createdAt").exists())
        ;
    }



    @DisplayName("유저 수정 유효성 검사 -> 400 BAD_REQUEST")
    @ParameterizedTest(name = "{index} {displayName} message={0}")
    @MethodSource("validModifyAccount")
    void modify_account_invalid_400(String password, String name, String address) throws Exception  {
        AccountRequestDto requestDto = createAccountRequestDto();

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
                .andExpect(jsonPath("balance").exists())
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
        AccountRequestDto requestDto = createAccountRequestDto();

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
                .andExpect(jsonPath("balance").exists())
                .andExpect(jsonPath("accountRole").exists())
                .andExpect(jsonPath("createdAt").exists());

        String token = generateToken(actions);
        AccountModifyRequestDto modifyRequestDto = createAccountModifyRequestDto();

        this.mockMvc.perform(put(ACCOUNT_URL + "-1")
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(modifyRequestDto))
                .header(HttpHeaders.AUTHORIZATION, token))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @DisplayName("정상적으로 유저 수정 -> 200 OK")
    @Test
    void modify_account_200() throws Exception  {
        AccountRequestDto requestDto = createAccountRequestDto();

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
                .andExpect(jsonPath("balance").exists())
                .andExpect(jsonPath("accountRole").exists())
                .andExpect(jsonPath("createdAt").exists());

        String contentAsString = actions.andReturn().getResponse().getContentAsString();
        AccountResponseDto responseDto = objectMapper.readValue(contentAsString, AccountResponseDto.class);
        String token = generateToken(actions);
        AccountModifyRequestDto modifyRequestDto = createAccountModifyRequestDto();

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
                .andExpect(jsonPath("balance").value(responseDto.getBalance()))
                .andExpect(jsonPath("accountRole").value(responseDto.getAccountRole().name()))
                .andExpect(jsonPath("createdAt").exists())
        ;

        List<Account> accountList = accountRepository.findAll();
        assertEquals(requestDto.getEmail(), accountList.get(0).getEmail());
        assertEquals(modifyRequestDto.getName(), accountList.get(0).getName());
        assertEquals(modifyRequestDto.getAddress(), accountList.get(0).getAddress());
    }

    @DisplayName("디비에 없는 유저 삭제 -> 404 NOT_FOUND")
    @Test
    void remove_account_not_exist_404() throws Exception    {
        AccountRequestDto requestDto = createAccountRequestDto();

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
                .andExpect(jsonPath("balance").exists())
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
        AccountRequestDto requestDto = createAccountRequestDto();

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
                .andExpect(jsonPath("balance").exists())
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
                Arguments.of("", "testPassword", "testName", "testAddress", 200L, true),
                Arguments.of(" ", "testPassword", "testName", "testAddress", 200L,true),
                Arguments.of("test@email.com", "", "testName", "testAddress", 200L, true),
                Arguments.of("test@email.com", " ", "testName", "testAddress", 200L, true),
                Arguments.of("test@email.com", "testPassword", "", "testAddress", 200L, true),
                Arguments.of("test@email.com", "testPassword", " ", "testAddress", 200L, true),
                Arguments.of("test@email.com", "testPassword", "testName", "", 200L, true),
                Arguments.of("test@email.com", "testPassword", "testNAme", " ", 200L, true),
                Arguments.of("test@email.com", "testPassword", "testName", "", null, true)
        );
    }

    private static Stream<Arguments> validModifyAccount()   {
        return Stream.of(
                Arguments.of("", "modifiedTestName", "modifiedTestAddress", true),
                Arguments.of(" ", "modifiedTestName", "modifiedTestAddress", true),
                Arguments.of("modifiedTestPassword", "", "modifiedTestAddress", true),
                Arguments.of("modifiedTestPassword", " ", "modifiedTestAddress", true),
                Arguments.of("modifiedTestPassword", "modifiedTestName", "", true),
                Arguments.of("modifiedTestPassword", "modifiedTestName", " ", true)
        );
    }
}