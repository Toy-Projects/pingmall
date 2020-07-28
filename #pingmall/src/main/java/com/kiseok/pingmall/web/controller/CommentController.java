package com.kiseok.pingmall.web.controller;

import com.kiseok.pingmall.api.service.CommentService;
import com.kiseok.pingmall.common.domain.account.Account;
import com.kiseok.pingmall.common.domain.account.CurrentUser;
import com.kiseok.pingmall.common.domain.comment.Comment;
import com.kiseok.pingmall.common.domain.comment.CommentType;
import com.kiseok.pingmall.common.domain.resources.ModelResource;
import com.kiseok.pingmall.web.dto.comment.CommentModifyRequestDto;
import com.kiseok.pingmall.web.dto.comment.CommentRequestDto;
import com.kiseok.pingmall.web.dto.comment.CommentResponseDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.security.auth.login.AccountNotFoundException;
import javax.validation.Valid;
import static com.kiseok.pingmall.common.domain.resources.RestDocsResource.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RequiredArgsConstructor
@RequestMapping(value = "/api/comments", produces = MediaTypes.HAL_JSON_VALUE)
@RestController
public class CommentController {

    private final ModelMapper modelMapper;
    private final ModelResource modelResource;
    private final CommentService commentService;

    @GetMapping
    ResponseEntity<?> loadFilteredComments(@RequestParam(required = false) CommentType commentType,
                                           @RequestParam(required = false) Long productId,
                                           Pageable pageable,
                                           PagedResourcesAssembler<Comment> assembler)  {

        Page<Comment> comments = commentService.loadAllFilteredComments(commentType, productId, pageable);
        PagedModel<EntityModel<?>> commentResources = assembler.toModel(comments, comment -> {
            CommentResponseDto responseDto = modelMapper.map(comment, CommentResponseDto.class);
            WebMvcLinkBuilder selfLinkBuilder = linkTo(CommentController.class).slash(responseDto.getId());
            EntityModel<?> resource = modelResource.getEntityModel(responseDto, selfLinkBuilder, LOAD_COMMENT.getProfile());
            resource.add(linkTo(ProductController.class).slash(responseDto.getProduct().getId()).withRel(LOAD_PRODUCT.getRel()));
            resource.add(linkTo(AccountController.class).slash(responseDto.getWriter().getId()).withRel(LOAD_ACCOUNT.getRel()));
            resource.add(linkTo(CommentController.class).withRel(CREATE_COMMENT.getRel()));
            resource.add(selfLinkBuilder.slash(responseDto.getId()).withRel(MODIFY_COMMENT.getRel()));
            resource.add(selfLinkBuilder.slash(responseDto.getId()).withRel(DELETE_COMMENT.getRel()));

            return resource;
        });
        commentResources.add(linkTo(CommentController.class).withRel(CREATE_COMMENT.getRel()));
        commentResources.add(Link.of(LOAD_ALL_COMMENTS.getProfile()).withRel(PROFILE.getRel()));

        return ResponseEntity.ok(commentResources);

    }

    @GetMapping("/{commentId}")
    ResponseEntity<?> loadComment(@PathVariable Long commentId)    {
        CommentResponseDto responseDto = commentService.loadComment(commentId);
        WebMvcLinkBuilder selfLinkBuilder = linkTo(CommentController.class).slash(responseDto.getId());
        EntityModel<?> resource = modelResource.getEntityModel(responseDto, selfLinkBuilder, LOAD_COMMENT.getProfile());
        resource.add(linkTo(ProductController.class).slash(responseDto.getProduct().getId()).withRel(LOAD_PRODUCT.getRel()));
        resource.add(linkTo(AccountController.class).slash(responseDto.getWriter().getId()).withRel(LOAD_ACCOUNT.getRel()));
        resource.add(selfLinkBuilder.withRel(LOAD_ALL_COMMENTS.getRel()));
        resource.add(linkTo(CommentController.class).withRel(CREATE_COMMENT.getRel()));
        resource.add(selfLinkBuilder.slash(responseDto.getId()).withRel(MODIFY_COMMENT.getRel()));
        resource.add(selfLinkBuilder.slash(responseDto.getId()).withRel(DELETE_COMMENT.getRel()));

        return ResponseEntity.ok(resource);
    }

    @PostMapping
    ResponseEntity<?> saveComment(@RequestBody @Valid CommentRequestDto requestDto, @CurrentUser Account currentUser) {
        CommentResponseDto responseDto = commentService.saveComment(requestDto, currentUser);
        WebMvcLinkBuilder selfLinkBuilder = linkTo(CommentController.class);
        EntityModel<?> resource = modelResource.getEntityModel(responseDto, selfLinkBuilder, CREATE_COMMENT.getProfile());
        resource.add(linkTo(ProductController.class).slash(responseDto.getProduct().getId()).withRel(LOAD_PRODUCT.getRel()));
        resource.add(linkTo(AccountController.class).slash(responseDto.getWriter().getId()).withRel(LOAD_ACCOUNT.getRel()));
        resource.add(selfLinkBuilder.slash(responseDto.getId()).withRel(LOAD_COMMENT.getRel()));
        resource.add(selfLinkBuilder.withRel(LOAD_ALL_COMMENTS.getRel()));
        resource.add(selfLinkBuilder.slash(responseDto.getId()).withRel(MODIFY_COMMENT.getRel()));
        resource.add(selfLinkBuilder.slash(responseDto.getId()).withRel(DELETE_COMMENT.getRel()));

        return ResponseEntity.created(selfLinkBuilder.toUri()).body(resource);
    }

    @PutMapping("/{commentId}")
    ResponseEntity<?> modifyComment(@PathVariable Long commentId, @RequestBody @Valid CommentModifyRequestDto requestDto, @CurrentUser Account currentAccount) throws AccountNotFoundException {
        CommentResponseDto responseDto = commentService.modifyComment(commentId, requestDto, currentAccount);
        WebMvcLinkBuilder selfLinkBuilder = linkTo(CommentController.class).slash(responseDto.getId());
        EntityModel<?> resource = modelResource.getEntityModel(responseDto, selfLinkBuilder, MODIFY_COMMENT.getProfile());
        resource.add(linkTo(ProductController.class).slash(responseDto.getProduct().getId()).withRel(LOAD_PRODUCT.getRel()));
        resource.add(linkTo(AccountController.class).slash(responseDto.getWriter().getId()).withRel(LOAD_ACCOUNT.getRel()));
        resource.add(selfLinkBuilder.slash(responseDto.getId()).withRel(LOAD_COMMENT.getRel()));
        resource.add(selfLinkBuilder.withRel(LOAD_ALL_COMMENTS.getRel()));
        resource.add(linkTo(CommentController.class).withRel(CREATE_COMMENT.getRel()));
        resource.add(selfLinkBuilder.slash(responseDto.getId()).withRel(DELETE_COMMENT.getRel()));

        return ResponseEntity.ok(resource);
    }

    @DeleteMapping("/{commentId}")
    ResponseEntity<?> deleteComment(@PathVariable Long commentId, @CurrentUser Account currentUser) throws AccountNotFoundException {
        CommentResponseDto responseDto = commentService.deleteComment(commentId, currentUser);
        WebMvcLinkBuilder selfLinkBuilder = linkTo(CommentController.class).slash(responseDto.getId());
        EntityModel<?> resource = modelResource.getEntityModel(responseDto, selfLinkBuilder, DELETE_COMMENT.getProfile());
        resource.add(linkTo(ProductController.class).slash(responseDto.getProduct().getId()).withRel(LOAD_PRODUCT.getRel()));
        resource.add(linkTo(AccountController.class).slash(responseDto.getWriter().getId()).withRel(LOAD_ACCOUNT.getRel()));
        resource.add(selfLinkBuilder.slash(responseDto.getId()).withRel(LOAD_COMMENT.getRel()));
        resource.add(selfLinkBuilder.withRel(LOAD_ALL_COMMENTS.getRel()));
        resource.add(linkTo(CommentController.class).withRel(CREATE_COMMENT.getRel()));
        resource.add(selfLinkBuilder.slash(responseDto.getId()).withRel(MODIFY_COMMENT.getRel()));

        return ResponseEntity.ok(resource);
    }
}
