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

    // find
    FIND_EMAIL("find-email", "/docs/index.html#resources-email-find"),
    FIND_PASSWORD("find-password", "/docs/index.html#resources-password-find")
    ;

    private final String rel;
    private final String profile;

}
