package com.kiseok.pingmall.web.controller;

import com.kiseok.pingmall.api.service.FindService;
import com.kiseok.pingmall.web.dto.find.FindPasswordRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

@RequiredArgsConstructor
@RequestMapping("/api/find")
@RestController
public class FindController {

    private final FindService findService;

    @GetMapping("/email")
    ResponseEntity<?> findEmail(@RequestParam(name = "name") String name) {
        return findService.findEmail(name);
    }

    @PutMapping("/password")
    ResponseEntity<?> findPassword(@RequestBody @Valid FindPasswordRequestDto requestDto)   {
        return findService.findPassword(requestDto);
    }
}
