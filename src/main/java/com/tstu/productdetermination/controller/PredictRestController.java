package com.tstu.productdetermination.controller;

import com.tstu.commons.dto.http.response.determination.PredictionResponse;
import com.tstu.commons.exception.PrsHttpException;
import com.tstu.productdetermination.exception.ProductDeterminationErrors;
import com.tstu.productdetermination.exception.ProductDeterminationExceptionMessage;
import com.tstu.productdetermination.service.ProductPredictionService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@RestController
@CrossOrigin
@Slf4j
@RequiredArgsConstructor
@RequestMapping(value = "api/v1/determination/predict")
public class PredictRestController {

    private final ProductPredictionService productPredictionService;

    /**
     * Предсказать продукт по его фотографии
     * @param file  Фотография продукта
     * @param modelName Наименование нейронной модели
     * @return Стуктура полученного предказания по продукту.
     * Содержит колекцию предметов наиболее подходящих под фотографии. Предмет состоит из наименования и рейтинга совпадения
     */
    @ApiOperation(value = "${api.swagger.predict.image}", response = PredictionResponse.class)
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = ProductDeterminationExceptionMessage.UNEXPECTED_ERROR_MSG),
            @ApiResponse(code = 403, message = ProductDeterminationExceptionMessage.ACCESS_DENIED_MSG),
            @ApiResponse(code = 422, message = ProductDeterminationExceptionMessage.FILE_NOT_READABLE_MSG),
    })
    @PostMapping(value = "/{modelName}", consumes = "multipart/form-data" , produces = "application/json")
    public ResponseEntity<?> predictImage(@RequestParam("file") MultipartFile file, @PathVariable("modelName") String modelName) {
        try {
            log.info("Запрос на предсказание");
            PredictionResponse predict = productPredictionService.predict(file, modelName);
            return ResponseEntity.ok(predict);
        } catch (IOException e) {
            throw new PrsHttpException(ProductDeterminationErrors.FILE_NOT_READABLE, e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    /**
     * Получение списка всех возможный наиименований продуктов, которые содержатся в конкретной нейронной модели
     * @param modelName Наименование нейронной модели
     * @return Коллекция, которая представляет общий список со всеми возможными наименованиями продуктов
     */
    @ApiOperation(value = "${api.swagger.predict.labels}", response = String.class, responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = ProductDeterminationExceptionMessage.UNEXPECTED_ERROR_MSG),
            @ApiResponse(code = 403, message = ProductDeterminationExceptionMessage.ACCESS_DENIED_MSG),
            @ApiResponse(code = 422, message = ProductDeterminationExceptionMessage.FILE_NOT_READABLE_MSG),
    })
    @GetMapping("/labels/{modelName}")
    public ResponseEntity<?> getAllClassLabels(@PathVariable("modelName") String modelName) {
        try {
            List<String> classLabels = productPredictionService.getClassLabels(modelName);
            return ResponseEntity.ok(classLabels);
        } catch (IOException e) {
            throw new PrsHttpException(ProductDeterminationErrors.FILE_NOT_READABLE, e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }


    /**
     * Получение списка наименований нейронных моделей
     * @return Набор наименований нейронных моделей
     */
    @GetMapping("/aliases")
    @ApiOperation(value = "${api.swagger.predict.aliases}", response = String.class, responseContainer = "Set")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = ProductDeterminationExceptionMessage.UNEXPECTED_ERROR_MSG),
            @ApiResponse(code = 403, message = ProductDeterminationExceptionMessage.ACCESS_DENIED_MSG),
    })
    public ResponseEntity<?> getAllAliases() {
        Set<String> modelAliases = productPredictionService.getModelAliases();
        return ResponseEntity.ok(modelAliases);
    }
}
