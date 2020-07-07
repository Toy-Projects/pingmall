package com.kiseok.pingmall.web.controller;

import com.kiseok.pingmall.api.exception.account.*;
import com.kiseok.pingmall.api.exception.image.*;
import com.kiseok.pingmall.api.exception.product.ProductNotFoundException;
import com.kiseok.pingmall.api.exception.product.StockShortageException;
import com.kiseok.pingmall.common.errors.ErrorCode;
import com.kiseok.pingmall.common.errors.ErrorResource;
import com.kiseok.pingmall.web.dto.ErrorResponseDto;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import static com.kiseok.pingmall.common.errors.ErrorConstants.*;

@RequestMapping("/api/errors")
@RestControllerAdvice
public class ErrorControllerAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<?> handleInvalidInputException(MethodArgumentNotValidException e)    {
        ErrorResponseDto responseDto = ErrorResponseDto.createErrorResponseDto(ErrorCode.INVALID_INPUT_ERROR, getExtractFieldErrors(e.getBindingResult()));
        ErrorResource resource = new ErrorResource(responseDto);
        resource.add(new Link("/docs/index.html#resources-error-" + INVALID_INPUT).withRel("profile"));

        return new ResponseEntity<>(resource, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<?> handleBindException(BindException e)   {
        ErrorResponseDto responseDto = ErrorResponseDto.createErrorResponseDto(ErrorCode.INVALID_INPUT_ERROR, getExtractFieldErrors(e.getBindingResult()));
        ErrorResource resource = new ErrorResource(responseDto);
        resource.add(new Link("/docs/index.html#resources-error-" + INVALID_INPUT).withRel("profile"));

        return new ResponseEntity<>(resource, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserDuplicatedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<?> handleUserDuplicatedException()   {
        ErrorResponseDto responseDto = ErrorResponseDto.createErrorResponseDto(ErrorCode.DUPLICATED_ACCOUNT_ERROR, new ArrayList<>());
        ErrorResource resource = new ErrorResource(responseDto);
        resource.add(new Link("/docs/index.html#resources-error-" + DUPLICATED_ACCOUNT).withRel("profile"));

        return new ResponseEntity<>(resource, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<?> handleUserNotFoundException()  {
        ErrorResponseDto responseDto = ErrorResponseDto.createErrorResponseDto(ErrorCode.NOT_FOUND_ACCOUNT_ERROR, new ArrayList<>());
        ErrorResource resource = new ErrorResource(responseDto);
        resource.add(new Link("/docs/index.html#resources-error-" + NOT_FOUND_ACCOUNT).withRel("profile"));

        return new ResponseEntity<>(resource, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserIdNotMatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<?> handleUserIdNotMatchException()   {
        ErrorResponseDto responseDto = ErrorResponseDto.createErrorResponseDto(ErrorCode.NOT_MATCH_ACCOUNT_ID_ERROR, new ArrayList<>());
        ErrorResource resource = new ErrorResource(responseDto);
        resource.add(new Link("/docs/index.html#resources-error-" + NOT_MATCH_ACCOUNT_ID).withRel("profile"));

        return new ResponseEntity<>(resource, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserIdEqualsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<?> handleUserIdEqualException()   {
        ErrorResponseDto responseDto = ErrorResponseDto.createErrorResponseDto(ErrorCode.EQUAL_ACCOUNT_ID_ERROR, new ArrayList<>());
        ErrorResource resource = new ErrorResource(responseDto);
        resource.add(new Link("/docs/index.html#resources-error-" + EQUAL_ACCOUNT_ID).withRel("profile"));

        return new ResponseEntity<>(resource, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserUnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<?> handleUserUnauthorizedException()  {
        ErrorResponseDto responseDto = ErrorResponseDto.createErrorResponseDto(ErrorCode.UNAUTHORIZED_ACCOUNT_ERROR, new ArrayList<>());
        ErrorResource resource = new ErrorResource(responseDto);
        resource.add(new Link("/docs/index.html#resources-error-" + UNAUTHORIZED_ACCOUNT).withRel("profile"));

        return new ResponseEntity<>(resource, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<?> handleProductNotFoundException() {
        ErrorResponseDto responseDto = ErrorResponseDto.createErrorResponseDto(ErrorCode.NOT_FOUND_PRODUCT_ERROR, new ArrayList<>());
        ErrorResource resource = new ErrorResource(responseDto);
        resource.add(new Link("/docs/index.html#resources-error-" + NOT_FOUND_PRODUCT).withRel("profile"));

        return new ResponseEntity<>(resource, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(FileNameInvalidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<?> handleFileNameInvalidException() {
        ErrorResponseDto responseDto = ErrorResponseDto.createErrorResponseDto(ErrorCode.INVALID_FILE_NAME_ERROR, new ArrayList<>());
        ErrorResource resource = new ErrorResource(responseDto);
        resource.add(new Link("/docs/index.html#resources-error-" + INVALID_FILE_NAME).withRel("profile"));

        return new ResponseEntity<>(resource, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(FileExtensionInvalidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<?> handleFileExtensionInvalidException()   {
        ErrorResponseDto responseDto = ErrorResponseDto.createErrorResponseDto(ErrorCode.INVALID_FILE_EXTENSION_ERROR, new ArrayList<>());
        ErrorResource resource = new ErrorResource(responseDto);
        resource.add(new Link("/docs/index.html#resources-error-" + INVALID_FILE_EXTENSION).withRel("profile"));

        return new ResponseEntity<>(resource, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(FileNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<?> handleFileNotFoundException()  {
        ErrorResponseDto responseDto = ErrorResponseDto.createErrorResponseDto(ErrorCode.NOT_FOUND_FILE_ERROR, new ArrayList<>());
        ErrorResource resource = new ErrorResource(responseDto);
        resource.add(new Link("/docs/index.html#resources-error-" + NOT_FOUND_FILE).withRel("profile"));

        return new ResponseEntity<>(resource, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(FileInvalidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<?> handleFileInvalidException()   {
        ErrorResponseDto responseDto = ErrorResponseDto.createErrorResponseDto(ErrorCode.INVALID_FILE_ERROR, new ArrayList<>());
        ErrorResource resource = new ErrorResource(responseDto);
        resource.add(new Link("/docs/index.html#resources-error-" + INVALID_FILE).withRel("profile"));

        return new ResponseEntity<>(resource, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(FilePathInvalidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<?> handleFilePathInvalidException()   {
        ErrorResponseDto responseDto = ErrorResponseDto.createErrorResponseDto(ErrorCode.INVALID_FILE_PATH_ERROR, new ArrayList<>());
        ErrorResource resource = new ErrorResource(responseDto);
        resource.add(new Link("/docs/index.html#resources-error-" + INVALID_FILE_PATH).withRel("profile"));

        return new ResponseEntity<>(resource, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BalanceShortageException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<?> handleBalanceShortageException()   {
        ErrorResponseDto responseDto = ErrorResponseDto.createErrorResponseDto(ErrorCode.SHORTAGE_BALANCE_ERROR, new ArrayList<>());
        ErrorResource resource = new ErrorResource(responseDto);
        resource.add(new Link("/docs/index.html#resources-error-" + SHORTAGE_BALANCE).withRel("profile"));

        return new ResponseEntity<>(resource, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(StockShortageException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<?> handleStockShortageException() {
        ErrorResponseDto responseDto = ErrorResponseDto.createErrorResponseDto(ErrorCode.SHORTAGE_STOCK_ERROR, new ArrayList<>());
        ErrorResource resource = new ErrorResource(responseDto);
        resource.add(new Link("/docs/index.html#resources-error-" + SHORTAGE_STOCK).withRel("profile"));

        return new ResponseEntity<>(resource, HttpStatus.BAD_REQUEST);
    }

    private static List<ErrorResponseDto.ExtractFieldError> getExtractFieldErrors(BindingResult bindingResult) {
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        return fieldErrors
                .parallelStream()
                .map(error ->
                        ErrorResponseDto.ExtractFieldError.builder()
                                .field(error.getField())
                                .value(String.valueOf(error.getRejectedValue()))
                                .reason(error.getDefaultMessage())
                                .build())
                .collect(Collectors.toList());
    }
}
