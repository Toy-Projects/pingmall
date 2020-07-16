package com.kiseok.pingmall.web.dto.find;

import lombok.*;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class FindPasswordResponseDto {

    private String password;
    private final String message = "Change your Password using this temporary Password RIGHT NOW!";

}
