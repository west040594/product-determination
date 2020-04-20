package com.tstu.productdetermination.exception;

import com.tstu.commons.exception.PrsErrorCode;

import java.util.Arrays;
import java.util.Optional;

public enum ProductDeterminationErrors implements PrsErrorCode {

    EXPIRED_OR_INVALID_JWT_TOKEN(1, ProductDeterminationExceptionMessage.EXPIRED_OR_INVALID_JWT_TOKEN_MSG),
    JSON_NOT_READABLE(2, ProductDeterminationExceptionMessage.JSON_NOT_READABLE),
    ACCESS_DENIED(3, ProductDeterminationExceptionMessage.ACCESS_DENIED_MSG),
    FILE_NOT_READABLE(4, ProductDeterminationExceptionMessage.FILE_NOT_READABLE_MSG),
    FILE_NOT_FOUND(5, ProductDeterminationExceptionMessage.FILE_NOT_FOUND_MSG),
    CANNOT_CREATE_MODEL(6, ProductDeterminationExceptionMessage.CANNOT_CREATE_MODEL_MSG),
    CANNOT_FIND_MODEL(7, ProductDeterminationExceptionMessage.CANNOT_FIND_MODEL_MSG),
    CANNOT_FIND_ZIP_ARCHIVE(8, ProductDeterminationExceptionMessage.CANNOT_FIND_ZIP_ARCHIVE_MSG),
    TYPE_OF_PRODUCT_DOES_NOT_SUPPORT(9, ProductDeterminationExceptionMessage.TYPE_OF_PRODUCT_DOES_NOT_SUPPORT_MSG),
    PROCESS_OF_CREATE_MODEL_IS_RUNNING(10, ProductDeterminationExceptionMessage.PROCESS_OF_CREATE_MODEL_IS_RUNNING_MSG);


    private Integer errorCode;
    private String errorDescription;

    ProductDeterminationErrors(Integer errorCode, String errorDescription) {
        this.errorCode = errorCode;
        this.errorDescription = errorDescription;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public static Optional<ProductDeterminationErrors> getByDescription(String errorDescription) {
        return Arrays.stream(values())
                .filter(productDeterminationErrors -> productDeterminationErrors.errorDescription.equals(errorDescription))
                .findFirst();
    }
}
