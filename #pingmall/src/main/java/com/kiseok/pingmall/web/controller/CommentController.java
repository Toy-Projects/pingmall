package com.kiseok.pingmall.web.controller;

import com.kiseok.pingmall.api.service.CommentService;
import com.kiseok.pingmall.common.domain.account.Account;
import com.kiseok.pingmall.common.domain.account.CurrentUser;
import com.kiseok.pingmall.web.dto.comment.CommentModifyRequestDto;
import com.kiseok.pingmall.web.dto.comment.CommentRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

@RequiredArgsConstructor
@RequestMapping(value = "/api/comments", produces = MediaTypes.HAL_JSON_VALUE)
@RestController
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    ResponseEntity<?> saveComment(@RequestBody @Valid CommentRequestDto requestDto, @CurrentUser Account currentUser) {
        return commentService.saveComment(requestDto, currentUser);
    }

    @PutMapping("/{commentId}")
    ResponseEntity<?> modifyComment(@PathVariable Long commentId, @RequestBody @Valid CommentModifyRequestDto requestDto, @CurrentUser Account currentAccount)  {
        return commentService.modifyComment(commentId, requestDto, currentAccount);
    }
}
