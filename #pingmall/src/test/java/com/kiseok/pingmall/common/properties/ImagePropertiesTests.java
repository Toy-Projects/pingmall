package com.kiseok.pingmall.common.properties;

import com.kiseok.pingmall.common.BaseCommonTests;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ImagePropertiesTests extends BaseCommonTests {

    @DisplayName("Image Properties 테스트")
    @Test
    void test_image_properties()    {
        assertEquals(imageProperties.getLocation(), "./images");
    }
}
