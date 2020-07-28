package com.kiseok.pingmall.common.domain.comment;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;
import static com.kiseok.pingmall.common.domain.comment.QComment.comment;

@Repository
public class CommentRepositoryImpl extends QuerydslRepositorySupport implements CommentRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    public CommentRepositoryImpl(JPAQueryFactory jpaQueryFactory) {
        super(Comment.class);
        this.jpaQueryFactory = jpaQueryFactory;
    }

    @Override
    public Page<Comment> findByFilter(CommentType commentType, Long productId, Pageable pageable) {
        QueryResults<Comment> filteredComments =
                jpaQueryFactory.selectFrom(comment)
                .where(
                        comment.product.id.eq(productId),
                        isContainCommentType(commentType)
                )
                .orderBy(comment.modifiedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults()
        ;

        return new PageImpl<>(filteredComments.getResults(), pageable, filteredComments.getTotal());
    }

    private BooleanExpression isContainCommentType(CommentType commentType) {
        if(commentType == null) {
            return null;
        }
        return comment.commentType.stringValue().eq(commentType.name());
    }
}
