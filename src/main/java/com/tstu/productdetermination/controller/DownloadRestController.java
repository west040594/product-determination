package com.tstu.productdetermination.controller;


import com.tstu.productdetermination.exception.ProductDeterminationExceptionMessage;
import com.tstu.productdetermination.service.FileStorageService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@RestController
@CrossOrigin
@Slf4j
@RequiredArgsConstructor
@RequestMapping(value = "api/v1/determination/download")
public class DownloadRestController {

    private final FileStorageService fileStorageService;

    /**
     * Скачать файл с сервера по его наименованию.
     * @param fileName  Наименование файла
     * @return Файл для загрузки
     */

    @ApiOperation(value = "${api.swagger.download.file}", response = UrlResource.class)
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = ProductDeterminationExceptionMessage.UNEXPECTED_ERROR_MSG),
            @ApiResponse(code = 403, message = ProductDeterminationExceptionMessage.ACCESS_DENIED_MSG),
            @ApiResponse(code = 404, message = ProductDeterminationExceptionMessage.FILE_NOT_FOUND_MSG),
    })
    @GetMapping("/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        Resource resource = fileStorageService.loadFileAsResource(fileName);
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            contentType = "application/octet-stream";
            log.info("Не удалось определить тип файла.");
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
