package com.recycle.file;

import com.recycle.common.exception.BizException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

/**
 * 本地文件存储。
 *
 * <p>MVP 存本机磁盘，通过 /uploads/** 静态映射直接访问。
 * 将来换 OSS 时只需替换本类实现（接口不变），业务代码不动。
 */
@Service
public class FileService {

    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "webp", "gif");
    private static final long MAX_SIZE = 5L * 1024 * 1024;
    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyy/MM");

    private final Path root;

    public FileService(@Value("${app.upload.dir:uploads}") String dir) {
        this.root = Paths.get(dir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new IllegalStateException("无法创建上传目录: " + root, e);
        }
    }

    public Path getRoot() {
        return root;
    }

    /**
     * 保存图片，返回可直接用于 &lt;img src&gt; 的相对路径，如 /uploads/2026/11/xxx.jpg
     */
    public String saveImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException("请选择要上传的图片");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BizException("图片不能超过 5MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BizException("只能上传图片文件");
        }
        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        ext = ext == null ? "" : ext.toLowerCase();
        if (!ALLOWED_EXT.contains(ext)) {
            throw new BizException("仅支持 jpg / png / webp / gif");
        }

        String relative = LocalDate.now().format(MONTH_FMT) + "/"
                + UUID.randomUUID().toString().replace("-", "") + "." + ext;
        Path target = root.resolve(relative);
        try {
            Files.createDirectories(target.getParent());
            file.transferTo(target);
        } catch (IOException e) {
            throw new BizException("图片保存失败，请重试");
        }
        return "/uploads/" + relative;
    }
}
