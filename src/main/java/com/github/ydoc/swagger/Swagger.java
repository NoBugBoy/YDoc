package com.github.ydoc.swagger;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

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
    @Getter
    @Setter
    public static class Info{
        private String title = "阿斯达";
        private String version = "last";
        private String description ="测试工程";
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
        swagger.schemes = Collections.singletonList("http");
        swagger.swagger = "2.0";
        swagger.info = new Info();
        return swagger;
    }
}
