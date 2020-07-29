package com.kiseok.pingmall.web.dto.verification;

import lombok.*;

@ToString
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class VerificationResponseDto {

    private Long id;
    private String email;
    private String verificationCode;
    private Boolean isVerified;
}
