package com.tstu.productdetermination.service;

import com.tstu.commons.dto.http.response.determination.PredictionResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ProductPredictionService {

    /**
     * Определение продукта на основе изображения(файла)
     * Алгоритм:
     * 1. Взять список всех возможных наименований продуктов нейронной модели
     * 2. Загрузить тренированную модель
     * 3. Преобразовать входное изображение в матрицу
     * 4. Сделать необходимые трансформации над матрицей изображения
     * 5. Сделать предзакание по данной модели
     * 6. Софрмировать список наиболее подходящих наименований продукта под входное изображение (коэфициент совпадения должен быть более 0.1)
     * 7. Сформировать структуру ответа предсказния
     * @param file Изображение продукта
     * @return Структура ответа предсказния
     * @throws IOException В случае если не удается прочитать изображение
     */
    PredictionResponse predict(MultipartFile file, String modelName) throws IOException;

    /**
     * Получение всех возможных наименований продуктов тренировачной модели
     * @return Список наименований продуктов
     * @throws IOException В случае если не удалось прочитать текстовый файл с наименованиями
     */
    List<String> getClassLabels(String labelsName) throws IOException;
}
