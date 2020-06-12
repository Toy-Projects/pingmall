package com.kiseok.pingmall.web.dto.account;

import com.kiseok.pingmall.common.domain.account.AccountRole;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class AccountResponseDto {

    private Long id;
    private String email;
    private String name;
    private String address;
    private Long balance;
    private AccountRole accountRole;
    private LocalDateTime createdAt;
}
