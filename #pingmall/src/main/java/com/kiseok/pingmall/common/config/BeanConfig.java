package com.kiseok.pingmall.common.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class BeanConfig {

    @Bean
    public ModelMapper modelMapper()    {
        return new ModelMapper();
    }

    @Bean
    public PasswordEncoder passwordEncoder()    {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
