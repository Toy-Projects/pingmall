package com.kiseok.pingmall.common;

import com.kiseok.pingmall.common.properties.AppProperties;
import com.kiseok.pingmall.common.properties.ImageProperties;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class BaseCommonTests {

    @Autowired
    protected AppProperties appProperties;

    @Autowired
    protected ModelMapper modelMapper;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected ImageProperties imageProperties;
}
