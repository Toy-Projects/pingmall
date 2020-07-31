package com.kiseok.pingmall.web.controller;

import com.kiseok.pingmall.api.service.FindService;
import com.kiseok.pingmall.common.resources.ModelResource;
import com.kiseok.pingmall.web.dto.find.FindEmailResponseDto;
import com.kiseok.pingmall.web.dto.find.FindPasswordRequestDto;
import com.kiseok.pingmall.web.dto.find.FindPasswordResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import static com.kiseok.pingmall.common.resources.RestDocsResource.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RequiredArgsConstructor
@RequestMapping("/api/find")
@RestController
public class FindController {

    private final ModelResource modelResource;
    private final FindService findService;

    @GetMapping("/email")
    ResponseEntity<?> findEmail(@RequestParam(name = "name") String name) {
        FindEmailResponseDto responseDto = findService.findEmail(name);
        WebMvcLinkBuilder selfLinkBuilder = linkTo(FindController.class).slash("email");
        EntityModel<?> resource = modelResource.getEntityModel(responseDto, selfLinkBuilder, FIND_EMAIL.getProfile());
        resource.add(linkTo(FindController.class).slash("password").withRel(FIND_PASSWORD.getRel()));
        resource.add(linkTo(LoginController.class).withRel(LOGIN_ACCOUNT.getRel()));

        return ResponseEntity.ok(resource);
    }

    @PutMapping("/password")
    ResponseEntity<?> findPassword(@RequestBody @Valid FindPasswordRequestDto requestDto)   {
        FindPasswordResponseDto responseDto = findService.findPassword(requestDto);
        WebMvcLinkBuilder selfLinkBuilder = linkTo(FindController.class).slash("password");
        EntityModel<?> resource = modelResource.getEntityModel(responseDto, selfLinkBuilder, FIND_PASSWORD.getProfile());
        resource.add(linkTo(FindController.class).slash("email").withRel(FIND_EMAIL.getRel()));
        resource.add(linkTo(LoginController.class).withRel(LOGIN_ACCOUNT.getRel()));

        return ResponseEntity.ok(resource);
    }
}
