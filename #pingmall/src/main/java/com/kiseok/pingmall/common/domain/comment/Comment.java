package com.kiseok.pingmall.common.domain.comment;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.kiseok.pingmall.common.domain.BaseTimeEntity;
import com.kiseok.pingmall.common.domain.account.Account;
import com.kiseok.pingmall.common.domain.product.Product;
import com.kiseok.pingmall.web.dto.comment.CommentModifyRequestDto;
import lombok.*;
import javax.persistence.*;

@Getter @Builder
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
@Entity @Table
@JsonIdentityInfo(
        scope = Comment.class,
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id"
)
public class Comment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private CommentType commentType;

    @ManyToOne
    private Account writer;

    @ManyToOne
    private Product product;

    public void updateComment(CommentModifyRequestDto requestDto) {
        this.content = requestDto.getContent();
        this.commentType = requestDto.getCommentType();
    }
}
