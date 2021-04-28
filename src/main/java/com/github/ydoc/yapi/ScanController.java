package com.github.ydoc.yapi;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author yujian
 * @description 获取全部文档
 * @create 2021-04-22 14:24
 **/
@Component
public class ScanController implements ApplicationContextAware {
    private ApplicationContext applicationContext;
    private LongAdder index = new LongAdder();
    // @PostConstruct
    public void scan(){
        Map<String, Object> restControllerMap = applicationContext.getBeansWithAnnotation(RestController.class);
        List<Yapi.Api>          yapiList          = new ArrayList<>();
        Yapi yapi = new Yapi();
        for (Map.Entry<String, Object> object : restControllerMap.entrySet()) {
            //每个controller作为一个分组，描述了一组Yapi.Api
            Yapi.Api api = new Yapi.Api();
            //api分组的描述
            yapi.setName(object.getKey());
            yapi.setDesc(object.getKey());
            yapi.setIndex(index.longValue());
            yapi.setAdd_time(LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8")));
            yapi.setUp_time(LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8")));
            api.setAdd_time(LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8")));
            api.setUp_time(LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8")));
            api.setApi_opened(false);
            api.setIndex(index.longValue());
            api.set_id(index.longValue());
            api.setUid(11L);
            api.setCatid(index.longValue());
            api.setProject_id(index.longValue());
            api.set__v(0L);
            api.setRes_body("{}");

            //controller 的描述
            System.out.println("controller作用" + object.getKey());
            Class<?> aClass = object.getValue().getClass();
            //如果有外层路径需要加上
            buildBaseUrl(aClass,api);
            //循环所有的restfulApi
            Method[] methods = aClass.getDeclaredMethods();
            for (Method method : methods) {
                RequestTypeMatching.matching(method,api);
                RequestTypeMatching.returnBuild(method,api);
            }
            Yapi.Query query = new Yapi.Query();
            query.setParams(Collections.emptyList());
            query.setPath(api.getPath());
            api.setQuery_path(query);
            yapiList.add(api);
            index.increment();
        }
        yapi.setList(yapiList);
        System.out.println(JSON.toJSONString(Collections.singletonList(yapi)));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 拼接baseurl
     * @param aClass controller class
     * @param api yapi
     */
    public void buildBaseUrl(Class<?> aClass,Yapi.Api api){
        String basePath = "";
        if(aClass.isAnnotationPresent(RequestMapping.class)){
            RequestMapping annotation = aClass.getAnnotation(RequestMapping.class);
            if(annotation.value().length > 0){
                basePath = annotation.value()[0];
                api.setPath(basePath);
            }
            System.out.println("外层路径 " + basePath);
        }
    }
}
