package com.kiseok.pingmall.web.dto.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotBlank;

@Getter @Builder
@NoArgsConstructor @AllArgsConstructor
public class AccountModifyRequestDto {

    @NotBlank(message = "비밀번호는 반드시 입력해야 합니다.")
    private String password;

    @NotBlank(message = "이름은 반드시 입력해야 합니다.")
    private String name;

    @NotBlank(message = "주소는 반드시 입력해야 합니다.")
    private String address;

}
