package com.kiseok.pingmall.web.controller;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import static com.kiseok.pingmall.common.domain.resources.RestDocsResource.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RequestMapping(value = "/api")
@RestController
public class IndexController {

    @GetMapping
    public RepresentationModel<?> index()    {
        RepresentationModel<?> resource = new RepresentationModel<>();
        resource.add(linkTo(LoginController.class).withRel(LOGIN_ACCOUNT.getRel()));
        resource.add(Link.of(LOAD_INDEX.getProfile()).withRel(PROFILE.getRel()));

        return resource;
    }
}
