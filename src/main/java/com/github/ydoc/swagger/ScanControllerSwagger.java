package com.github.ydoc.swagger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.ydoc.config.YDocPropertiesConfig;
import com.github.ydoc.config.YapiApi;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author yujian
 * @description 获取全部文档
 * @create 2021-04-22 14:24
 **/
@Component
@EnableConfigurationProperties(YDocPropertiesConfig.class)
public class ScanControllerSwagger implements ApplicationContextAware, EnvironmentAware, InitializingBean {
    @Autowired
    YDocPropertiesConfig propertiesConfig;
    private ApplicationContext applicationContext;
    private Environment e;
    public void scan(){
        Map<String, Object> restControllerMap = applicationContext.getBeansWithAnnotation(RestController.class);
        Swagger swagger = Swagger.initialize();
        swagger.setBasePath(StringUtils.hasText(e.getProperty("server.servlet.context-path"))?e.getProperty("server.servlet.context-path"):"/");
        List<Swagger.Tag> tags = new ArrayList<>();
        JSONObject paths = new JSONObject();
        for (Map.Entry<String, Object> object : restControllerMap.entrySet()) {
            //组装swagger-api
            Class<?> aClass = object.getValue().getClass();
            //如果有外层路径需要加上
            String outPath = buildBaseUrl(aClass);
            if(!outPath.startsWith("/")){
                continue;
            }
            //controller分组
            tags.add(new Swagger.Tag(object.getKey(),object.getKey()));
            //循环所有的restfulApi
            Method[] methods = aClass.getDeclaredMethods();

            for (Method method : methods) {
                RequestTypeMatchingSwagger.matching(paths,method, outPath, object.getKey());
            }
        }
        swagger.setPaths(paths);
        swagger.setTags(tags);
        String json = JSON.toJSONString(swagger);
        if(propertiesConfig.isPrint()){
            System.out.println(json);
        }
        YapiApi.importDoc(propertiesConfig.getToken(),propertiesConfig.getHost(),json);

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 拼接baseurl
     * @param aClass controller class
     */
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
            scan();
            System.out.println(" >>> YDoc Sync Api Successful !<<<");
        }
    }
}
