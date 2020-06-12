package com.kiseok.pingmall.web.dto.account;

import lombok.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class AccountDepositRequestDto {

    @Min(value = 1L, message = "1원 부터 예금이 가능 합니다.")
    @Max(value = 9999999L, message = "9,999,999원 까지만 예금이 가능 합니다.")
    private Long balance;
}
