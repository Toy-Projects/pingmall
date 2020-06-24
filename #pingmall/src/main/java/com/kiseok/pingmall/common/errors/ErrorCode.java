package com.kiseok.pingmall.common.errors;

import lombok.Getter;
import static com.kiseok.pingmall.common.errors.ErrorsConstants.*;

@Getter
public enum ErrorCode {

    // Account
    INVALID_INPUT_ERROR("a001", BAD_REQUEST_400, INVALID_INPUT),
    DUPLICATED_ACCOUNT_ERROR("a002", BAD_REQUEST_400, DUPLICATED_ACCOUNT),
    NOT_FOUND_ACCOUNT_ERROR("a003", NOT_FOUND_404, NOT_FOUND_ACCOUNT),
    NOT_MATCH_ACCOUNT_ID_ERROR("a004", BAD_REQUEST_400, NOT_MATCH_ACCOUNT_ID),

    // Product
    NOT_FOUND_PRODUCT_ERROR("p001", NOT_FOUND_404, NOT_FOUND_PRODUCT),

    // Image
    INVALID_FILE_NAME_ERROR("i001", BAD_REQUEST_400, INVALID_FILE_NAME),
    INVALID_FILE_EXTENSION_ERROR("i002", BAD_REQUEST_400, INVALID_FILE_EXTENSION),
    NOT_FOUND_FILE_ERROR("i003", NOT_FOUND_404, NOT_FOUND_FILE),
    INVALID_FILE_ERROR("i004", BAD_REQUEST_400, INVALID_FILE),
    INVALID_FILE_PATH_ERROR("i005", BAD_REQUEST_400, INVALID_FILE_PATH),

    // Orders
    SHORTAGE_BALANCE_ERROR("o001", BAD_REQUEST_400, SHORTAGE_BALANCE),
    SHORTAGE_STOCK_ERROR("o002", BAD_REQUEST_400, SHORTAGE_STOCK);

    private final String code;
    private final String status;
    private final String message;

    ErrorCode(String code, String status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }
}
