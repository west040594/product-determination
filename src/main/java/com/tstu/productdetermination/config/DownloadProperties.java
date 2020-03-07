package com.tstu.productdetermination.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(value = "download")
@Getter
@Setter
public class DownloadProperties {

    private String uploadDir;
    private String imageDir;
    private FilesInfo energyDrinks;
    private FilesInfo animals;

    @Getter
    @Setter
    public static class FilesInfo {
        private String url;
        private String zipFile;
        private String md5;
        private String dataSize;
    }

}
