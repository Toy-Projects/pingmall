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

@RequiredArgsConstructor
@Service
public class ProductService {

    private final ModelMapper modelMapper;
    private final ProductRepository productRepository;
    private final AccountRepository accountRepository;

    public ResponseEntity<?> loadProduct(Long productId) {
        Product product = productRepository.findById(productId).orElseThrow(ProductNotFoundException::new);
        ProductResponseDto responseDto = modelMapper.map(product, ProductResponseDto.class);

        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    public ResponseEntity<?> saveProduct(ProductRequestDto requestDto, Account currentUser) {
        Account account = accountRepository.findById(currentUser.getId()).orElseThrow(UserNotFoundException::new);
        Product product = requestDto.toEntity(currentUser);
        account.getSellProducts().add(product);
        accountRepository.save(account);
        product = productRepository.save(product);
        ProductResponseDto responseDto = modelMapper.map(product, ProductResponseDto.class);

        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    public ResponseEntity<?> modifyProduct(Long productId, ProductRequestDto requestDto, Account currentUser) {
        Account account = accountRepository.findById(currentUser.getId()).orElseThrow(UserNotFoundException::new);
        Product product = productRepository.findById(productId).orElseThrow(ProductNotFoundException::new);
        if(!product.getSeller().getId().equals(account.getId()))  {
            throw new UserIdNotMatchException();
        }
        account.getSellProducts().remove(product);
        modelMapper.map(requestDto, product);
        account.getSellProducts().add(product);
        accountRepository.save(account);
        product = productRepository.save(product);
        ProductResponseDto responseDto = modelMapper.map(product, ProductResponseDto.class);

        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    public ResponseEntity<?> deleteProduct(Long productId, Account currentUser) {
        Account account = accountRepository.findById(currentUser.getId()).orElseThrow(UserNotFoundException::new);
        Product product = productRepository.findById(productId).orElseThrow(ProductNotFoundException::new);
        if(!product.getSeller().getId().equals(account.getId()))  {
            throw new UserIdNotMatchException();
        }
        productRepository.delete(product);
        account.getSellProducts().remove(product);

        return new ResponseEntity<>(accountRepository.save(account), HttpStatus.OK);
    }
}
