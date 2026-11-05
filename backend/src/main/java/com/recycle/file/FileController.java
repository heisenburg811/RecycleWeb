package com.recycle.file;

import com.recycle.common.api.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传。需要登录（见 SecurityConfig 的默认规则）。
 */
@Tag(name = "文件", description = "商品图片上传")
@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @Operation(summary = "上传图片", description = "返回相对路径，可直接放进商品 images 字段")
    @PostMapping
    public R<String> upload(@RequestParam("file") MultipartFile file) {
        return R.ok(fileService.saveImage(file));
    }
}
