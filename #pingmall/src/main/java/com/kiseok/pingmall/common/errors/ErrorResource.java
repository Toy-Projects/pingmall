package com.kiseok.pingmall.common.errors;

import com.kiseok.pingmall.web.controller.ErrorControllerAdvice;
import com.kiseok.pingmall.web.dto.ErrorResponseDto;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@NoArgsConstructor
public class ErrorResource extends EntityModel<ErrorResponseDto> {

    public ErrorResource(ErrorResponseDto responseDto, Link... links) {
        super(responseDto, links);
        add(linkTo(ErrorControllerAdvice.class).withSelfRel());
    }
}
