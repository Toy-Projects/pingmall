package com.kiseok.pingmall.web.controller;

import com.kiseok.pingmall.api.exception.account.UserDuplicatedException;
import com.kiseok.pingmall.api.exception.account.UserIdNotMatchException;
import com.kiseok.pingmall.api.exception.account.UserNotFoundException;
import com.kiseok.pingmall.api.exception.image.*;
import com.kiseok.pingmall.api.exception.product.ProductNotFoundException;
import com.kiseok.pingmall.common.errors.ErrorCode;
import com.kiseok.pingmall.web.dto.ErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ErrorControllerAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<?> handleInvalidInputException(MethodArgumentNotValidException e)    {
        ErrorResponseDto responseDto = ErrorResponseDto.createErrorResponseDto(ErrorCode.INVALID_INPUT_ERROR, getExtractFieldErrors(e.getBindingResult()));
        return new ResponseEntity<>(responseDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserDuplicatedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<?> handleUserDuplicatedException()   {
        ErrorResponseDto responseDto = ErrorResponseDto.createErrorResponseDto(ErrorCode.DUPLICATED_ACCOUNT_ERROR, new ArrayList<>());
        return new ResponseEntity<>(responseDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<?> handleUserNotFoundException()  {
        ErrorResponseDto responseDto = ErrorResponseDto.createErrorResponseDto(ErrorCode.NOT_FOUND_ACCOUNT_ERROR, new ArrayList<>());
        return new ResponseEntity<>(responseDto, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserIdNotMatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<?> handleUserIdNotMatchException()   {
        ErrorResponseDto responseDto = ErrorResponseDto.createErrorResponseDto(ErrorCode.NOT_MATCH_ACCOUNT_ID_ERROR, new ArrayList<>());
        return new ResponseEntity<>(responseDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<?> handleProductNotFoundException() {
        ErrorResponseDto responseDto = ErrorResponseDto.createErrorResponseDto(ErrorCode.NOT_FOUND_PRODUCT_ERROR, new ArrayList<>());
        return new ResponseEntity<>(responseDto, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(FileNameInvalidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<?> handleFileNameInvalidException() {
        ErrorResponseDto responseDto = ErrorResponseDto.createErrorResponseDto(ErrorCode.INVALID_FILE_NAME_ERROR, new ArrayList<>());
        return new ResponseEntity<>(responseDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(FileExtensionInvalidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<?> handleFileExtensionInvalidException()   {
        ErrorResponseDto responseDto = ErrorResponseDto.createErrorResponseDto(ErrorCode.INVALID_FILE_EXTENSION_ERROR, new ArrayList<>());
        return new ResponseEntity<>(responseDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(FileNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<?> handleFileNotFoundException()  {
        ErrorResponseDto responseDto = ErrorResponseDto.createErrorResponseDto(ErrorCode.NOT_FOUND_FILE_ERROR, new ArrayList<>());
        return new ResponseEntity<>(responseDto, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(FileInvalidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<?> handleFileInvalidException()   {
        ErrorResponseDto responseDto = ErrorResponseDto.createErrorResponseDto(ErrorCode.INVALID_FILE_ERROR, new ArrayList<>());
        return new ResponseEntity<>(responseDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(FilePathInvalidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<?> handleFilePathInvalidException()   {
        ErrorResponseDto responseDto = ErrorResponseDto.createErrorResponseDto(ErrorCode.INVALID_FILE_PATH_ERROR, new ArrayList<>());
        return new ResponseEntity<>(responseDto, HttpStatus.BAD_REQUEST);
    }

    private static List<ErrorResponseDto.ExtractFieldError> getExtractFieldErrors(BindingResult bindingResult) {
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        return fieldErrors.parallelStream().map(error ->
                ErrorResponseDto.ExtractFieldError.builder()
                        .field(error.getField())
                        .value(String.valueOf(error.getRejectedValue()))
                        .reason(error.getDefaultMessage())
                        .build()
        ).collect(Collectors.toList());
    }
}
