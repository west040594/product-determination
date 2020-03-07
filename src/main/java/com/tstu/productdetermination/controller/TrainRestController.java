package com.tstu.productdetermination.controller;

import com.tstu.commons.exception.PrsException;
import com.tstu.commons.exception.PrsHttpException;
import com.tstu.productdetermination.exception.ProductDeterminationErrors;
import com.tstu.productdetermination.exception.ProductDeterminationExceptionMessage;
import com.tstu.productdetermination.service.ProductClassificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
            return new ResponseEntity<>("Создание модели в данный момент уже запущено", HttpStatus.NOT_ACCEPTABLE);
        }
    }
}
