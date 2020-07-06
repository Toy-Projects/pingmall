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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ProductService {

    private final ModelMapper modelMapper;
    private final ProductRepository productRepository;
    private final AccountRepository accountRepository;

    public Page<Product> loadAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    public ProductResponseDto loadProduct(Long productId) {
        Product product = isProductExist(productId);

        return modelMapper.map(product, ProductResponseDto.class);
    }

    public ProductResponseDto saveProduct(ProductRequestDto requestDto, Account currentUser) {
        Account account = isUserExist(currentUser);
        Product product = requestDto.toEntity(currentUser);
        account.getSellProducts().add(product);
        product = productRepository.save(product);

        return modelMapper.map(product, ProductResponseDto.class);
    }

    public ProductResponseDto modifyProduct(Long productId, ProductRequestDto requestDto, Account currentUser) {
        Account account = isUserExist(currentUser);
        Product product = isProductExist(productId);
        isUserIdMatch(account, product);
        account.getSellProducts().remove(product);
        product.updateProduct(requestDto);
        account.getSellProducts().add(product);
        product = productRepository.save(product);

        return modelMapper.map(product, ProductResponseDto.class);
    }

    public ProductResponseDto deleteProduct(Long productId, Account currentUser) {
        Account account = isUserExist(currentUser);
        Product product = isProductExist(productId);
        isUserIdMatch(account, product);
        productRepository.delete(product);
        account.getSellProducts().remove(product);

        return modelMapper.map(product, ProductResponseDto.class);
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
