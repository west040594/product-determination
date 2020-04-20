package com.tstu.productdetermination.service;

import com.tstu.productdetermination.config.NetworkLayerProperties;
import com.tstu.productdetermination.models.NetworkModelData;
import com.tstu.productdetermination.utils.DownloaderUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.datavec.api.io.labels.ParentPathLabelGenerator;
import org.datavec.api.split.FileSplit;
import org.datavec.image.loader.NativeImageLoader;
import org.datavec.image.recordreader.ImageRecordReader;
import org.datavec.image.transform.ImageTransform;
import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.api.InvocationType;
import org.deeplearning4j.optimize.listeners.EvaluativeListener;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.stats.StatsListener;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class ProductClassificationServiceImpl implements ProductClassificationService {

    private final NetworkLayerProperties properties;
    private final UIServer uiServer;
    private final StatsStorage statsStorage;
    private final ImageTransform transform;
    private final DataNormalization scaler;
    private final DownloaderUtility downloaderUtility;

    @Override
    public NetworkLayerProperties getProperties() {
        return properties;
    }

    public void train(String modelName) throws IOException {
        ParentPathLabelGenerator labelMaker = new ParentPathLabelGenerator();
        //Создаем file split
        FileSplit fileSplit = buildFileSplit(modelName);

        //Создаем данные для тренировки и тестирования
        NetworkModelData data = buildModelData(fileSplit, labelMaker);

        //Получаем количество наименований и формируем нейронную модель
        int numLabels = getNumberOfLabels(fileSplit);
        MultiLayerNetwork network = buildNetwork(numLabels);

        //Создаем image рекордеры для тестовых и тренировочных данных
        ImageRecordReader testImageRecordReader = buildImageRecordReader(labelMaker);
        ImageRecordReader trainImageRecordReader = buildImageRecordReader(labelMaker);

        //Обрабатываем данные для тестирования
        testImageRecordReader.initialize(data.getTestData());
        DataSetIterator testDataSetIterator = buildDataSetIterator(numLabels, testImageRecordReader);

        network.setListeners(
                new StatsListener(statsStorage, 1),
                new ScoreIterationListener(1),
                new EvaluativeListener(testDataSetIterator, 1, InvocationType.EPOCH_END));

        // Обрабатываем данные для тренировки без трансформации. и тренируем модель
        trainImageRecordReader.initialize(data.getTrainData());
        fitNetwork(numLabels, properties.getEpochs(), network, trainImageRecordReader);

        // Обрабатываем данные для тренировки с трансформацией. и тренируем модель еще раз
        trainImageRecordReader.initialize(data.getTrainData(), transform);
        fitNetwork(numLabels, properties.getEpochs(), network, trainImageRecordReader);

        if (properties.getSave()) {
            List<String> allClassLabels = trainImageRecordReader.getLabels();
            saveModelData(modelName, network, allClassLabels);
        }
        log.info("Завершение тренировки");
    }


    @Override
    public DataSetIterator buildDataSetIterator(int numLabels, ImageRecordReader imageRecordReader)  {
        DataSetIterator dataSetIterator=
                new RecordReaderDataSetIterator(imageRecordReader, properties.getBatchSize(), 1, numLabels);
        scaler.fit(dataSetIterator);
        dataSetIterator.setPreProcessor(scaler);
        return dataSetIterator;
    }


    @Override
    public FileSplit buildFileSplit(String modelName) throws IOException {
        String dataLocalPath = downloaderUtility.Download(modelName);
        File mainPath = new File(dataLocalPath);
        return new FileSplit(mainPath, NativeImageLoader.ALLOWED_FORMATS, properties.getLayer().getRng());
    }


    @Override
    public void saveModelData(String modelName, MultiLayerNetwork network, List<String> allClassLabels) throws IOException {
        log.info("Сохраняем модель....");
        Path modelSavePath = Paths.get(properties.getModelsFilesPath(), modelName + "-model.zip");
        Path labelsSavePath = Paths.get(properties.getLabelsFilesPath(), modelName + "-labels.txt");
        FileUtils.writeLines(new File(labelsSavePath.toUri()), allClassLabels);
        network.save(new File(modelSavePath.toUri()));
    }

    @Override
    public void saveTrainData(String modelName, DataSetIterator dataSetIterator) {
        log.info("Сохраняем тренировачные данные...");
        dataSetIterator.reset();
        while (dataSetIterator.hasNext()) {
            Path trainDataSavePath = Paths.get(properties.getFilesPath() + "/train", "train-data-" + modelName + ".bin" );
            dataSetIterator.next().save(new File(trainDataSavePath.toUri()));
        }
    }

}
