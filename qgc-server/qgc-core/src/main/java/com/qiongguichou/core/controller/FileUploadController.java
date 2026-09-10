package com.qiongguichou.core.controller;

import com.qiongguichou.common.result.ErrorCode;
import com.qiongguichou.common.result.Result;
import com.qiongguichou.core.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件上传Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileUploadController {

    private final StorageService storageService;

    /**
     * 单文件上传
     */
    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error(ErrorCode.PARAM_ERROR,"文件不能为空");
        }
        try {
            String url = storageService.upload(
                    "proof",
                    file.getOriginalFilename(),
                    file.getInputStream(),
                    file.getContentType()
            );
            Map<String, String> data = new HashMap<>();
            data.put("url", url);
            data.put("originalName", file.getOriginalFilename());
            return Result.success(data);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.error(ErrorCode.PARAM_ERROR,"文件上传失败");
        }
    }

    /**
     * 多文件上传(最多9张)
     */
    @PostMapping("/upload/batch")
    public Result<List<String>> uploadBatch(@RequestParam("files") MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return Result.error(ErrorCode.PARAM_ERROR,"文件不能为空");
        }
        if (files.length > 9) {
            return Result.error(ErrorCode.PARAM_ERROR,"最多上传9张图片");
        }
        try {
            List<String> urls = new ArrayList<>();
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String url = storageService.upload(
                            "proof",
                            file.getOriginalFilename(),
                            file.getInputStream(),
                            file.getContentType()
                    );
                    urls.add(url);
                }
            }
            return Result.success(urls);
        } catch (IOException e) {
            log.error("批量上传失败", e);
            return Result.error(ErrorCode.PARAM_ERROR,"文件上传失败");
        }
    }
}