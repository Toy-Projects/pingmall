package com.kiseok.pingmall.api.service;

import com.kiseok.pingmall.api.exception.account.UserIdNotMatchException;
import com.kiseok.pingmall.api.exception.comment.CommentNotFoundException;
import com.kiseok.pingmall.api.exception.product.ProductNotFoundException;
import com.kiseok.pingmall.common.domain.account.Account;
import com.kiseok.pingmall.common.domain.comment.Comment;
import com.kiseok.pingmall.common.domain.comment.CommentRepository;
import com.kiseok.pingmall.common.domain.product.Product;
import com.kiseok.pingmall.common.domain.product.ProductRepository;
import com.kiseok.pingmall.web.dto.comment.CommentModifyRequestDto;
import com.kiseok.pingmall.web.dto.comment.CommentRequestDto;
import com.kiseok.pingmall.web.dto.comment.CommentResponseDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CommentService {

    private final ModelMapper modelMapper;
    private final ProductRepository productRepository;
    private final CommentRepository commentRepository;

    public ResponseEntity<?> loadComment(Long commentId) {
        Comment comment = isExistComment(commentId);
        return new ResponseEntity<>(modelMapper.map(comment, CommentResponseDto.class), HttpStatus.OK);
    }

    public ResponseEntity<?> saveComment(CommentRequestDto requestDto, Account currentUser) {
        Product product = isExistProduct(requestDto.getProductId());
        Comment comment = commentRepository.save(requestDto.toEntity(currentUser, product));
        product.getComment().add(comment);
        productRepository.save(product);
        CommentResponseDto responseDto = modelMapper.map(comment, CommentResponseDto.class);

        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    public ResponseEntity<?> modifyComment(Long commentId, CommentModifyRequestDto requestDto, Account currentAccount) {
        Comment comment = isExistComment(commentId);
        isEqualAccountId(comment, currentAccount);
        Product product = isExistProduct(comment.getProduct().getId());
        product.getComment().remove(comment);
        comment.updateComment(requestDto);
        product.getComment().add(comment);
        CommentResponseDto responseDto = modelMapper.map(commentRepository.save(comment), CommentResponseDto.class);

        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    public ResponseEntity<?> deleteComment(Long commentId, Account currentUser) {
        Comment comment = isExistComment(commentId);
        isEqualAccountId(comment, currentUser);
        Product product = isExistProduct(comment.getProduct().getId());
        commentRepository.delete(comment);
        product.getComment().remove(comment);

        return new ResponseEntity<>(modelMapper.map(comment, CommentResponseDto.class), HttpStatus.OK);
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
        return productRepository.findById(productId).orElseThrow(ProductNotFoundException::new);
    }
}
