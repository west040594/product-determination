package com.tstu.productdetermination.service;

import com.tstu.commons.dto.http.response.determination.PredictionResponse;
import com.tstu.productdetermination.config.DownloadProperties;
import com.tstu.productdetermination.config.NetworkLayerProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.shade.guava.primitives.Doubles;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.IntStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductPredictionServiceImpl implements ProductPredictionService{

    private final DataNormalization scaler;
    private final NativeImageLoader loader;
    private final NetworkLayerProperties networkLayerProperties;
    private final DownloadProperties downloadProperties;


    @Override
    public PredictionResponse predict(MultipartFile file, String modelName) throws IOException {
        log.info("Предсказываем продукт по изображению. Модель - {}", modelName);
        List<String> allClassLabels = getClassLabels(modelName);
        MultiLayerNetwork network = loadModel(modelName);
        //Предикт по изображению
        INDArray image = loader.asMatrix(file.getInputStream());
        scaler.transform(image);
        INDArray predictArray = network.output(image, false);
        List<PredictionResponse.Item> predictionList = buildPredictionList(predictArray, allClassLabels);
        PredictionResponse predictionResponse = new PredictionResponse(predictionList);
        log.info("Продукты найдены. Ответ - {}", predictionResponse);
        return predictionResponse;
    }

    @Override
    public List<String> getClassLabels(String modelName) throws IOException {
        Path labelsSavePath = Paths.get(networkLayerProperties.getLabelsFilesPath(), modelName + "-labels.txt");
        return FileUtils.readLines(new File(labelsSavePath.toUri()), StandardCharsets.UTF_8);
    }

    @Override
    public Set<String> getModelAliases() {
        return downloadProperties.getModels().keySet();
    }

    /**
     * Загрузка обученной нейронной модели с диска
     * @return Нейронная модель
     * @throws IOException
     */
    private MultiLayerNetwork loadModel(String modelName) throws IOException {
        Path modelFilePath = Paths.get(networkLayerProperties.getModelsFilesPath(), modelName + "-model.zip");
        return ModelSerializer.restoreMultiLayerNetwork(new File(modelFilePath.toUri()));
    }

    /**
     * На основе массива предсказаний строится мапа где ключом выступает имя продукта, а значением его коэфициент совпадения
     * Если входной коэфициент совпадения ниже чем constraintCoef то в результирующую мапу этот продукт не добавляется
     * Алгоритм:
     * 1. Преобразовать массив предсказаний в лист double значений с коэфициентами совпадения
     * 2. Создания стрима по индексам double листа
     * 3. Фильтр тех значений у которых коэфициент >=  constraintRatio
     * 4. Сортировка по убыванию. В результирующей мапе сначало будут показываться результата у которых коэфициент совпадения больше
     * 5. Заполнение мапы. Ключ - наименование продукта, Значение - кэфициент его совпадения
     * @param predictArray Массив предсказаний
     * @param allLabels Список всех наименований продуктов
     * @return Коллекция  предметов предсказания
     */
    private List<PredictionResponse.Item> buildPredictionList(INDArray predictArray, List<String> allLabels) {
        List<PredictionResponse.Item> predictionList = new LinkedList<>();
        List<Double> doubleList = Doubles.asList(predictArray.toDoubleVector());
        IntStream.range(0, doubleList.size())
                .boxed()
                .filter(index -> doubleList.get(index) >= networkLayerProperties.getPredict().getConstraintRatio())
                .sorted(Comparator.comparingDouble(doubleList::get).reversed())
                .forEach(index -> {
                    predictionList.add(new PredictionResponse.Item(allLabels.get(index), doubleList.get(index)));
                });
        return predictionList;
    }
}
