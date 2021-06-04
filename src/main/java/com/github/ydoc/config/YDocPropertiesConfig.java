package com.github.ydoc.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * author yujian
 * description
 * create 2021-04-22 14:46
 *
 * @author yujian*/

@Getter
@Setter
@ConfigurationProperties(prefix = "ydoc")
public class YDocPropertiesConfig {
    /**
     * yapi中创建项目的的token
     */
    private String token;
    /**
     * yapi服务url
     */
    private String host;
    /**
     * 是否启动时开始导入更新api文档
     */
    private boolean enable = true;
    /**
     * 打印导入的json,方便离线导入
     */
    private boolean print = false;
    /**
     * 是否启用原生Swagger注解来生成api文档
     * 需要配置swagger扫包等config,并且移除相关pom依赖
     * （YDoc默认为swagger3.0）
     */
    private boolean swaggerNative = false;

}
