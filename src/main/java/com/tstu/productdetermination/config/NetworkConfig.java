package com.tstu.productdetermination.config;


import lombok.extern.slf4j.Slf4j;
import org.datavec.image.loader.NativeImageLoader;
import org.datavec.image.transform.FlipImageTransform;
import org.datavec.image.transform.ImageTransform;
import org.datavec.image.transform.PipelineImageTransform;
import org.datavec.image.transform.WarpImageTransform;
import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.api.storage.StatsStorageRouter;
import org.deeplearning4j.api.storage.impl.RemoteUIStatsStorageRouter;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.storage.InMemoryStatsStorage;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.nd4j.linalg.primitives.Pair;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.Random;


@Configuration
@Slf4j
public class NetworkConfig {

    @Bean
    public UIServer getUISerer() {
        UIServer instance = UIServer.getInstance();
        return instance;
    }

    @Bean
    public StatsStorage statsStorage(UIServer uiServer) {
        InMemoryStatsStorage inMemoryStatsStorage = new InMemoryStatsStorage();
        uiServer.attach(inMemoryStatsStorage);
        return inMemoryStatsStorage;
    }

    @Bean
    public PipelineImageTransform transform(NetworkLayerProperties properties) {
        ImageTransform flipTransform1 = new FlipImageTransform(properties.getLayer().getRng());
        ImageTransform flipTransform2 = new FlipImageTransform(new Random(123));
        ImageTransform warpTransform = new WarpImageTransform(properties.getLayer().getRng(), 42);
        boolean shuffle = false;
        List<Pair<ImageTransform,Double>> pipeline = Arrays.asList(new Pair<>(flipTransform1,0.9),
                new Pair<>(flipTransform2,0.8),
                new Pair<>(warpTransform,0.5));

        return new PipelineImageTransform(pipeline,shuffle);
    }

    @Bean
    public DataNormalization scaler() {
        return new ImagePreProcessingScaler(0, 1);
    }

    @Bean
    public NativeImageLoader loader(NetworkLayerProperties properties) {
        return new NativeImageLoader(
                properties.getLayer().getHeight(),
                properties.getLayer().getWidth(),
                properties.getLayer().getChannels());
    }

}
