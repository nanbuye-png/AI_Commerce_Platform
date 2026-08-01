package com.commerce.platform.common.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 静态文件访问 Controller
 * 通过 /uploads/** 提供上传的商品图片访问
 */
@Slf4j
@RestController
public class FileController {

    /** 上传根目录：项目运行目录 /uploads */
    private static final Path UPLOAD_ROOT = Paths.get("uploads").toAbsolutePath().normalize();

    @GetMapping("/uploads/{year}/{month}/{day}/{filename:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String year,
                                              @PathVariable String month,
                                              @PathVariable String day,
                                              @PathVariable String filename) {
        try {
            Path filePath = UPLOAD_ROOT.resolve(year).resolve(month).resolve(day).resolve(filename).normalize();
            // 防止路径穿越
            if (!filePath.startsWith(UPLOAD_ROOT) || !Files.exists(filePath) || !Files.isRegularFile(filePath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(filePath);
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .body(resource);
        } catch (Exception e) {
            log.error("Failed to serve upload file: {} {}/{}/{}", year, month, day, filename, e);
            return ResponseEntity.notFound().build();
        }
    }
}