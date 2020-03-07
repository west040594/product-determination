package com.tstu.productdetermination.service;

import java.io.IOException;

public interface ProductClassificationService {
    /**
     * Сформировать нейронную модель продуктов
     * @throws IOException
     */
    void train(String modelName) throws IOException;
}
