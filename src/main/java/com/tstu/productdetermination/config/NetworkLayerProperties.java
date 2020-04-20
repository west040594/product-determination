package com.tstu.productdetermination.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@ConfigurationProperties(value = "network")
@Getter
@Setter
public class NetworkLayerProperties {

    private Boolean save;
    private int epochs;
    private int batchSize;
    private int maxPathsPerLabel;
    private String filesPath;
    private String labelsFilesPath;
    private String modelsFilesPath;
    private LayerOptions layer;
    private PredictOptions predict;
    private Double splitTrainTestRatio;

    @Getter
    @Setter
    public static class LayerOptions {
        private int height;
        private int width;
        private int channels;
        private long seed;
        private Random rng = new Random(seed);
    }

    @Getter
    @Setter
    public static class PredictOptions {
        /**
         * Ограничение коэфициента. Все коэфициенты которые ниже этого значения, не будут добавлены в результат предсказания
         */
        private Double constraintRatio;
    }
}
