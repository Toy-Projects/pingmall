package com.kiseok.pingmall.common.resources;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static com.kiseok.pingmall.common.domain.resources.RestDocsResource.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class RestDocsResourcesTests {

    @DisplayName("REST DOCS 리소스 테스트")
    @Test
    void test_rest_docs_resources() {

        // index
        assertEquals(LOAD_INDEX.getRel(), "load-index");
        assertEquals(LOAD_INDEX.getProfile(), "/docs/index.html#resources-index-load");

        // profile
        assertEquals(PROFILE.getRel(), "profile");
        assertNull(PROFILE.getProfile());

        // error
        assertNull(ERROR.getRel());
        assertEquals(ERROR.getProfile(), "/docs/index.html#resources-error-");

        // account
        assertEquals(LOGIN_ACCOUNT.getRel(), "login-account");
        assertEquals(LOGIN_ACCOUNT.getProfile(), "/docs/index.html#resources-account-login");
        assertEquals(LOAD_ACCOUNT.getRel(), "load-account");
        assertEquals(LOAD_ACCOUNT.getProfile(), "/docs/index.html#resources-account-load");
        assertEquals(CREATE_ACCOUNT.getRel(), "create-account");
        assertEquals(CREATE_ACCOUNT.getProfile(), "/docs/index.html#resources-account-create");
        assertEquals(DEPOSIT_ACCOUNT.getRel(), "deposit-account");
        assertEquals(DEPOSIT_ACCOUNT.getProfile(), "/docs/index.html#resources-account-deposit");
        assertEquals(MODIFY_ACCOUNT.getRel(), "modify-account");
        assertEquals(MODIFY_ACCOUNT.getProfile(), "/docs/index.html#resources-account-modify");
        assertEquals(DELETE_ACCOUNT.getRel(), "delete-account");
        assertEquals(DELETE_ACCOUNT.getProfile(), "/docs/index.html#resources-account-delete");

        // product
        assertEquals(LOAD_PRODUCT.getRel(), "load-product");
        assertEquals(LOAD_PRODUCT.getProfile(), "/docs/index.html#resources-product-load");
        assertEquals(LOAD_ALL_PRODUCT.getRel(), "load-all-products");
        assertEquals(LOAD_ALL_PRODUCT.getProfile(), "/docs/index.html#resources-product-load-all");
        assertEquals(CREATE_PRODUCT.getRel(), "create-product");
        assertEquals(CREATE_PRODUCT.getProfile(), "/docs/index.html#resources-product-create");
        assertEquals(CREATE_PRODUCT_IMAGE.getRel(), "create-product-image");
        assertEquals(CREATE_PRODUCT_IMAGE.getProfile(), "/docs/index.html#resources-product-image-create");
        assertEquals(MODIFY_PRODUCT.getRel(), "modify-product");
        assertEquals(MODIFY_PRODUCT.getProfile(), "/docs/index.html#resources-product-modify");
        assertEquals(DELETE_PRODUCT.getRel(), "delete-product");
        assertEquals(DELETE_PRODUCT.getProfile(), "/docs/index.html#resources-product-delete");

        // orders
        assertEquals(CREATE_ORDERS.getRel(), "create-orders");
        assertEquals(CREATE_ORDERS.getProfile(), "/docs/index.html#resources-orders-create");
    }
}
