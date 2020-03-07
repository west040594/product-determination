package com.tstu.productdetermination.service;

import org.springframework.core.io.Resource;

public interface FileStorageService {
    Resource loadFileAsResource(String fileName);
}
