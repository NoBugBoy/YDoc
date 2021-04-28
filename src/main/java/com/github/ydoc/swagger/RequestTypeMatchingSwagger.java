package com.github.ydoc.swagger;

import com.alibaba.fastjson.JSONObject;
import com.github.ydoc.anno.ParamDesc;
import com.github.ydoc.anno.ParamIgnore;
import com.github.ydoc.yapi.RequestBodyType;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.*;
import java.util.*;

/**
 * author yujian
 * description 匹配方法类型
 * create 2021-04-23 12:25
 **/

public class RequestTypeMatchingSwagger {
    public static JSONObject matching(JSONObject json,Method method, String outPath,String tag){
        if(method.isAnnotationPresent(GetMapping.class)){
            GetMapping getMapping = method.getAnnotation(GetMapping.class);
            String path = "";
            if(getMapping.value().length > 0){
                //base拼接restfulApi的路径
                path = getMapping.value()[0];
            }
            return get(getMapping.name(),path,json,method,outPath,tag);
        }else if(method.isAnnotationPresent(PostMapping.class)){
            PostMapping annotation = method.getAnnotation(PostMapping.class);
            String path = "";
            if(annotation.value().length > 0){
                //base拼接restfulApi的路径
                path = annotation.value()[0];
            }
            return post(annotation.name(),path,json,method,outPath,tag);
        }else if(method.isAnnotationPresent(PutMapping.class)){
            PutMapping annotation = method.getAnnotation(PutMapping.class);
            String path = "";
            if(annotation.value().length > 0){
                //base拼接restfulApi的路径
                path = annotation.value()[0];
            }
            return put(annotation.name(),path,json,method,outPath,tag);
        }else if(method.isAnnotationPresent(DeleteMapping.class)){
            DeleteMapping annotation = method.getAnnotation(DeleteMapping.class);
            String path = "";
            if(annotation.value().length > 0){
                //base拼接restfulApi的路径
                path = annotation.value()[0];
            }
            return delete(annotation.name(),path,json,method,outPath,tag);
        }else if(method.isAnnotationPresent(RequestMapping.class)){
            RequestMapping annotation = method.getAnnotation(RequestMapping.class);
            RequestMethod requestMethod = null;
            if(annotation.method().length > 0){
                requestMethod = annotation.method()[0];
            }
            String path = "";
            if(annotation.value().length > 0){
                //base拼接restfulApi的路径
                path = annotation.value()[0];
            }
            if(requestMethod != null){
                switch (requestMethod){
                    case GET: return get(requestMethod.name(),path,json,method,outPath,tag);
                    case PUT:return put(requestMethod.name(),path,json,method,outPath,tag);
                    case POST:return post(requestMethod.name(),path,json,method,outPath,tag);
                    case DELETE:return delete(requestMethod.name(),path,json,method,outPath,tag);
                    default:
                        return Factory.get();
                }
            }
        }else {
            //nothing
            return new JSONObject();
        }
        return Factory.get();
    }
    public static void returnBuild(Method method, JSONObject json){
        JSONObject res = Factory.get();
        json.put("responses",res);
        JSONObject resDetail = Factory.get();
        res.put("200",resDetail);
        resDetail.put("description","successful operation");
        JSONObject schema = Factory.get();
        resDetail.put("schema",schema);
        schema.put("type","object");
        schema.put("title","YDoc");
        JSONObject properties = Factory.get();

        Class<?> returnType = method.getReturnType();
        String desc = returnType.getName();
        if(returnType.isAnnotationPresent(ParamDesc.class)){
            ParamDesc annotation = returnType.getAnnotation(ParamDesc.class);
            desc = annotation.value();
        }
        if(returnType.equals(List.class) || returnType.equals(Set.class)) {
            Type genericReturnType = method.getGenericReturnType();
            if (genericReturnType instanceof ParameterizedType){
                ParameterizedType genericReturnType1 = (ParameterizedType)genericReturnType;
                for (Type actualTypeArgument : genericReturnType1.getActualTypeArguments()) {
                    if(actualTypeArgument.getTypeName().startsWith("java")){
                        JSONObject jsonObject = Factory.get();
                        properties.put(actualTypeArgument.getTypeName(),jsonObject);
                        jsonObject.put("description",desc);
                        jsonObject.put("type",actualTypeArgument.getTypeName());
                        schema.put("properties",properties);
                    }else{
                        try {
                            Class<?> clazz = Class.forName(actualTypeArgument.getTypeName());
                            for (Field declaredField : clazz.getDeclaredFields()) {
                                properties.put(declaredField.getName(), deepObject(Factory.get(),declaredField));
                            }
                            schema.put("properties",properties);
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        else if(returnType.getName().startsWith("java")){
            JSONObject jsonObject = Factory.get();
            properties.put(returnType.getSimpleName(),jsonObject);
            jsonObject.put("description",desc);
            jsonObject.put("type",returnType.getSimpleName());
            schema.put("properties",properties);
        }else{
            for (Field declaredField : returnType.getDeclaredFields()) {
                properties.put(declaredField.getName(), deepObject(Factory.get(),declaredField));
            }
            schema.put("properties",properties);
        }

    }


    private static JSONObject get(String name,String path,JSONObject api,Method method, String outPath,String tag){
        //方法对象
        JSONObject apiMethod = Factory.get();
        api.put(outPath + path,apiMethod);
        //body
        JSONObject content = Factory.get();
        apiMethod.put("get",content);
        //restfulApi接口的描述/功能
        content.put("summary",name);
        content.put("description",name);
        //处理get参数 1.如果不是pojo则必须带上@RequestParam用来获取参数描述信息
        List<JSONObject> parametersJson = new ArrayList<>();
        content.put("parameters",parametersJson);
        content.put("tags",Collections.singletonList(tag));
        //循环方法内参数
        returnBuild(method,content);
        Parameter[] parameters    = method.getParameters();
        for (Parameter parameterType : parameters) {
            if(parameterType.isAnnotationPresent(ParamIgnore.class)){
                continue;
            }
            boolean isObject  = Boolean.FALSE;
            JSONObject param = Factory.get();
            //参数名
            String paramName= parameterType.getName();
            //参数对象类型
            String paramType = parameterType.getType().getSimpleName();
            //参数描述
            String paramDesc = parameterType.getName();
            String in = "query";
            boolean required = Boolean.FALSE;
            //1 参数有注解
            if(parameterType.isAnnotationPresent(PathVariable.class)){
                PathVariable annotation = parameterType.getAnnotation(PathVariable.class);
                paramName = annotation.value();
                required = Boolean.TRUE;
                paramDesc = "1";
                paramType = "string";
                in = "path";
            } else if(parameterType.isAnnotationPresent(RequestParam.class)){
                RequestParam annotation = parameterType.getAnnotation(RequestParam.class);
                paramName = annotation.value();
                paramDesc = annotation.name();
                required = annotation.required();
            }else{
                //如果有其他注解则不对其生成操作
                if(parameterType.getDeclaredAnnotations().length > 1){
                    continue;
                }
                //2 参数无注解则有可能没加，或是pojo只获取第一层的参数
                if(!parameterType.getParameterizedType().getTypeName().startsWith("java")){
                    isObject = Boolean.TRUE;
                    Field[] declaredFields = parameterType.getType().getDeclaredFields();
                    for (Field field : declaredFields) {
                        JSONObject bodyField = Factory.get();
                        bodyField.put("name",field.getName());
                        bodyField.put("in","query");
                        bodyField.put("type",field.getType().getSimpleName());
                        if(field.isAnnotationPresent(ParamDesc.class)){
                            ParamDesc annotation = field.getAnnotation(ParamDesc.class);
                            bodyField.put("required",annotation.required());
                            bodyField.put("description",annotation.value());
                        }else{
                            bodyField.put("required",Boolean.TRUE);
                            bodyField.put("description",field.getName());
                        }
                        parametersJson.add(bodyField);
                    }
                }
            }
            if(!isObject){
                param.put("name",paramName);
                param.put("in",in);
                param.put("required",required);
                param.put("description",paramDesc);
                param.put("type",paramType);
                parametersJson.add(param);
            }
        }
        return api;
    }
    private static JSONObject post(String name,String path,JSONObject api,Method method, String outPath,String tag){
        //方法对象
        JSONObject apiMethod = Factory.get();
        api.put(outPath + path,apiMethod);
        //body
        JSONObject content = Factory.get();
        apiMethod.put("post",content);
        content.put("tags",Collections.singletonList(tag));
        //restfulApi接口的描述/功能
        content.put("summary",name);
        content.put("description",name);
        content.put("consumes", Collections.singleton("application/json"));
        //处理get参数 1.如果不是pojo则必须带上@RequestParam用来获取参数描述信息
        List<JSONObject> parametersJson = new ArrayList<>();
        content.put("parameters",parametersJson);
        returnBuild(method,content);
        //restfulApi接口的描述/功能
        baseRequestBody(method.getParameters(),parametersJson);
        return api;
    }
    private static JSONObject delete(String name,String path,JSONObject api,Method method, String outPath,String tag){
        //方法对象
        JSONObject apiMethod = Factory.get();
        api.put(outPath + path,apiMethod);
        //body
        JSONObject content = Factory.get();
        apiMethod.put("delete",content);
        content.put("tags",Collections.singletonList(tag));
        //restfulApi接口的描述/功能
        content.put("summary",name);
        content.put("description",name);
        content.put("consumes", Collections.singleton("application/json"));
        //处理get参数 1.如果不是pojo则必须带上@RequestParam用来获取参数描述信息
        List<JSONObject> parametersJson = new ArrayList<>();
        content.put("parameters",parametersJson);
        returnBuild(method,content);
        //restfulApi接口的描述/功能
        baseRequestBody(method.getParameters(),parametersJson);
        return api;
    }
    private static JSONObject put(String name,String path,JSONObject api,Method method, String outPath,String tag){
        //方法对象
        JSONObject apiMethod = Factory.get();
        api.put(outPath + path,apiMethod);
        //body
        JSONObject content = Factory.get();
        apiMethod.put("put",content);
        content.put("tags",Collections.singletonList(tag));
        //restfulApi接口的描述/功能
        content.put("summary",name);
        content.put("description",name);
        content.put("consumes", Collections.singleton("application/json"));
        //处理get参数 1.如果不是pojo则必须带上@RequestParam用来获取参数描述信息
        List<JSONObject> parametersJson = new ArrayList<>();
        content.put("parameters",parametersJson);
        returnBuild(method,content);
        //restfulApi接口的描述/功能
        baseRequestBody(method.getParameters(),parametersJson);
        return api;
    }

    private static void baseRequestBody(Parameter[] parameters,List<JSONObject> list){
        for (Parameter parameter : parameters) {
            if(parameter.isAnnotationPresent(PathVariable.class)){
                PathVariable annotation = parameter.getAnnotation(PathVariable.class);
                JSONObject api = Factory.get();
                api.put("name",annotation.value());
                api.put("in","path");
                api.put("required",Boolean.TRUE);
                api.put("description","1");
                api.put("type","string");
                list.add(api);
            }else if(parameter.isAnnotationPresent(RequestBody.class)){
                Class<?> type = parameter.getType();
                Field[]  declaredFields = type.getDeclaredFields();
                JSONObject api = Factory.get();
                api.put("name","root");
                api.put("in","body");
                JSONObject schema = Factory.get();
                api.put("schema",schema);
                schema.put("type","object");
                schema.put("title","YDoc");
                JSONObject properties = Factory.get();
                schema.put("properties",properties);
                //对象内properties第一层
                for (Field declaredField : declaredFields) {
                    if(declaredField.isAnnotationPresent(ParamIgnore.class)){
                        continue;
                    }
                    properties.put(declaredField.getName(),deepObject(Factory.get(),declaredField));
                }
                list.add(api);
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
            //常规类型
            json.put("type","enum");
            Object[] enumConstants = declaredField.getType().getEnumConstants();
            Set<String> jsonArray = new HashSet<>();
            for (Object enumConstant : enumConstants) {
                jsonArray.add(enumConstant.toString());
            }
            json.put("description",jsonArray);
            return json;
        }
        if(declaredField.getType().equals(List.class) || declaredField.getType().equals(Set.class)){
            Type              genericType = declaredField.getGenericType();
            ParameterizedType pt          = (ParameterizedType) genericType;
            Class<?>          actualTypeArgument = (Class<?>)pt.getActualTypeArguments()[0];
            json.put("type",RequestBodyType.ARRAY.type);
            if(actualTypeArgument.getTypeName().startsWith("java")){
                //如果是普通类型
                JSONObject jsonObject = Factory.get();
                jsonObject.put("type",RequestBodyType.of(actualTypeArgument.getSimpleName()).type);
                jsonObject.put("description",desc);
                json.put("items",jsonObject);
            }else{
                //如果是对象
                JSONObject jsonObject = Factory.get();
                jsonObject.put("type",RequestBodyType.OBJECT.type);
                JSONObject filedObject = Factory.get();
                for (Field field : actualTypeArgument.getDeclaredFields()) {
                    if(field.getType().equals(declaredField.getType())){
                        // User 里有 list<User> 会死递归
                        break;
                    }else{
                        filedObject.put(field.getName(),deepObject(Factory.get(),field));
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
            JSONObject objectTypeJSON = Factory.get();
            for (Field field : declaringClass.getDeclaredFields()) {
                objectTypeJSON.put(field.getName(),deepObject(json, field));
            }
            json.put("properties",objectTypeJSON);
            json.put("description",desc);
            return json;
        }

    }
}
