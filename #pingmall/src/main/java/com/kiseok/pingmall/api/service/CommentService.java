package com.kiseok.pingmall.api.service;

import com.kiseok.pingmall.api.exception.account.UserIdNotMatchException;
import com.kiseok.pingmall.api.exception.account.UserNotFoundException;
import com.kiseok.pingmall.api.exception.comment.CommentNotFoundException;
import com.kiseok.pingmall.api.exception.product.ProductNotFoundException;
import com.kiseok.pingmall.common.domain.account.Account;
import com.kiseok.pingmall.common.domain.account.AccountRepository;
import com.kiseok.pingmall.common.domain.comment.Comment;
import com.kiseok.pingmall.common.domain.comment.CommentRepository;
import com.kiseok.pingmall.common.domain.comment.CommentType;
import com.kiseok.pingmall.common.domain.product.Product;
import com.kiseok.pingmall.common.domain.product.ProductRepository;
import com.kiseok.pingmall.web.dto.comment.CommentModifyRequestDto;
import com.kiseok.pingmall.web.dto.comment.CommentRequestDto;
import com.kiseok.pingmall.web.dto.comment.CommentResponseDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CommentService {

    private final ModelMapper modelMapper;
    private final AccountRepository accountRepository;
    private final ProductRepository productRepository;
    private final CommentRepository commentRepository;

    public Page<Comment> loadAllFilteredComments(CommentType commentType, Long productId, Pageable pageable) {
        isExistProduct(productId);
        return commentRepository.findByFilter(commentType, productId, pageable);
    }

    public CommentResponseDto loadComment(Long commentId) {
        Comment comment = isExistComment(commentId);
        return modelMapper.map(comment, CommentResponseDto.class);
    }

    public CommentResponseDto saveComment(CommentRequestDto requestDto, Account currentUser) {
        Product product = isExistProduct(requestDto.getProductId());
        Account account = accountRepository.findById(currentUser.getId()).orElseThrow(UserNotFoundException::new);
        Comment comment = requestDto.toEntity(currentUser, product);
        product.getComment().add(comment);
        account.getComments().add(comment);
        comment = commentRepository.save(comment);

        return modelMapper.map(comment, CommentResponseDto.class);
    }

    public CommentResponseDto modifyComment(Long commentId, CommentModifyRequestDto requestDto, Account currentAccount) {
        Comment comment = isExistComment(commentId);
        isEqualAccountId(comment, currentAccount);
        Product product = isExistProduct(comment.getProduct().getId());
        product.getComment().remove(comment);
        comment.updateComment(requestDto);
        product.getComment().add(comment);

        return modelMapper.map(commentRepository.save(comment), CommentResponseDto.class);
    }

    public CommentResponseDto deleteComment(Long commentId, Account currentUser) {
        Comment comment = isExistComment(commentId);
        isEqualAccountId(comment, currentUser);
        commentRepository.delete(comment);

        return modelMapper.map(comment, CommentResponseDto.class);
    }

    private void isEqualAccountId(Comment comment, Account currentAccount) {
        if(!comment.getWriter().getId().equals(currentAccount.getId())) {
            throw new UserIdNotMatchException();
        }
    }

    private Comment isExistComment(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(CommentNotFoundException::new);
    }

    private Product isExistProduct(Long productId) {
        if(productId == null)
            throw new ProductNotFoundException();
        return productRepository.findById(productId).orElseThrow(ProductNotFoundException::new);
    }
}
