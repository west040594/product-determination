package com.tstu.productdetermination.service;

import com.tstu.commons.exception.PrsHttpException;
import com.tstu.productdetermination.config.DownloadProperties;
import com.tstu.productdetermination.exception.ProductDeterminationErrors;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    private final DownloadProperties properties;
    private final Map<String, DownloadProperties.FilesInfo> modelTypes;

    @Override
    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = Paths.get(properties.getUploadDir() +  "/" + fileName).toAbsolutePath().normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists()) {
                return resource;
            } else {
                throw new PrsHttpException(ProductDeterminationErrors.FILE_NOT_FOUND, fileName + " не найден", HttpStatus.NOT_FOUND);
            }
        } catch (MalformedURLException ex) {
            throw new PrsHttpException(ProductDeterminationErrors.FILE_NOT_FOUND, ex.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}
