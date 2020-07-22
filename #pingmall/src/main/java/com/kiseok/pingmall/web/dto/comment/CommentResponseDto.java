package com.kiseok.pingmall.web.dto.comment;

import com.kiseok.pingmall.common.domain.account.Account;
import com.kiseok.pingmall.common.domain.comment.CommentType;
import com.kiseok.pingmall.common.domain.product.Product;
import lombok.*;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class CommentResponseDto {

    private Long id;
    private String content;
    private CommentType commentType;
    private Account writer;
    private Product product;
}
