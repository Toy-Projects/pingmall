package com.kiseok.pingmall.api.service;

import com.kiseok.pingmall.api.exception.image.*;
import com.kiseok.pingmall.api.exception.product.ProductNotFoundException;
import com.kiseok.pingmall.common.domain.account.Account;
import com.kiseok.pingmall.common.domain.product.Product;
import com.kiseok.pingmall.common.domain.product.ProductRepository;
import com.kiseok.pingmall.common.properties.ImageProperties;
import com.kiseok.pingmall.web.dto.product.ProductResponseDto;
import org.apache.commons.io.FilenameUtils;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Service
public class ImageService {

    private final Path root;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;
    private static final String DEFAULT_IMAGE = "DefaultProduct.jpg";

    public ImageService(ImageProperties imageProperties, ProductRepository productRepository, ModelMapper modelMapper) throws FileUploadException {
        this.root = Paths.get(imageProperties.getLocation()).toAbsolutePath().normalize();
        this.productRepository = productRepository;
        this.modelMapper = modelMapper;
        try {
            Files.createDirectories(this.root);
        }
        catch(Exception e) {
            throw new FileUploadException("Failed to create directory for upload file!! 파일을 업로드할 디렉토리를 생성하지 못했습니다.", e);
        }
    }

    public ResponseEntity<?> loadImage(String imagePath, HttpServletRequest request) {
        try {
            Path filePath = this.root.resolve(imagePath).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if(resource.exists()) {
                String contentType = null;
                try {
                    contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
                } catch (IOException ex) {
                    throw new FileInvalidException();
                }
                if(contentType == null) {
                    contentType = "application/octet-stream";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            }
            else {
                throw new FileNotFoundException();
            }
        }
        catch(MalformedURLException e) {
            throw new FilePathInvalidException();
        }
    }

    public ResponseEntity<?> saveProductImage(Long productId, MultipartFile file, HttpServletRequest request, Account currentUser) throws IOException {
        Product product = isProductExist(productId);
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String extension = FilenameUtils.getExtension(fileName);
        if(fileName.contains("..")) {
            throw new FileNameInvalidException();
        }
        else if(!"png".equals(extension) && !"jpg".equals(extension) && !"jpeg".equals(extension))  {
            throw new FileExtensionInvalidException();
        }

        String requestUri = request.getRequestURI() + "/";
        String imagePrefix = currentUser.getEmail() + "_" + product.getName();
        fileName = UUID.randomUUID().toString() + "_" + imagePrefix + "_" + fileName;

        Path targetLocation = this.root.resolve(fileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        String imagePath =  ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(requestUri)
                .path(fileName)
                .toUriString();
        product.setImage(imagePath);
        ProductResponseDto responseDto = modelMapper.map(productRepository.save(product),  ProductResponseDto.class);

        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    public ResponseEntity<?> saveDefaultProductImage(Long productId, HttpServletRequest request, Account currentUser) {
        Product product = isProductExist(productId);
        String imagePrefix = currentUser.getEmail() + "_" + product.getName();
        String fileName = UUID.randomUUID().toString() + "_" + imagePrefix + "_" + DEFAULT_IMAGE;
        String requestUri = request.getRequestURI() + "/";
        String imagePath = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(requestUri)
                .pathSegment(fileName)
                .toUriString();
        product.setImage(imagePath);
        ProductResponseDto responseDto = modelMapper.map(productRepository.save(product),  ProductResponseDto.class);

        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    private Product isProductExist(Long productId) {
        return productRepository.findById(productId).orElseThrow(ProductNotFoundException::new);
    }
}
