package com.kiseok.pingmall.common.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static com.kiseok.pingmall.common.config.jwt.JwtConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JwtConstantsTests {

    @DisplayName("JWT Constants 테스트")
    @Test
    void test_jwt_constants()   {
        assertEquals(SECRET, "KISEOK");
        assertEquals(HEADER, "Authorization");
        assertEquals(PREFIX, "Bearer ");
        assertEquals(JWT_VALIDITY, 5 * 60 * 60);
    }
}
