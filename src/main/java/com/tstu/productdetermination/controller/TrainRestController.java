package com.tstu.productdetermination.controller;

import com.tstu.commons.exception.PrsException;
import com.tstu.commons.exception.PrsHttpException;
import com.tstu.productdetermination.exception.ProductDeterminationErrors;
import com.tstu.productdetermination.exception.ProductDeterminationExceptionMessage;
import com.tstu.productdetermination.service.ProductClassificationService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.tstu.productdetermination.exception.ProductDeterminationErrors.PROCESS_OF_CREATE_MODEL_IS_RUNNING;

@RestController
@Slf4j
@RequestMapping("api/v1/determination/train")
public class TrainRestController {

    private ProductClassificationService productClassificationService;
    private ExecutorService executorService;


    public TrainRestController(ProductClassificationService productClassificationService) {
        this.productClassificationService = productClassificationService;
        this.executorService = Executors.newSingleThreadExecutor();
        executorService.shutdownNow();
    }

    /**
     * Запуск процесса тренировки модели
     * @param modelName Наименование модели
     * @return Строка статуса о запуске тренировки
     */
    @ApiOperation(value = "${api.swagger.train.model}", response = String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = ProductDeterminationExceptionMessage.UNEXPECTED_ERROR_MSG),
            @ApiResponse(code = 403, message = ProductDeterminationExceptionMessage.ACCESS_DENIED_MSG),
            @ApiResponse(code = 406, message = ProductDeterminationExceptionMessage.PROCESS_OF_CREATE_MODEL_IS_RUNNING_MSG),
    })
    @PostMapping("/{modelName}")
    public ResponseEntity<?> trainModel(@PathVariable("modelName") String modelName) {
        if(executorService.isTerminated()) {
            executorService = Executors.newSingleThreadExecutor();
            executorService.execute(() -> {
                try {
                    productClassificationService.train(modelName);
                } catch (IOException | PrsException e) {
                    log.error(e.getMessage());
                }
                executorService.shutdown();
            });
            return new ResponseEntity<>("Процесс создания модели запущен", HttpStatus.ACCEPTED);
        } else {
            throw new PrsHttpException(PROCESS_OF_CREATE_MODEL_IS_RUNNING, HttpStatus.NOT_ACCEPTABLE);
        }
    }
}
