package com.kiseok.pingmall.common.domain.resources;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public enum RestDocsResource {

    // index
    LOAD_INDEX("load-index", "/docs/index.html#resources-index-load"),

    // profile
    PROFILE("profile", null),

    // error
    ERROR(null, "/docs/index.html#resources-error-"),

    // account
    LOGIN_ACCOUNT("login-account", "/docs/index.html#resources-account-login"),
    LOAD_ACCOUNT("load-account", "/docs/index.html#resources-account-load"),
    CREATE_ACCOUNT("create-account", "/docs/index.html#resources-account-create"),
    DEPOSIT_ACCOUNT("deposit-account", "/docs/index.html#resources-account-deposit"),
    MODIFY_ACCOUNT("modify-account", "/docs/index.html#resources-account-modify"),
    DELETE_ACCOUNT("delete-account", "/docs/index.html#resources-account-delete"),

    // product
    LOAD_PRODUCT("load-product", "/docs/index.html#resources-product-load"),
    LOAD_ALL_PRODUCT("load-all-products", "/docs/index.html#resources-product-load-all"),
    CREATE_PRODUCT("create-product", "/docs/index.html#resources-product-create"),
    CREATE_PRODUCT_IMAGE("create-product-image", "/docs/index.html#resources-product-image-create"),
    MODIFY_PRODUCT("modify-product", "/docs/index.html#resources-product-modify"),
    DELETE_PRODUCT("delete-product", "/docs/index.html#resources-product-delete"),

    // orders
    CREATE_ORDERS("create-orders", "/docs/index.html#resources-orders-create"),

    // comment
    LOAD_COMMENT("load-comment", "/docs/index.html#resources-comment-load"),
    LOAD_ALL_COMMENTS("load-all-comments", "/docs/index.html#resources-comment-load-all"),
    CREATE_COMMENT("create-comment", "/docs/index.html#resources-comment-create"),
    MODIFY_COMMENT("modify-comment", "/docs/index.html#resources-comment-modify"),
    DELETE_COMMENT("delete-comment", "/docs/index.html#resources-comment-delete"),

    // find
    FIND_EMAIL("find-email", "/docs/index.html#resources-email-find"),
    FIND_PASSWORD("find-password", "/docs/index.html#resources-password-find"),

    // verification
    VERIFY_EMAIL("verify-email", "/docs/index.html#resources-email-verify"),
    VERIFY_CODE("verify-code", "/docs/index.html#resources-code-verify")
    ;

    private final String rel;
    private final String profile;

}
