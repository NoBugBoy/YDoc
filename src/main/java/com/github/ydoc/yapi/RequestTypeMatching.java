package com.github.ydoc.yapi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.ydoc.anno.ParamDesc;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * author yujian
 * description 匹配方法类型
 * create 2021-04-23 12:25
 **/

public class RequestTypeMatching {
    public static void matching(Method method,Yapi.Api api){
        if(method.isAnnotationPresent(GetMapping.class)){
            get(method,api);
        }else if(method.isAnnotationPresent(PostMapping.class)){
            post(method,api);
        }else if(method.isAnnotationPresent(PutMapping.class)){
            put(method,api);
        }else if(method.isAnnotationPresent(DeleteMapping.class)){
            delete(method,api);
        }else{
            //nothing
        }
    }
    public static void returnBuild(Method method,Yapi.Api api){
        Class<?> returnType = method.getReturnType();
        String desc = returnType.getName();
        if(returnType.isAnnotationPresent(ParamDesc.class)){
            ParamDesc annotation = returnType.getAnnotation(ParamDesc.class);
            desc = annotation.value();
        }
        if(returnType.getName().startsWith("java")){
           api.setRes_body(JSON.toJSONString(returnType.getName()));
        }else{
            JSONObject jsonObject =new JSONObject();
            jsonObject.put("type",RequestBodyType.OBJECT.type);
            jsonObject.put("title","empty object");
            JSONObject data = new JSONObject();
            for (Field declaredField : returnType.getDeclaredFields()) {

                data.put(declaredField.getName(), deepObject(new JSONObject(),declaredField));

            }
            jsonObject.put("properties",data);
            api.setRes_body(JSON.toJSONString(jsonObject));
        }

    }



    private static void get(Method method,Yapi.Api api){
        api.setReq_headers(null);
        GetMapping getMapping = method.getAnnotation(GetMapping.class);
        //请求方式
        api.setMethod("GET");
        if(getMapping.value().length > 0){
            //base拼接restfulApi的路径
            api.setPath(api.getPath() + getMapping.value()[0]);
        }
        //restfulApi接口的描述/功能
        api.setTitle(getMapping.name());
        System.out.println("controller方法描述 " + getMapping.name());
        //处理get参数 1.如果不是pojo则必须带上@RequestParam用来获取参数描述信息
        Parameter[] parameters    = method.getParameters();
        List<Yapi.ReqQuery> reqQueries = new ArrayList<>();
        for (Parameter parameterType : parameters) {
            //参数名
            String paramName="";
            //参数对象类型
            String paramType = parameterType.getType().getSimpleName();
            //参数描述
            String paramDesc = parameterType.getName();
            //1 参数有注解
            if(parameterType.isAnnotationPresent(RequestParam.class)){
                Yapi.ReqQuery reqQuery = new Yapi.ReqQuery();
                RequestParam annotation = parameterType.getAnnotation(RequestParam.class);
                paramName = annotation.value();
                paramDesc = annotation.name();
                reqQuery.setName(paramName);
                reqQuery.setRequired(annotation.required()?Required.TRUE.getCode():Required.FALSE.getCode());
                reqQueries.add(reqQuery);
            }else{
                //2 参数无注解则有可能没加，或是pojo只获取第一层的参数
                if(parameterType.getParameterizedType().getTypeName().startsWith("java")){
                    //如果是常规类型就直接处理
                    Yapi.ReqQuery reqQuery = new Yapi.ReqQuery();
                    reqQuery.setName(parameterType.getName());
                    reqQuery.setDesc(parameterType.getName());
                    reqQuery.setRequired(Required.FALSE.getCode());
                    reqQueries.add(reqQuery);
                    System.out.println("对象内参数名 " + reqQuery.getName());
                    System.out.println("对象内参数描述" + reqQuery.getDesc());
                    System.out.println("对象内参数是否必须" + reqQuery.getRequired());
                }else{
                    Field[] declaredFields = parameterType.getType().getDeclaredFields();
                    for (Field field : declaredFields) {
                        Yapi.ReqQuery reqQuery = new Yapi.ReqQuery();
                        if(field.isAnnotationPresent(ParamDesc.class)){
                            ParamDesc annotation = field.getAnnotation(ParamDesc.class);
                            reqQuery.setDesc(annotation.value());
                            reqQuery.setRequired(annotation.required()?Required.TRUE.getCode():Required.FALSE.getCode());
                        }else{
                            reqQuery.setDesc(field.getName());
                            reqQuery.setRequired(Required.FALSE.getCode());
                        }
                        reqQuery.setName(field.getName());
                        reqQueries.add(reqQuery);
                        System.out.println("对象内参数名 " + reqQuery.getName());
                        System.out.println("对象内参数描述" + reqQuery.getDesc());
                        System.out.println("对象内参数是否必须" + reqQuery.getRequired());
                    }
                }
            }
        }
    }
    private static void post(Method method,Yapi.Api api){
        api.setReq_body_is_json_schema(true);
        api.setRes_body_is_json_schema(true);
        api.setMethod("POST");
        api.setRes_body_type("json");
        api.setReq_headers(Collections.singletonList(new Yapi.Header()));
        PostMapping annotation = method.getAnnotation(PostMapping.class);
        if(annotation.value().length > 0){
            //base拼接restfulApi的路径
            api.setPath(api.getPath() + annotation.value()[0]);
        }
        //restfulApi接口的描述/功能
        api.setTitle(annotation.name());
        baseRequestBody(method.getParameters(),api);
    }
    private static void delete(Method method,Yapi.Api api){
        api.setReq_body_is_json_schema(true);
        api.setRes_body_is_json_schema(true);
        api.setMethod("DELETE");
        api.setRes_body_type("json");
        api.setReq_headers(Collections.singletonList(new Yapi.Header()));
        DeleteMapping annotation = method.getAnnotation(DeleteMapping.class);
        if(annotation.value().length > 0){
            //base拼接restfulApi的路径
            api.setPath(api.getPath() + annotation.value()[0]);
        }
        //restfulApi接口的描述/功能
        api.setTitle(annotation.name());
        baseRequestBody(method.getParameters(),api);
    }
    private static void put(Method method,Yapi.Api api){
        api.setReq_body_is_json_schema(true);
        api.setRes_body_is_json_schema(true);
        api.setMethod("PUT");
        api.setRes_body_type("json");
        api.setReq_headers(Collections.singletonList(new Yapi.Header()));
        PutMapping annotation = method.getAnnotation(PutMapping.class);
        if(annotation.value().length > 0){
            //base拼接restfulApi的路径
            api.setPath(api.getPath() + annotation.value()[0]);
        }
        //restfulApi接口的描述/功能
        api.setTitle(annotation.name());
        baseRequestBody(method.getParameters(),api);
    }

    private static void baseRequestBody(Parameter[] parameters,Yapi.Api api){
        RequestBodyJson requestBodyJson = new RequestBodyJson();
        for (Parameter parameter : parameters) {
            // if(parameter.isAnnotationPresent(PathVariable.class)){
            //     //TODO
            //     PathVariable annotation = parameter.getAnnotation(PathVariable.class);
            //     annotation.value().
            // }
            if(parameter.isAnnotationPresent(RequestBody.class)){
                Class<?> type = parameter.getType();
                Field[]  declaredFields = type.getDeclaredFields();
                JSONObject properties = new JSONObject();
                //对象内properties第一层
                for (Field declaredField : declaredFields) {
                    properties.put(declaredField.getName(),deepObject(new JSONObject(),declaredField));
                }
                // JSONObject out = properties(properties);
                requestBodyJson.setProperties(properties);
                api.setReq_body_other(JSON.toJSONString(requestBodyJson, SerializerFeature.DisableCircularReferenceDetect));
            }
        }

    }

    private static JSONObject deepObject(JSONObject json,Field declaredField){
        String desc = declaredField.getName();
        if(declaredField.isAnnotationPresent(ParamDesc.class)){
            ParamDesc annotation = declaredField.getAnnotation(ParamDesc.class);
            desc = annotation.value();
        }
        if(declaredField.getType().isEnum()){
            //// TODO: 2021/4/26
            //常规类型
            json.put("type","string");
            json.put("description",desc);
            return json;
        }
        if(declaredField.getType().equals(List.class) || declaredField.getType().equals(Set.class)){
            Type              genericType = declaredField.getGenericType();
            ParameterizedType pt          = (ParameterizedType) genericType;
            Class<?>          actualTypeArgument = (Class<?>)pt.getActualTypeArguments()[0];
            json.put("type",RequestBodyType.ARRAY.type);
            if(actualTypeArgument.getTypeName().startsWith("java")){
                //如果是普通类型
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("type",RequestBodyType.of(actualTypeArgument.getSimpleName()).type);
                jsonObject.put("description",desc);
                json.put("items",jsonObject);
            }else{
                //如果是对象
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("type",RequestBodyType.OBJECT.type);
                JSONObject filedObject = new JSONObject();
                for (Field field : actualTypeArgument.getDeclaredFields()) {
                    if(field.getType().equals(declaredField.getType())){
                        //TODO User 里有 list<User> 会死递归
                        filedObject.put(field.getName(),deepObject(new JSONObject(),field));
                    }else{
                        filedObject.put(field.getName(),deepObject(new JSONObject(),field));
                    }

                }
                jsonObject.put("properties",filedObject);
                json.put("items",jsonObject);
                json.put("description",desc);
            }
            return json;
        }
        else if(declaredField.getType().getTypeName().startsWith("java")){
            //常规类型
            json.put("type",RequestBodyType.of(declaredField.getType().getSimpleName()).type);
            json.put("description",desc);
            return json;
        }else{
            //对象 先解析desc
            Class<?> declaringClass = declaredField.getDeclaringClass();
            json.put("type",RequestBodyType.OBJECT.type);
            JSONObject objectTypeJSON = new JSONObject();
            for (Field field : declaringClass.getDeclaredFields()) {
                objectTypeJSON.put(field.getName(),deepObject(json, field));
            }
            json.put("properties",objectTypeJSON);
            json.put("description",desc);
            return json;
        }

    }
}
