package com.tstu.productdetermination.exception.handle;

import com.tstu.commons.dto.http.response.error.ErrorField;
import com.tstu.commons.dto.http.response.error.ErrorResponse;
import com.tstu.commons.dto.http.response.error.ErrorValidationResponse;
import com.tstu.commons.exception.handle.PrsErrorHandler;
import com.tstu.productdetermination.exception.ProductDeterminationErrors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ProductDeterminationErrorHandler extends PrsErrorHandler {

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatus status,
                                                                  WebRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .id(UUID.randomUUID().toString())
                .time(LocalDateTime.now())
                .code(ProductDeterminationErrors.JSON_NOT_READABLE.name())
                .message(ex.getMessage())
                .displayMessage(ProductDeterminationErrors.JSON_NOT_READABLE.getErrorDescription())
                .techInfo(null)
                .build();
        return handleExceptionInternal(ex, errorResponse, headers, HttpStatus.BAD_REQUEST, request);
    }


    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        List<FieldError> fieldError = ex.getBindingResult().getFieldErrors();
        List<ErrorField> errorFields = (List)fieldError.stream().map((fe) -> new ErrorField(fe.getField(), fe.getDefaultMessage())).collect(Collectors.toList());
        ErrorValidationResponse errorValidationResponse = new ErrorValidationResponse(errorFields);
        return this.handleExceptionInternal(ex, errorValidationResponse, headers, HttpStatus.UNPROCESSABLE_ENTITY, request);
    }
}
