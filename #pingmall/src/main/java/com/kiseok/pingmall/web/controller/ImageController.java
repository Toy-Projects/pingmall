package com.kiseok.pingmall.web.controller;

import com.kiseok.pingmall.api.service.ImageService;
import com.kiseok.pingmall.common.domain.account.Account;
import com.kiseok.pingmall.common.domain.account.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@RequiredArgsConstructor
@RequestMapping(value = "/api/images")
@RestController
public class ImageController {

    private final ImageService imageService;

    @GetMapping("/products/{productId}/{imagePath:.+}")
    ResponseEntity<?> loadImage(@PathVariable Long productId, @PathVariable String imagePath, HttpServletRequest request) {
        return imageService.loadImage(imagePath, request);
    }

    @PostMapping("/products/{productId}")
    ResponseEntity<?> saveProductImage(@PathVariable Long productId, @RequestParam(name = "file", required = false) MultipartFile file,
                                       HttpServletRequest request, @CurrentUser Account currentUser) throws IOException {
        if(file == null)    {
            return imageService.saveDefaultProductImage(productId, request,  currentUser);
        }
        else {
            return imageService.saveProductImage(productId, file, request, currentUser);
        }
    }
}
