package com.recycle.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 接口文档：http://localhost:8080/swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI recycleOpenApi() {
        return new OpenAPI().info(new Info()
                .title("二手家具交易平台 API")
                .version("v1")
                .description("MVP：商品发布 / 搜索分类 / 下单支付 + 物流预约"));
    }
}
