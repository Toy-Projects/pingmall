package com.kiseok.pingmall.api.service;

import com.kiseok.pingmall.common.domain.account.Account;
import com.kiseok.pingmall.common.domain.account.AccountRepository;
import com.kiseok.pingmall.common.domain.account.CurrentUser;
import com.kiseok.pingmall.common.domain.product.Product;
import com.kiseok.pingmall.common.domain.product.ProductRepository;
import com.kiseok.pingmall.web.dto.product.ProductRequestDto;
import com.kiseok.pingmall.web.dto.product.ProductResponseDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ProductService {

    private final ModelMapper modelMapper;
    private final ProductRepository productRepository;
    private final AccountRepository accountRepository;

    public ResponseEntity<?> loadProduct(Long productId) {
        Optional<Product> optionalProduct = productRepository.findById(productId);
        if(!optionalProduct.isPresent())    {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        ProductResponseDto responseDto = modelMapper.map(optionalProduct.get(), ProductResponseDto.class);

        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    public ResponseEntity<?> saveProduct(ProductRequestDto requestDto, @CurrentUser Account currentUser) {
        Optional<Account> optionalAccount = accountRepository.findById(currentUser.getId());
        if(!optionalAccount.isPresent())    {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Account account = optionalAccount.get();
        Product product = requestDto.toEntity(currentUser);
        account.getSellProducts().add(product);
        accountRepository.save(account);
        product = productRepository.save(product);
        ProductResponseDto responseDto = modelMapper.map(product, ProductResponseDto.class);

        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }
}
