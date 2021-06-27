package com.github.ydoc.core;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.ydoc.config.YDocPropertiesConfig;
import com.github.ydoc.config.YapiApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.service.Documentation;
import springfox.documentation.spring.web.DocumentationCache;
import springfox.documentation.swagger2.mappers.ServiceModelToSwagger2Mapper;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * author yujian
 * description 获取全部文档
 * create 2021-04-22 14:24
 **/
@EnableConfigurationProperties(YDocPropertiesConfig.class)
@Slf4j
public class ScanControllerSwagger implements ApplicationContextAware, EnvironmentAware, InitializingBean,
    CommandLineRunner {
    private final YapiApi yapiApi;
    public ScanControllerSwagger(YapiApi yapiApi){
        this.yapiApi = yapiApi;
    }
    @Autowired
    YDocPropertiesConfig         propertiesConfig;
    @Autowired(required = false)
    DocumentationCache           documentationCache;
    @Autowired(required = false)
    ServiceModelToSwagger2Mapper map;

    private ApplicationContext applicationContext;
    private Environment e;

    Supplier<String> basePath = () -> StringUtils.hasText(e.getProperty("server.servlet.context-path"))?e.getProperty("server.servlet.context-path"):"/";
    public void scan(){
        Map<String, Object> restControllerMap = applicationContext.getBeansWithAnnotation(RestController.class);
        Swagger swagger = Swagger.initialize();
        swagger.setDefinitions(Factory.definitions);
        swagger.setBasePath(basePath.get());
        List<Swagger.Tag> tags = new ArrayList<>();
        JSONObject paths = new JSONObject();
        for (Map.Entry<String, Object> object : restControllerMap.entrySet()) {
            //组装swagger-api
            Class<?> aClass = object.getValue().getClass();
            //如果有外层路径需要加上
            String outPath = buildBaseUrl(aClass);
            if(StringUtils.hasText(outPath) && !outPath.startsWith("/") || "/swagger-json".equals(outPath)){
                continue;
            }
            //controller分组
            tags.add(new Swagger.Tag( object.getKey(),object.getKey()));
            //循环所有的restfulApi
            Method[] methods = aClass.getDeclaredMethods();

            for (Method method : methods) {
                RequestTypeMatchingSwagger.matching(paths,method, outPath, object.getKey());
            }
        }
        swagger.setPaths(paths);
        swagger.setTags(tags);
        Factory.json = JSON.toJSONString(swagger);
        if(propertiesConfig.isPrint()){
            print();
        }
        if(enableImport()){
            importToYApi();
        }

    }
    public void print(){
        log.info(Factory.json);
    }
    public boolean enableImport(){
        return StringUtils.hasText(propertiesConfig.getHost()) && StringUtils.hasText(propertiesConfig.getToken());
    }
    public synchronized void importToYApi(){
            yapiApi.importDoc(propertiesConfig.isCloud(),propertiesConfig.getToken(),propertiesConfig.getHost(),Factory.json);
            Factory.definitions.clear();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    
    public String buildBaseUrl(Class<?> aClass){
        String basePath = "";
        if(aClass.isAnnotationPresent(RequestMapping.class)){
            RequestMapping annotation = aClass.getAnnotation(RequestMapping.class);
            if(annotation.value().length > 0){
                basePath = annotation.value()[0];
            }
        }
        return basePath;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.e = environment;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if(propertiesConfig.isEnable()){
            if(!propertiesConfig.isSwaggerNative()){
                log.info(" >>> YDoc Sync Api start !<<<");
                scan();
                log.info(" >>> YDoc Sync Api Successful !<<<");
            }
        }
    }

    @Override
    public void run(String... args) throws Exception {
        if(propertiesConfig.isSwaggerNative() && documentationCache!=null && map!=null){
            //这里暂时只考虑一个group的时候，因为大多数情况都是只有一个default
            log.info(" >>> YDoc Sync Api start !<<<");
            if(documentationCache.all().values().size() > 0){
                Documentation             documentation = new ArrayList<>(documentationCache.all().values()).get(0);
                io.swagger.models.Swagger swagger       = this.map.mapDocumentation(documentation);
                Factory.json = JSON.toJSONString(swagger);
                if(propertiesConfig.isPrint()){
                    print();
                }
                if(enableImport()){
                    importToYApi();
                }
            }else{
                log.warn("未发现任何Api,可能未配置Swagger2 Config....");
            }
            log.info(" >>> YDoc Sync Api Successful !<<<");
        }
        //access调用链
        if(access()){
            YapiAccess login = yapiApi.login();
            //异步执行即可
            if(propertiesConfig.isAutoTest()){
                yapiApi.autoTest(login);
            }
        }
        //no access



    }
    /**
        某些方法必须login才ok
     */
    private boolean access(){
        return StringUtils.hasText(propertiesConfig.getYapiUserEmail())&&StringUtils.hasText(propertiesConfig.getYapiUserPassword());
    }
}
