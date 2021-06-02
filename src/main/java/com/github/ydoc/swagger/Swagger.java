package com.github.ydoc.swagger;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

/**
 * author yujian
 * description
 * create 2021-04-27 09:50
 **/
@Getter
@Setter
public class Swagger {
    private String    swagger;
    //项目级别
    private Info      info;
    private String    basePath;
    private List<Tag> tags;
    private List<String> schemes;
    private JSONObject paths;
    private JSONObject definitions;
    @Getter
    @Setter
    public static class Info{
        private String title = "YDoc(同时支持SwaggerUi和YApi的一款无侵入api文档生成器)";
        private String version = "last";
        private String description ="YDoc生成的RestfulApi文档";
    }
    @Getter
    @Setter
    @AllArgsConstructor
    public static class Tag{
        //一个controller就是一个分类
        private String name;
        private String description;
    }

    public static Swagger initialize(){
        Swagger swagger = new Swagger();
        //help gc 只保留最后的json string对象就可以了
        WeakReference<Swagger> weakReference = new WeakReference<>(swagger);
        swagger.schemes = Collections.singletonList("http");
        swagger.swagger = "2.0";
        swagger.info = new Info();
        return weakReference.get();
    }
}
