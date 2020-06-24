package com.kiseok.pingmall.common.properties;

import com.kiseok.pingmall.common.BaseCommonTests;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AppPropertiesTests extends BaseCommonTests {

    @DisplayName("App Properties 테스트")
    @Test
    void test_app_properties()  {

        // Account
        assertEquals(appProperties.getTestEmail(), "test@email.com");
        assertEquals(appProperties.getTestPassword(), "testPW123!");
        assertEquals(appProperties.getTestModifiedPassword(), "modifiedPW123!");
        assertEquals(appProperties.getTestName(), "testName");
        assertEquals(appProperties.getTestModifiedName(), "modifiedName");
        assertEquals(appProperties.getTestAddress(), "testAddress");
        assertEquals(appProperties.getTestModifiedAddress(), "modifiedAddress");
        assertEquals(appProperties.getTestBalance(), 9999999L);

        // Product
        assertEquals(appProperties.getTestProductName(), "testProductName");
        assertEquals(appProperties.getTestModifiedProductName(), "modifiedProductName");
        assertEquals(appProperties.getTestSize(), "265");
        assertEquals(appProperties.getTestModifiedSize(), "270");
        assertEquals(appProperties.getTestImage(), "testImage");
        assertEquals(appProperties.getTestModifiedImage(), "modifiedImage");
        assertEquals(appProperties.getTestPrice(), 20000L);
        assertEquals(appProperties.getTestModifiedPrice(), 10000L);
        assertEquals(appProperties.getTestStock(), 10L);
        assertEquals(appProperties.getTestModifiedStock(), 5L);

        // Orders
        assertEquals(appProperties.getTestAmount(), 2L);
    }
}
