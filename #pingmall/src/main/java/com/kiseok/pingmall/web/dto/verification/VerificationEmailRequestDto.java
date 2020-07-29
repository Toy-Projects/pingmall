package com.kiseok.pingmall.web.dto.verification;

import com.kiseok.pingmall.common.domain.verification.Verification;
import lombok.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class VerificationEmailRequestDto {

    @Email(message = "이메일 형식이 아닙니다.")
    @NotBlank(message = "이메일은 반드시 입력해야 합니다.")
    private String email;

    public Verification toEntity(String verificationCode) {
        return Verification.builder()
                .email(email)
                .verificationCode(verificationCode)
                .isVerified(false)
                .build();
    }
}
