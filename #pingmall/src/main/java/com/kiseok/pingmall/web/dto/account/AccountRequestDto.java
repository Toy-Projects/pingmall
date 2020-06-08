package com.kiseok.pingmall.web.dto.account;

import com.kiseok.pingmall.common.domain.account.Account;
import com.kiseok.pingmall.common.domain.account.AccountRole;
import lombok.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class AccountRequestDto {

    @Email(message = "이메일 형식이 아닙니다.")
    @NotBlank(message = "이메일은 반드시 입력해야 합니다.")
    private String email;

    @NotBlank(message = "비밀번호는 반드시 입력해야 합니다.")
    private String password;

    @NotBlank(message = "이름은 반드시 입력해야 합니다.")
    private String name;

    @NotBlank(message = "주소는 반드시 입력해야 합니다.")
    private String address;

    public Account toEntity() {
        return Account.builder()
                .email(email)
                .password(password)
                .name(name)
                .address(address)
                .accountRole(AccountRole.USER)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
