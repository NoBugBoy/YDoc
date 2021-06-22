package com.github.ydoc.config;

import com.github.ydoc.swagger.ScanControllerSwagger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * author yujian
 * description
 * create 2021-04-22 14:22
 **/
@Configuration
@EnableConfigurationProperties(YDocPropertiesConfig.class)
@EnableAsync
public class AutoConfig {
    @ConditionalOnProperty(prefix="ydoc",name = "enable",havingValue = "true")
    @Bean
    public ScanControllerSwagger controllerSwagger(){
        return new ScanControllerSwagger(yapiApi());
    }

    @Bean
    @Primary
    @ConditionalOnProperty(prefix = "ydoc",name = "swaggerNative",havingValue = "true")
    public SwaggerResourcesConfig swaggerResourcesConfig(){
        return new SwaggerResourcesConfig();
    }

    @Bean
    public YapiApi yapiApi(){
        return new YapiApi();
    }
    @Bean
    public SwaggerApi swaggerApi(){
        return new SwaggerApi();
    }




}
