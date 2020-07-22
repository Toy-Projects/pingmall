package com.kiseok.pingmall.web.dto.comment;

import com.kiseok.pingmall.common.domain.comment.CommentType;
import lombok.*;
import javax.validation.constraints.NotBlank;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class CommentModifyRequestDto {

    @NotBlank(message = "내용은 반드시 입력해야 합니다.")
    private String content;

    private CommentType commentType;
}
