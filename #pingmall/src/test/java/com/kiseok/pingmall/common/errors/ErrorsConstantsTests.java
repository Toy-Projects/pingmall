package com.kiseok.pingmall.common.errors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static com.kiseok.pingmall.common.errors.ErrorsConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ErrorsConstantsTests {

    @DisplayName("Error Constants 테스트")
    @Test
    void test_error_constants() {
        assertEquals(NOT_FOUND_404, "404 NOT_FOUND");
        assertEquals(BAD_REQUEST_400, "400 BAD_REQUEST");
        assertEquals(INVALID_INPUT, "Entered values are invalid!");
        assertEquals(DUPLICATED_ACCOUNT, "Entered email is already exist!");
        assertEquals(NOT_FOUND_ACCOUNT, "AccountId is not exist!");
        assertEquals(NOT_MATCH_ACCOUNT_ID, "AccountIds are not match!");
        assertEquals(EQUAL_ACCOUNT_ID, "AccountId is equals to seller's ID!");
        assertEquals(NOT_FOUND_PRODUCT, "ProductId is not exist!");
        assertEquals(INVALID_FILE_NAME, "Filename is invalid value!");
        assertEquals(INVALID_FILE_EXTENSION, "File extension is invalid!");
        assertEquals(NOT_FOUND_FILE, "File is not exist!");
        assertEquals(INVALID_FILE, "The file unfit for use!");
        assertEquals(INVALID_FILE_PATH, "The file path is invalid!");
        assertEquals(SHORTAGE_BALANCE, "You have shortage of balance to buy the product!");
        assertEquals(SHORTAGE_STOCK, "The product has shortage of stock to fulfill the amount you want!");
    }
}
