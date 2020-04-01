package com.tstu.productdetermination.config;


import lombok.extern.slf4j.Slf4j;
import org.datavec.image.loader.NativeImageLoader;
import org.datavec.image.transform.FlipImageTransform;
import org.datavec.image.transform.ImageTransform;
import org.datavec.image.transform.PipelineImageTransform;
import org.datavec.image.transform.WarpImageTransform;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.nd4j.linalg.primitives.Pair;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.*;

import static com.tstu.commons.constants.NetworkModelTypes.ANIMALS;
import static com.tstu.commons.constants.NetworkModelTypes.ENERGY_DRINKS;


@Configuration
@Slf4j
public class NetworkConfig {

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

    @Bean
    public Map<String, DownloadProperties.FilesInfo> modelTypes(DownloadProperties downloadProperties) {
        HashMap<String, DownloadProperties.FilesInfo> modelTypes = new HashMap<>();
        modelTypes.put(ENERGY_DRINKS, downloadProperties.getEnergyDrinks());
        modelTypes.put("beer", downloadProperties.getBeer());
        return modelTypes;
    }

}
