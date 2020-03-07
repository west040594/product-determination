package com.tstu.productdetermination.exception;

import com.tstu.commons.exception.ExceptionMessage;

public interface ProductDeterminationExceptionMessage extends ExceptionMessage {
    String FILE_NOT_FOUND_MSG = "Файл не найден";
    String FILE_NOT_READABLE_MSG = "Не удалось прочитать файл";
    String CANNOT_CREATE_MODEL_MSG = "Не удалось создать модель";
    String CANNOT_FIND_MODEL_MSG = "Не удалось найти модель";
    String CANNOT_FIND_ZIP_ARCHIVE_MSG = "Не удалось найти архив c фотографиями";
    String TYPE_OF_PRODUCT_DOES_NOT_SUPPORT_MSG = "Тип продукта(имя модели) не поддерживается";
}
