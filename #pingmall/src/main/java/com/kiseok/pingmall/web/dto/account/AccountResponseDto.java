package com.kiseok.pingmall.web.dto.account;

import com.kiseok.pingmall.common.domain.account.AccountRole;
import lombok.*;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class AccountResponseDto {

    private Long id;
    private String email;
    private String name;
    private String address;
    private Long balance;
    private AccountRole accountRole;
}
