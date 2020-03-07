package com.tstu.productdetermination.service;

import com.tstu.productdetermination.NetworkLayers;
import com.tstu.productdetermination.config.NetworkLayerProperties;
import com.tstu.productdetermination.utils.DownloaderUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.datavec.api.io.filters.BalancedPathFilter;
import org.datavec.api.io.labels.ParentPathLabelGenerator;
import org.datavec.api.split.FileSplit;
import org.datavec.api.split.InputSplit;
import org.datavec.image.loader.NativeImageLoader;
import org.datavec.image.recordreader.ImageRecordReader;
import org.datavec.image.transform.ImageTransform;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import static java.lang.Math.toIntExact;

@Component
@Slf4j
@RequiredArgsConstructor
public class ProductClassificationServiceImpl implements ProductClassificationService {

    private final NetworkLayerProperties properties;
    private final ImageTransform transform;
    private final DataNormalization scaler;
    private final DownloaderUtility downloaderUtility;

    public void train(String modelName) throws IOException {
        ParentPathLabelGenerator labelMaker = new ParentPathLabelGenerator();
        FileSplit fileSplit = buildFileSplit(modelName);
        int numExamples = toIntExact(fileSplit.length());
        int numLabels = Objects.requireNonNull(fileSplit.getRootDir().listFiles(File::isDirectory)).length;
        BalancedPathFilter pathFilter = new BalancedPathFilter(
                properties.getLayer().getRng(),
                labelMaker,
                numExamples,
                numLabels,
                properties.getMaxPathsPerLabel());


        double splitTrainTest = 0.8;
        InputSplit[] inputSplit = fileSplit.sample(pathFilter, splitTrainTest, 1 - splitTrainTest);
        InputSplit trainData = inputSplit[0];
        InputSplit testData = inputSplit[1];


        MultiLayerNetwork network = buildModel(numLabels);
        ImageRecordReader trainRR = new ImageRecordReader(
                properties.getLayer().getHeight(),
                properties.getLayer().getWidth(),
                properties.getLayer().getChannels(),
                labelMaker);


        log.info("Тренируем модель....");
        DataSetIterator trainIter;
        // Train without transformations
        trainRR.initialize(trainData, null);
        trainIter = new RecordReaderDataSetIterator(trainRR, properties.getBatchSize(), 1, numLabels);
        scaler.fit(trainIter);
        trainIter.setPreProcessor(scaler);
        network.fit(trainIter, properties.getEpochs());

        // Train with transformations
        trainRR.initialize(trainData, transform);
        trainIter = new RecordReaderDataSetIterator(trainRR, properties.getBatchSize(), 1, numLabels);
        scaler.fit(trainIter);
        trainIter.setPreProcessor(scaler);
        network.fit(trainIter, properties.getEpochs());


        if (properties.getSave()) {
            //saveTrainData(modelName, trainIter);
            List<String> allClassLabels = trainRR.getLabels();
            saveModelData(modelName, network, allClassLabels);
        }
        log.info("****************Example finished********************");
    }


    private FileSplit buildFileSplit(String modelName) throws IOException {
        String dataLocalPath = downloaderUtility.Download(modelName);
        File mainPath = new File(dataLocalPath);
        return new FileSplit(mainPath, NativeImageLoader.ALLOWED_FORMATS, properties.getLayer().getRng());
    }

    /**
     * Формирование модели
     * @param numLabels
     * @return
     */
    private MultiLayerNetwork buildModel(int numLabels) {
        log.info("Собираем нейронную модель....");
        MultiLayerNetwork network = NetworkLayers.builder()
                .height(properties.getLayer().getHeight())
                .width(properties.getLayer().getWidth())
                .channels(properties.getLayer().getChannels())
                .seed(properties.getLayer().getSeed())
                .numLabels(numLabels)
                .build().lenetModel();
        network.init();
        return network;
    }

    /**
     * Сохранене нейронной модели и всех наименований продуктов
     * @param network
     * @param allClassLabels
     * @throws IOException
     */
    private void saveModelData(String modelName, MultiLayerNetwork network, List<String> allClassLabels) throws IOException {
        log.info("Сохраняем модель....");
        Path modelSavePath = Paths.get(properties.getModelsFilesPath(), modelName + "-model.zip");
        Path labelsSavePath = Paths.get(properties.getLabelsFilesPath(), modelName + "-labels.txt");
        FileUtils.writeLines(new File(labelsSavePath.toUri()), allClassLabels);
        network.save(new File(modelSavePath.toUri()));
    }

    /**
     * Сохранение тренировачных данных
     * @param trainIter
     */
    private void saveTrainData(String modelName, DataSetIterator trainIter) {
        log.info("Сохраняем тренировачные данные...");
        trainIter.reset();
        while (trainIter.hasNext()) {
            Path trainDataSavePath = Paths.get(properties.getFilesPath() + "/train", "train-data-" + modelName + ".bin" );
            trainIter.next().save(new File(trainDataSavePath.toUri()));
        }
    }
}
