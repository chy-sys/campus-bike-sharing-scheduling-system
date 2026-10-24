package com.campus.bike.system.controller;

import com.campus.bike.common.R;
import com.campus.bike.common.exception.BizException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.UUID;

/**
 * 文件上传：报修拍照、车辆图片等
 */
@RestController
@RequestMapping("/api/files")
public class FileController {

    @Value("${file.upload-path:uploads/}")
    private String uploadPath;

    @Value("${file.access-prefix:/uploads/}")
    private String accessPrefix;

    @PostMapping("/upload")
    public R<String> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException("上传文件不能为空");
        }

        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.lastIndexOf('.') >= 0) {
            ext = original.substring(original.lastIndexOf('.'));
        }
        String filename = UUID.randomUUID().toString().replace("-", "") + ext;

        // 相对路径按项目工作目录解析，避免落到容器临时目录
        File dir = new File(uploadPath);
        if (!dir.isAbsolute()) {
            dir = new File(System.getProperty("user.dir"), uploadPath);
        }
        if (!dir.exists() && !dir.mkdirs()) {
            throw new BizException("创建上传目录失败");
        }
        try {
            file.transferTo(new File(dir, filename));
        } catch (Exception e) {
            throw new BizException("文件上传失败：" + e.getMessage());
        }

        String prefix = accessPrefix.endsWith("/") ? accessPrefix : accessPrefix + "/";
        return R.ok("上传成功", prefix + filename);
    }
}
