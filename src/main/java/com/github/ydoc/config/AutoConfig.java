package com.github.ydoc.config;

import com.github.ydoc.swagger.ScanControllerSwagger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author yujian
 * @description
 * @create 2021-04-22 14:22
 **/
@Configuration
@EnableConfigurationProperties(YDocPropertiesConfig.class)
public class AutoConfig {
    @ConditionalOnProperty(prefix="ydoc",name = "enable",havingValue = "true")
    @Bean
    public ScanControllerSwagger controllerSwagger(){
        return new ScanControllerSwagger();
    }
}
