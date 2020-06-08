package com.kiseok.pingmall.web.dto;

import lombok.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class LoginRequestDto {

    @Email(message = "이메일 형식이 아닙니다.")
    @NotBlank(message = "이메일은 반드시 입력해야 합니다.")
    private String email;

    @NotBlank(message = "비밀번호는 반드시 입력해야 합니다.")
    private String password;

}
