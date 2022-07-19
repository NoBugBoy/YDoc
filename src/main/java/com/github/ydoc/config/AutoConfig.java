package com.github.ydoc.config;

import com.github.ydoc.core.ScanApi;
import com.github.ydoc.plugin.mc.MethodChainAdvisor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author nobugboy
 **/
@Configuration
@EnableConfigurationProperties(YDocPropertiesConfig.class)
@EnableAsync
public class AutoConfig implements ApplicationContextAware {
    @Autowired
    private YDocPropertiesConfig yDocPropertiesConfig;

    private ApplicationContext applicationContext;

    @ConditionalOnProperty(prefix = "ydoc", name = "enable", havingValue = "true")
    @Bean
    public ScanApi controllerSwagger() {
	return new ScanApi(yapiApi());
    }

    @Bean
    @Primary
    @ConditionalOnClass(EnableSwagger2.class)
    public SwaggerResourcesConfig swaggerResourcesConfig() {
	return new SwaggerResourcesConfig();
    }

    @Bean
    @ConditionalOnProperty(prefix = "ydoc", name = "boost", havingValue = "true")
    public MethodChainAdvisor methodChainAdvisor() {
	return new MethodChainAdvisor();
    }

    @Bean
    @ConditionalOnProperty(prefix = "ydoc", name = { "email.password", "user.email",
	    "email.host" }, matchIfMissing = true)
    public JavaMailSender javaMailSender() {
	JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
	javaMailSender.setPassword(yDocPropertiesConfig.getEmailPassword());
	javaMailSender.setHost(yDocPropertiesConfig.getEmailHost());
	javaMailSender.setUsername(yDocPropertiesConfig.getYapiUserEmail());
	javaMailSender.setDefaultEncoding("utf-8");
	return javaMailSender;
    }

    @Bean
    public YapiApi yapiApi() {
	return new YapiApi(javaMailSender(), yDocPropertiesConfig, yapiRestTemplate());
    }

    @Bean
    public SwaggerApi swaggerApi() {
	return new SwaggerApi();
    }

    @Bean
    public RestTemplate yapiRestTemplate() {
	return new RestTemplate();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
	this.applicationContext = applicationContext;
    }
}
