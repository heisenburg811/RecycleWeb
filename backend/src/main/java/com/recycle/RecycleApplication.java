package com.recycle;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 二手家具交易平台 - 应用入口。
 *
 * <p>架构约定：单体应用，按业务域分包（item / search / order / payment / logistics / user）。
 * 模块之间只通过 Service 接口或领域事件交互，禁止跨包直接访问 Mapper。
 */
@SpringBootApplication
@MapperScan("com.recycle.**.mapper")
public class RecycleApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecycleApplication.class, args);
    }
}
