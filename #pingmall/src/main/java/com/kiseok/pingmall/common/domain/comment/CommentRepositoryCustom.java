package com.kiseok.pingmall.common.domain.comment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentRepositoryCustom {
    Page<Comment> findByFilter(CommentType commentType, Long productId, Pageable pageable);
}
