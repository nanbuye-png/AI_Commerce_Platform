package com.commerce.platform.common.controller;

import com.commerce.platform.common.entity.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 文件上传 Controller
 * 将图片保存到本地 uploads/ 目录，返回可访问的完整 URL：http://host/uploads/...
 */
@Slf4j
@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp", "bmp", "svg"
    );

    @Value("${app.upload.max-size-mb:5}")
    private long maxSizeMb;

    /** 上传目录：项目根目录 /uploads */
    private static final Path UPLOAD_ROOT = Paths.get("uploads").toAbsolutePath().normalize();

    @PostMapping("/image")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file,
                                                   HttpServletRequest request) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }

        long maxBytes = maxSizeMb * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            throw new IllegalArgumentException("文件大小不能超过 " + maxSizeMb + "MB");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = getExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("不支持的图片格式: " + extension + "（支持 jpg/png/gif/webp/bmp/svg）");
        }

        try {
            // 按日期分目录：uploads/2026/08/01/uuid.jpg
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            Path targetDir = UPLOAD_ROOT.resolve(datePath).normalize();
            Files.createDirectories(targetDir);

            String filename = UUID.randomUUID().toString().replace("-", "") + "." + extension;
            Path targetFile = targetDir.resolve(filename).normalize();

            // 防止路径穿越
            if (!targetFile.startsWith(UPLOAD_ROOT)) {
                throw new IllegalArgumentException("非法路径");
            }

            Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);

            // 构造完整 URL，使 C 端(5173)/M 端(5174) 都能直接访问
            String scheme = request.getScheme();
            String host = request.getServerName();
            int port = request.getServerPort();
            String base = scheme + "://" + host + (port != 80 && port != 443 ? ":" + port : "");
            String relativePath = "/uploads/" + datePath + "/" + filename;
            String url = base + relativePath;

            log.info("Uploaded file: {} -> {} (url={})", originalFilename, targetFile, url);

            Map<String, String> data = new HashMap<>();
            data.put("url", url);
            return Result.success(data);
        } catch (IOException e) {
            log.error("File upload failed", e);
            throw new RuntimeException("文件上传失败，请稍后重试");
        }
    }

    private String getExtension(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "";
        }
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return "";
        }
        return filename.substring(dot + 1).toLowerCase();
    }
}