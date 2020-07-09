package com.kiseok.pingmall.common.domain.resources;

import com.kiseok.pingmall.web.controller.ErrorControllerAdvice;
import com.kiseok.pingmall.web.dto.ErrorResponseDto;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;
import java.util.List;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Component
public class ModelResource {

    public EntityModel<?> getEntityModel(Object responseDto) {
        return EntityModel.of(responseDto);
    }

    public EntityModel<ErrorResponseDto> getErrorResponseModelWithSelfRel(ErrorResponseDto responseDto) {
        return EntityModel.of(responseDto)
                .add(linkTo(ErrorControllerAdvice.class).withSelfRel());
    }

    public EntityModel<?> getEntityModelWithSelfRel(Object responseDto, WebMvcLinkBuilder selfLinkBuilder) {
        return EntityModel.of(responseDto)
                .add(selfLinkBuilder.withSelfRel());
    }

    public CollectionModel<EntityModel<?>> getCollectionModelWithSelfRel(List<EntityModel<?>> entityModelList, WebMvcLinkBuilder selfLinkBuilder) {
        return CollectionModel.of(entityModelList)
                .add(selfLinkBuilder.withSelfRel());
    }
}
