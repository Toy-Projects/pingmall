package com.kiseok.pingmall.common;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "app-property")
@Component
@Getter @Setter
public class AppProperties {

    // Account & Login에 필요한 properties
    private String testEmail;
    private String testPassword;
    private String testModifiedPassword;
    private String testName;
    private Long testBalance;
    private String testModifiedName;
    private String testAddress;
    private String testModifiedAddress;

}
