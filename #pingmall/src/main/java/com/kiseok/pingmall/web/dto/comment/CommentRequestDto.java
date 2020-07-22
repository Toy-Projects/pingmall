package com.kiseok.pingmall.web.dto.comment;

import com.kiseok.pingmall.common.domain.account.Account;
import com.kiseok.pingmall.common.domain.comment.Comment;
import com.kiseok.pingmall.common.domain.comment.CommentType;
import com.kiseok.pingmall.common.domain.product.Product;
import lombok.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class CommentRequestDto {

    @NotBlank(message = "내용은 반드시 입력해야 합니다.")
    private String content;

    private CommentType commentType;

    @NotNull
    private Long productId;

    public Comment toEntity(Account currentUser, Product product) {
        return Comment.builder()
                .content(content)
                .commentType(commentType)
                .product(product)
                .writer(currentUser)
                .build();
    }
}
