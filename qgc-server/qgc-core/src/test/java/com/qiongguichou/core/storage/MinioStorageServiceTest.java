package com.qiongguichou.core.storage;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MinioStorageServiceTest {

    @Test
    void keepsQgcObjectsAndBuildsPublicUrl() {
        MinioStorageService service = new MinioStorageService();
        service.setExternalUrl("https://mall.21zuo.com/minio/mall-public");

        assertEquals("qgc/proof/image.png", service.buildObjectName("proof", "image.png"));
        assertEquals("https://mall.21zuo.com/minio/mall-public/qgc/proof/image.png",
                service.buildUrl("qgc/proof/image.png"));
    }
}
