package com.kiseok.pingmall.common.errors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static com.kiseok.pingmall.common.errors.ErrorCode.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ErrorCodeTests {

    @DisplayName("Error Code 테스트")
    @Test
    void test_error_constants() {

        // Account
        assertEquals(INVALID_INPUT_ERROR.getCode(), "a001");
        assertEquals(INVALID_INPUT_ERROR.getStatus(), "400 BAD_REQUEST");
        assertEquals(INVALID_INPUT_ERROR.getMessage(), "Entered values are invalid!");

        assertEquals(DUPLICATED_ACCOUNT_ERROR.getCode(), "a002");
        assertEquals(DUPLICATED_ACCOUNT_ERROR.getStatus(), "400 BAD_REQUEST");
        assertEquals(DUPLICATED_ACCOUNT_ERROR.getMessage(), "Entered email is already exist!");

        assertEquals(NOT_FOUND_ACCOUNT_ERROR.getCode(), "a003");
        assertEquals(NOT_FOUND_ACCOUNT_ERROR.getStatus(), "404 NOT_FOUND");
        assertEquals(NOT_FOUND_ACCOUNT_ERROR.getMessage(), "AccountId is not exist!");

        assertEquals(NOT_MATCH_ACCOUNT_ID_ERROR.getCode(), "a004");
        assertEquals(NOT_MATCH_ACCOUNT_ID_ERROR.getStatus(), "400 BAD_REQUEST");
        assertEquals(NOT_MATCH_ACCOUNT_ID_ERROR.getMessage(), "AccountIds are not match!");

        assertEquals(EQUAL_ACCOUNT_ID_ERROR.getCode(), "a005");
        assertEquals(EQUAL_ACCOUNT_ID_ERROR.getStatus(), "400 BAD_REQUEST");
        assertEquals(EQUAL_ACCOUNT_ID_ERROR.getMessage(), "AccountId is equals to seller's ID!");

        // Product
        assertEquals(NOT_FOUND_PRODUCT_ERROR.getCode(), "p001");
        assertEquals(NOT_FOUND_PRODUCT_ERROR.getStatus(), "404 NOT_FOUND");
        assertEquals(NOT_FOUND_PRODUCT_ERROR.getMessage(), "ProductId is not exist!");

        // Image
        assertEquals(INVALID_FILE_NAME_ERROR.getCode(), "i001");
        assertEquals(INVALID_FILE_NAME_ERROR.getStatus(), "400 BAD_REQUEST");
        assertEquals(INVALID_FILE_NAME_ERROR.getMessage(), "Filename is invalid value!");

        assertEquals(INVALID_FILE_EXTENSION_ERROR.getCode(), "i002");
        assertEquals(INVALID_FILE_EXTENSION_ERROR.getStatus(), "400 BAD_REQUEST");
        assertEquals(INVALID_FILE_EXTENSION_ERROR.getMessage(), "File extension is invalid!");

        assertEquals(NOT_FOUND_FILE_ERROR.getCode(), "i003");
        assertEquals(NOT_FOUND_FILE_ERROR.getStatus(), "404 NOT_FOUND");
        assertEquals(NOT_FOUND_FILE_ERROR.getMessage(), "File is not exist!");

        assertEquals(INVALID_FILE_ERROR.getCode(), "i004");
        assertEquals(INVALID_FILE_ERROR.getStatus(), "400 BAD_REQUEST");
        assertEquals(INVALID_FILE_ERROR.getMessage(), "The file unfit for use!");

        assertEquals(INVALID_FILE_PATH_ERROR.getCode(), "i005");
        assertEquals(INVALID_FILE_PATH_ERROR.getStatus(), "400 BAD_REQUEST");
        assertEquals(INVALID_FILE_PATH_ERROR.getMessage(), "The file path is invalid!");

        // Orders
        assertEquals(SHORTAGE_BALANCE_ERROR.getCode(), "o001");
        assertEquals(SHORTAGE_BALANCE_ERROR.getStatus(), "400 BAD_REQUEST");
        assertEquals(SHORTAGE_BALANCE_ERROR.getMessage(), "You have shortage of balance to buy the product!");

        assertEquals(SHORTAGE_STOCK_ERROR.getCode(), "o002");
        assertEquals(SHORTAGE_STOCK_ERROR.getStatus(), "400 BAD_REQUEST");
        assertEquals(SHORTAGE_STOCK_ERROR.getMessage(), "The product has shortage of stock to fulfill the amount you want!");
    }
}
