package com.kiseok.pingmall.web.dto.jwt;

import lombok.*;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class JwtResponseDto {

    String token;
}
