package com.qiongguichou.core.storage;

import java.io.InputStream;

/**
 * 对象存储抽象接口
 */
public interface StorageService {

    /**
     * 上传文件
     * @param directory 目录
     * @param filename 文件名
     * @param inputStream 文件流
     * @param contentType 内容类型
     * @return 文件访问URL
     */
    String upload(String directory, String filename, InputStream inputStream, String contentType);

    /**
     * 删除文件
     * @param fileUrl 文件URL
     */
    void delete(String fileUrl);

    /**
     * 获取文件URL
     * @param directory 目录
     * @param filename 文件名
     * @return 文件访问URL
     */
    String getUrl(String directory, String filename);
}