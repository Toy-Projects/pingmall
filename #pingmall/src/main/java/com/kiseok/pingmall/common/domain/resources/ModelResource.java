package com.kiseok.pingmall.common.domain.resources;

import com.kiseok.pingmall.web.controller.ErrorControllerAdvice;
import com.kiseok.pingmall.web.controller.IndexController;
import com.kiseok.pingmall.web.dto.ErrorResponseDto;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;
import java.util.List;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static com.kiseok.pingmall.common.domain.resources.RestDocsResource.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ModelResource {

    public EntityModel<?> getEntityModel(Object responseDto, WebMvcLinkBuilder selfLinkBuilder) {
        return EntityModel.of(responseDto)
                .add(selfLinkBuilder.withSelfRel());
    }

    public EntityModel<?> getEntityModel(Object responseDto, WebMvcLinkBuilder selfLinkBuilder, String profile) {
        return EntityModel.of(responseDto)
                .add(selfLinkBuilder.withSelfRel())
                .add(Link.of(profile).withRel(PROFILE.getRel()));
    }

    public EntityModel<ErrorResponseDto> getErrorResponseModel(ErrorResponseDto responseDto, String error) {
        return EntityModel.of(responseDto)
                .add(linkTo(ErrorControllerAdvice.class).withSelfRel())
                .add(linkTo(methodOn(IndexController.class).index()).withRel(LOAD_INDEX.getRel()))
                .add(Link.of(ERROR.getProfile() + error).withRel(PROFILE.getRel()));
    }

    public CollectionModel<EntityModel<?>> getCollectionModel(List<EntityModel<?>> entityModelList, WebMvcLinkBuilder selfLinkBuilder, String profile) {
        return CollectionModel.of(entityModelList)
                .add(selfLinkBuilder.withSelfRel())
                .add(Link.of(profile).withRel(PROFILE.getRel()));
    }
}
