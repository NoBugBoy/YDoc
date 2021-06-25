package com.github.ydoc.config;

import com.github.ydoc.core.ScanControllerSwagger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;

/**
 * author yujian
 * description
 * create 2021-04-22 14:22
 **/
@Configuration
@EnableConfigurationProperties(YDocPropertiesConfig.class)
@EnableAsync
public class AutoConfig {
    @Autowired
    private YDocPropertiesConfig yDocPropertiesConfig;

    @ConditionalOnProperty(prefix="ydoc",name = "enable",havingValue = "true")
    @Bean
    public ScanControllerSwagger controllerSwagger(){
        return new ScanControllerSwagger(yapiApi());
    }

    @Bean
    @Primary
    public SwaggerResourcesConfig swaggerResourcesConfig(){
        return new SwaggerResourcesConfig();
    }

    @Bean
    @ConditionalOnProperty(prefix="ydoc",name ={"email.password","user.email","email.host"},matchIfMissing = true)
    public JavaMailSender javaMailSender(){
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setPassword(yDocPropertiesConfig.getEmailPassword());
        javaMailSender.setHost(yDocPropertiesConfig.getEmailHost());
        javaMailSender.setUsername(yDocPropertiesConfig.getYapiUserEmail());
        javaMailSender.setDefaultEncoding("utf-8");
        return javaMailSender;
    }

    @Bean
    public YapiApi yapiApi(){
        return new YapiApi(javaMailSender(),yDocPropertiesConfig,yapiRestTemplate());
    }
    @Bean
    public SwaggerApi swaggerApi(){
        return new SwaggerApi();
    }

    @Bean
    public RestTemplate yapiRestTemplate(){
        return new RestTemplate();
    }




}
