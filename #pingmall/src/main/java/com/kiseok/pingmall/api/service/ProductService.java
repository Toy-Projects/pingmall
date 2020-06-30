package com.kiseok.pingmall.api.service;

import com.kiseok.pingmall.api.exception.product.ProductNotFoundException;
import com.kiseok.pingmall.api.exception.account.UserIdNotMatchException;
import com.kiseok.pingmall.api.exception.account.UserNotFoundException;
import com.kiseok.pingmall.common.domain.account.Account;
import com.kiseok.pingmall.common.domain.account.AccountRepository;
import com.kiseok.pingmall.common.domain.product.Product;
import com.kiseok.pingmall.common.domain.product.ProductRepository;
import com.kiseok.pingmall.web.dto.product.ProductRequestDto;
import com.kiseok.pingmall.web.dto.product.ProductResponseDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ProductService {

    private final ModelMapper modelMapper;
    private final ProductRepository productRepository;
    private final AccountRepository accountRepository;

    public ResponseEntity<?> loadAllProducts() {
        List<ProductResponseDto> responseDtoList = new ArrayList<>();
        productRepository.findAll().forEach(product -> {
            responseDtoList.add(modelMapper.map(product, ProductResponseDto.class));
        });

        return new ResponseEntity<>(responseDtoList, HttpStatus.OK);
    }

    public ResponseEntity<?> loadProduct(Long productId) {
        Product product = isProductExist(productId);
        ProductResponseDto responseDto = modelMapper.map(product, ProductResponseDto.class);

        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    public ResponseEntity<?> saveProduct(ProductRequestDto requestDto, Account currentUser) {
        Account account = isUserExist(currentUser);
        Product product = requestDto.toEntity(currentUser);
        account.getSellProducts().add(product);
        product = productRepository.save(product);
        ProductResponseDto responseDto = modelMapper.map(product, ProductResponseDto.class);

        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    public ResponseEntity<?> modifyProduct(Long productId, ProductRequestDto requestDto, Account currentUser) {
        Account account = isUserExist(currentUser);
        Product product = isProductExist(productId);
        isUserIdMatch(account, product);
        account.getSellProducts().remove(product);
        product.updateProduct(requestDto);
        account.getSellProducts().add(product);
        product = productRepository.save(product);
        ProductResponseDto responseDto = modelMapper.map(product, ProductResponseDto.class);

        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    public ResponseEntity<?> deleteProduct(Long productId, Account currentUser) {
        Account account = isUserExist(currentUser);
        Product product = isProductExist(productId);
        isUserIdMatch(account, product);
        productRepository.delete(product);
        account.getSellProducts().remove(product);

        return new ResponseEntity<>(accountRepository.save(account), HttpStatus.OK);
    }

    private Account isUserExist(Account currentUser) {
        return accountRepository.findById(currentUser.getId()).orElseThrow(UserNotFoundException::new);
    }

    private Product isProductExist(Long productId) {
        return productRepository.findById(productId).orElseThrow(ProductNotFoundException::new);
    }

    private void isUserIdMatch(Account account, Product product) {
        if (!product.getSeller().getId().equals(account.getId())) {
            throw new UserIdNotMatchException();
        }
    }
}
