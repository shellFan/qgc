package com.qiongguichou.core.storage;

import com.qiongguichou.common.exception.BusinessException;
import com.qiongguichou.common.result.ErrorCode;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * MinIO 对象存储。QGC 所有对象固定放在 qgc/ 前缀下，避免污染 Mall 的业务目录。
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "qgc.storage.type", havingValue = "minio")
public class MinioStorageService implements StorageService {

    @Value("${qgc.storage.minio.endpoint:}")
    private String endpoint;

    @Value("${qgc.storage.minio.access-key:}")
    private String accessKey;

    @Value("${qgc.storage.minio.secret-key:}")
    private String secretKey;

    @Value("${qgc.storage.minio.bucket:mall-public}")
    private String bucket;

    @Setter
    @Value("${qgc.storage.minio.external-url:}")
    private String externalUrl;

    @Value("${qgc.upload.max-size:5242880}")
    private long maxFileSize;

    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
            ".jpg", ".jpeg", ".png", ".webp", ".gif", ".bmp"
    ));
    private static final Set<String> ALLOWED_CONTENT_TYPES = new HashSet<>(Arrays.asList(
            "image/jpeg", "image/png", "image/webp", "image/gif", "image/bmp"
    ));

    @Override
    public String upload(String directory, String filename, InputStream inputStream, String contentType) {
        validate(filename, contentType, inputStream);
        String ext = extension(filename);
        String objectName = buildObjectName(directory, UUID.randomUUID().toString().replace("-", "") + ext);
        try {
            MinioClient client = client();
            ensureBucket(client);
            long size = inputStream.available();
            client.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(inputStream, size, -1)
                    .contentType(contentType)
                    .build());
            return buildUrl(objectName);
        } catch (Exception e) {
            log.error("MinIO 上传失败, object={}", objectName, e);
            throw new RuntimeException("文件上传失败", e);
        }
    }

    @Override
    public void delete(String fileUrl) {
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            return;
        }
        String base = normalizedExternalUrl();
        if (!fileUrl.startsWith(base + "/")) {
            return;
        }
        String objectName = fileUrl.substring(base.length() + 1);
        try {
            client().removeObject(RemoveObjectArgs.builder().bucket(bucket).object(objectName).build());
        } catch (Exception e) {
            log.warn("MinIO 删除失败, object={}", objectName, e);
        }
    }

    @Override
    public String getUrl(String directory, String filename) {
        return buildUrl(buildObjectName(directory, filename));
    }

    String buildObjectName(String directory, String filename) {
        String safeDirectory = directory == null ? "common" : directory.replaceAll("[^a-zA-Z0-9/_-]", "");
        String safeFilename = filename == null ? "" : filename.replaceAll("[^a-zA-Z0-9._-]", "");
        return "qgc/" + safeDirectory + "/" + safeFilename;
    }

    String buildUrl(String objectName) {
        return normalizedExternalUrl() + "/" + objectName;
    }

    private MinioClient client() {
        if (endpoint.trim().isEmpty() || accessKey.trim().isEmpty() || secretKey.trim().isEmpty()) {
            throw new IllegalStateException("MinIO 配置不完整");
        }
        return MinioClient.builder().endpoint(endpoint).credentials(accessKey, secretKey).build();
    }

    private void ensureBucket(MinioClient client) throws Exception {
        if (!client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
            client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
    }

    private void validate(String filename, String contentType, InputStream inputStream) {
        String ext = extension(filename);
        if (filename == null || filename.trim().isEmpty() || !ALLOWED_EXTENSIONS.contains(ext)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "不支持的图片类型");
        }
        if (contentType != null && !contentType.trim().isEmpty() && !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "不支持的图片Content-Type");
        }
        try {
            long size = inputStream.available();
            if (size <= 0 || size > maxFileSize) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "图片大小不合法");
            }
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "文件读取失败");
        }
    }

    private String extension(String filename) {
        if (filename == null) return "";
        int index = filename.lastIndexOf('.');
        return index > 0 ? filename.substring(index).toLowerCase() : "";
    }

    private String normalizedExternalUrl() {
        if (externalUrl != null && !externalUrl.trim().isEmpty()) {
            return externalUrl.endsWith("/") ? externalUrl.substring(0, externalUrl.length() - 1) : externalUrl;
        }
        return endpoint + "/" + bucket;
    }
}
