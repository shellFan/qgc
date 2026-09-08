package com.qiongguichou.core.storage;

import com.qiongguichou.common.exception.BusinessException;
import com.qiongguichou.common.result.ErrorCode;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 本地文件存储(开发环境)
 * RC5增强：文件类型白名单+大小校验+扩展名安全检查
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "qgc.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements StorageService {

    @Value("${qgc.storage.local.path:./uploads}")
    private String basePath;

    @Value("${qgc.storage.local.url-prefix:/uploads}")
    private String urlPrefix;

    @Value("${qgc.upload.max-size:5242880}")
    private long maxFileSize;

    /** 允许的文件扩展名白名单 */
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
            ".jpg", ".jpeg", ".png", ".webp", ".gif", ".bmp"
    ));

    /** 允许的Content-Type白名单 */
    private static final Set<String> ALLOWED_CONTENT_TYPES = new HashSet<>(Arrays.asList(
            "image/jpeg", "image/png", "image/webp", "image/gif", "image/bmp"
    ));

    /** 危险扩展名黑名单(双重保护) */
    private static final Set<String> DANGEROUS_EXTENSIONS = new HashSet<>(Arrays.asList(
            ".exe", ".bat", ".cmd", ".com", ".sh", ".html", ".htm", ".js",
            ".svg", ".php", ".jsp", ".asp", ".aspx", ".py", ".rb", ".pl"
    ));

    @Override
    public String upload(String directory, String filename, InputStream inputStream, String contentType) {
        // 1. 文件名校验
        if (filename == null || filename.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "文件名不能为空");
        }

        // 2. 扩展名白名单校验
        String ext = getFileExtension(filename).toLowerCase();
        if (ext.isEmpty() || !ALLOWED_EXTENSIONS.contains(ext)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR,
                    "不支持的文件类型，仅允许: jpg/jpeg/png/webp/gif/bmp");
        }

        // 3. 危险扩展名双重检查
        if (DANGEROUS_EXTENSIONS.contains(ext)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "禁止上传可执行文件");
        }

        // 4. Content-Type校验(如果提供)
        if (contentType != null && !contentType.isEmpty() && !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "不支持的文件Content-Type: " + contentType);
        }

        // 5. 文件大小校验
        try {
            long size = inputStream.available();
            if (size > maxFileSize) {
                throw new BusinessException(ErrorCode.PARAM_ERROR,
                        "文件大小超过限制(" + (maxFileSize / 1024 / 1024) + "MB)");
            }
            if (size <= 0) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "文件内容为空");
            }
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "文件读取失败");
        }

        // 6. 安全存储: UUID重命名，保留扩展名
        try {
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