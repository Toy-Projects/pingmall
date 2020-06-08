package com.kiseok.pingmall.web.dto.jwt;

import lombok.*;
import javax.validation.constraints.NotBlank;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class JwtRequestDto {

    @NotBlank
    private Long id;

    @NotBlank
    private String email;
}
