package com.tstu.productdetermination.utils;

import com.tstu.commons.exception.PrsException;
import com.tstu.commons.exception.PrsHttpException;
import com.tstu.productdetermination.config.DownloadProperties;
import com.tstu.productdetermination.exception.ProductDeterminationErrors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.nd4j.resources.Downloader;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DownloaderUtility {

    private final DownloadProperties downloadProperties;
    private final Map<String, DownloadProperties.FilesInfo> modelTypes;

    public String Download(String modelName) throws IOException {
        if(!modelTypes.containsKey(modelName)) {
            throw new PrsException(ProductDeterminationErrors.TYPE_OF_PRODUCT_DOES_NOT_SUPPORT);
        }
        DownloadProperties.FilesInfo filesInfo = modelTypes.get(modelName);

        String resourceName = filesInfo.getZipFile().substring(0, filesInfo.getZipFile().lastIndexOf(".zip"));
        Path downloadPath = Paths.get(System.getProperty("java.io.tmpdir"), filesInfo.getZipFile());
        Path extractPath = Paths.get(downloadProperties.getImageDir());
        String dataPathLocal = FilenameUtils.concat(extractPath.toString(),resourceName);
        int downloadRetries = 10;
        if (!new File(dataPathLocal).exists()) {
            System.out.println("_______________________________________________________________________");
            System.out.println("Скачиваем данные ("+filesInfo.getDataSize()+") и распаковываем в \n\t" + dataPathLocal);
            System.out.println("_______________________________________________________________________");
            Downloader.downloadAndExtract("files",
                    new URL(filesInfo.getUrl()),
                    new File(downloadPath.toUri()),
                    new File(extractPath.toUri()),
                    filesInfo.getMd5(),
                    downloadRetries);
        } else {
            System.out.println("_______________________________________________________________________");
            System.out.println("Данные сохранены в \n\t" + dataPathLocal);
            System.out.println("_______________________________________________________________________");
        }
        return dataPathLocal;
    }
}
