package com.tstu.productdetermination.service;

import com.tstu.productdetermination.NetworkLayers;
import com.tstu.productdetermination.config.NetworkLayerProperties;
import com.tstu.productdetermination.models.NetworkModelData;
import org.datavec.api.io.filters.BalancedPathFilter;
import org.datavec.api.io.labels.ParentPathLabelGenerator;
import org.datavec.api.split.FileSplit;
import org.datavec.api.split.InputSplit;
import org.datavec.image.recordreader.ImageRecordReader;
import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.storage.FileStatsStorage;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static java.lang.Math.toIntExact;

public interface ProductClassificationService {

    /**
     * Получить настройки формирования нейронной модели
     * @return
     */
    NetworkLayerProperties getProperties();

    /**
     * Сформировать нейронную модель продуктов
     * @throws IOException
     */
    void train(String modelName) throws IOException;

    /**
     * Обработка данных модели и создание DataSetIterator
     * @param numLabels количество наименований
     * @param imageRecordReader
     * @return DataSetIterator
     */
    DataSetIterator buildDataSetIterator(int numLabels, ImageRecordReader imageRecordReader);

    /**
     * Получение объекта fileSplit по наименованию модели
     * 1. Скачать все папки с изображениями данной модели
     * 2. Получить корневой путь к ним
     * 3. Создать объект FileSplit на основе пути и вернуть
     * @param modelName Наименование модели, которая будет тренироваться
     * @return объект fileSplit
     * @throws IOException
     */
    FileSplit buildFileSplit(String modelName) throws IOException;


    /**
     * Сохранене нейронной модели и всех наименований продуктов
     * @param network Построенная нейронна модель
     * @param allClassLabels Список всех возможных наименований продуктов
     * @throws IOException
     */
    void saveModelData(String modelName, MultiLayerNetwork network, List<String> allClassLabels) throws IOException;

    /**
     * Сохранение тренировачных/тестовых данных
     * @param dataSetIterator
     */
    void saveTrainData(String modelName, DataSetIterator dataSetIterator);

    /**
     * Тренировать нейронную модель
     * @param numLabels Количество наименований
     * @param network нейронная модель
     * @param imageRecordReader
     */
    default void fitNetwork(int numLabels, int epochs,
                            MultiLayerNetwork network, ImageRecordReader imageRecordReader) {
        DataSetIterator dataSetIterator = buildDataSetIterator(numLabels, imageRecordReader);
        network.fit(dataSetIterator, epochs);
    }


    /**
     * Формирование модели
     * @param numLabels Количество наименований продуктов в модели
     * @return Построенная нейронная модель
     */
    default MultiLayerNetwork buildNetwork(int numLabels) {
        NetworkLayerProperties properties = getProperties();
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
     * Метод создает тренировочные и тестовые данные для нейронной мождели
     * @return Объект который хранит тестовые и тренировочные данные
     */
    default NetworkModelData buildModelData(FileSplit fileSplit, ParentPathLabelGenerator labelMaker) {
        NetworkLayerProperties properties = getProperties();
        int numLabels = getNumberOfLabels(fileSplit);
        int numExamples = toIntExact(fileSplit.length());
        BalancedPathFilter pathFilter = new BalancedPathFilter(
                properties.getLayer().getRng(), labelMaker, numExamples,
                numLabels, properties.getMaxPathsPerLabel());
        //Формируем данные для модели на основе коэфициента разделения и pathFilter
        InputSplit[] inputSplit = fileSplit.sample(pathFilter,
                properties.getSplitTrainTestRatio(),
                1 - properties.getSplitTrainTestRatio());
        InputSplit trainData = inputSplit[0];
        InputSplit testData = inputSplit[1];
        return new NetworkModelData(trainData, testData);
    }

    /**
     * Создания объекта ImageRecordReader
     */
    default ImageRecordReader buildImageRecordReader(ParentPathLabelGenerator labelMaker) {
        NetworkLayerProperties properties = getProperties();
        return new ImageRecordReader(
                properties.getLayer().getHeight(),
                properties.getLayer().getWidth(),
                properties.getLayer().getChannels(),
                labelMaker);
    }

    /**
     * Получить количество под-папок которые содержатся в  корневой папки fileSplit
     * @param fileSplit Объект FileSplit
     * @return количество под-папок.
     */
    default int getNumberOfLabels(FileSplit fileSplit) {
        return Objects.requireNonNull(fileSplit.getRootDir().listFiles(File::isDirectory)).length;
    }
}
