package com.kiseok.pingmall.web.controller;

import com.kiseok.pingmall.api.service.VerificationService;
import com.kiseok.pingmall.common.resources.ModelResource;
import com.kiseok.pingmall.web.dto.verification.VerificationCodeRequestDto;
import com.kiseok.pingmall.web.dto.verification.VerificationEmailRequestDto;
import com.kiseok.pingmall.web.dto.verification.VerificationResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import static com.kiseok.pingmall.common.resources.RestDocsResource.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/verifications", produces = MediaTypes.HAL_JSON_VALUE)
public class VerificationController {

    private final ModelResource modelResource;
    private final VerificationService verificationService;

    @PostMapping
    ResponseEntity<?> verifyEmail(@RequestBody @Valid VerificationEmailRequestDto requestDto)  {
        VerificationResponseDto responseDto = verificationService.verifyEmail(requestDto);
        WebMvcLinkBuilder selfLinkBuilder = linkTo(VerificationController.class);
        EntityModel<?> resource = modelResource.getEntityModel(responseDto, selfLinkBuilder, VERIFY_EMAIL.getProfile());
        resource.add(linkTo(LoginController.class).withRel(LOGIN_ACCOUNT.getRel()));
        resource.add(linkTo(ProductController.class).withRel(LOAD_ALL_PRODUCT.getRel()));

        return ResponseEntity.created(selfLinkBuilder.toUri()).body(resource);
    }

    @PutMapping
    ResponseEntity<?> verifyCode(@RequestBody @Valid VerificationCodeRequestDto requestDto)  {
        VerificationResponseDto responseDto = verificationService.verifyCode(requestDto);
        WebMvcLinkBuilder selfLinkBuilder = linkTo(VerificationController.class);
        EntityModel<?> resource = modelResource.getEntityModel(responseDto, selfLinkBuilder, VERIFY_CODE.getProfile());
        resource.add(linkTo(LoginController.class).withRel(LOGIN_ACCOUNT.getRel()));
        resource.add(linkTo(ProductController.class).withRel(LOAD_ALL_PRODUCT.getRel()));

        return ResponseEntity.ok(resource);
    }

}
