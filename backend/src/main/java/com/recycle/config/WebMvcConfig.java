package com.recycle.config;

import com.recycle.file.FileService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 把本地上传目录映射成 /uploads/** 静态资源。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final FileService fileService;

    public WebMvcConfig(FileService fileService) {
        this.fileService = fileService;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(fileService.getRoot().toUri().toString());
    }
}
