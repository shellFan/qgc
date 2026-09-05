package com.qiongguichou.core.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * 本地文件存储(开发环境)
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "qgc.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements StorageService {

    @Value("${qgc.storage.local.path:./uploads}")
    private String basePath;

    @Value("${qgc.storage.local.url-prefix:/uploads}")
    private String urlPrefix;

    @Override
    public String upload(String directory, String filename, InputStream inputStream, String contentType) {
        try {
            String ext = getFileExtension(filename);
            String newFilename = UUID.randomUUID().toString().replace("-", "") + ext;
            Path dirPath = Paths.get(basePath, directory);
            Files.createDirectories(dirPath);
            Path filePath = dirPath.resolve(newFilename);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            return urlPrefix + "/" + directory + "/" + newFilename;
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败", e);
        }
    }

    @Override
    public void delete(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith(urlPrefix)) {
            return;
        }
        String relativePath = fileUrl.substring(urlPrefix.length() + 1);
        Path filePath = Paths.get(basePath, relativePath);
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("文件删除失败: {}", filePath, e);
        }
    }

    @Override
    public String getUrl(String directory, String filename) {
        return urlPrefix + "/" + directory + "/" + filename;
    }

    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0) {
            return filename.substring(dotIndex).toLowerCase();
        }
        return "";
    }
}