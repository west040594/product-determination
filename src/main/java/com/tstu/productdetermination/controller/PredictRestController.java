package com.tstu.productdetermination.controller;

import com.tstu.commons.dto.http.response.determination.PredictionResponse;
import com.tstu.commons.exception.PrsHttpException;
import com.tstu.productdetermination.config.DownloadProperties;
import com.tstu.productdetermination.exception.ProductDeterminationErrors;
import com.tstu.productdetermination.service.ProductPredictionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@Slf4j
@RequiredArgsConstructor
@RequestMapping(value = "api/v1/determination/predict")
public class PredictRestController {

    private final ProductPredictionService productPredictionService;
    private final Map<String, DownloadProperties.FilesInfo> modelTypes;

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

    @GetMapping("/labels/{modelName}")
    public ResponseEntity<?> getAllClassLabels(@PathVariable("modelName") String modelName) {
        try {
            List<String> classLabels = productPredictionService.getClassLabels(modelName);
            return ResponseEntity.ok(classLabels);
        } catch (IOException e) {
            throw new PrsHttpException(ProductDeterminationErrors.FILE_NOT_READABLE, e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }


    @GetMapping("/aliases")
    public ResponseEntity<?> getAllAliases() {
        return ResponseEntity.ok(modelTypes.keySet());
    }
}
