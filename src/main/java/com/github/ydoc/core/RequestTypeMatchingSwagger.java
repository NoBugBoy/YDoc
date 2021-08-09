package com.github.ydoc.core;

import java.lang.reflect.*;
import java.util.*;

import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.ydoc.anno.ParamDesc;
import com.github.ydoc.anno.ParamIgnore;

/**
 * author NoBugBoY description 匹配方法类型 create 2021-04-23 12:25
 **/

public class RequestTypeMatchingSwagger {
    private static Set<String> REF = new HashSet<>();

    public static void matching(JSONObject json, Method method, String outPath, String tag) {
	if (method.isAnnotationPresent(GetMapping.class)) {
	    GetMapping getMapping = method.getAnnotation(GetMapping.class);
	    String path = "";
	    if (getMapping.value().length > 0) {
		// base拼接restfulApi的路径
		path = Factory.pathFormat.apply(getMapping.value()[0]);
	    }
	    get(getMapping.name(), path, json, method, outPath, tag);
	} else if (method.isAnnotationPresent(PostMapping.class)) {
	    PostMapping annotation = method.getAnnotation(PostMapping.class);
	    String path = "";
	    if (annotation.value().length > 0) {
		// base拼接restfulApi的路径
		path = Factory.pathFormat.apply(annotation.value()[0]);
	    }
	    post(annotation.name(), path, json, method, outPath, tag);
	} else if (method.isAnnotationPresent(PutMapping.class)) {
	    PutMapping annotation = method.getAnnotation(PutMapping.class);
	    String path = "";
	    if (annotation.value().length > 0) {
		// base拼接restfulApi的路径
		path = Factory.pathFormat.apply(annotation.value()[0]);
	    }
	    put(annotation.name(), path, json, method, outPath, tag);
	} else if (method.isAnnotationPresent(DeleteMapping.class)) {
	    DeleteMapping annotation = method.getAnnotation(DeleteMapping.class);
	    String path = "";
	    if (annotation.value().length > 0) {
		// base拼接restfulApi的路径
		path = Factory.pathFormat.apply(annotation.value()[0]);
	    }
	    delete(annotation.name(), path, json, method, outPath, tag);
	} else if (method.isAnnotationPresent(RequestMapping.class)) {
	    RequestMapping annotation = method.getAnnotation(RequestMapping.class);
	    RequestMethod requestMethod = null;
	    if (annotation.method().length > 0) {
		requestMethod = annotation.method()[0];
	    }
	    String path = "";
	    if (annotation.value().length > 0) {
		// base拼接restfulApi的路径
		path = Factory.pathFormat.apply(annotation.value()[0]);
	    }
	    if (requestMethod != null) {
		switch (requestMethod) {
		case GET:
		    get(annotation.name(), path, json, method, outPath, tag);
		    return;
		case PUT:
		    put(annotation.name(), path, json, method, outPath, tag);
		    return;
		case POST:
		    post(annotation.name(), path, json, method, outPath, tag);
		    return;
		case DELETE:
		    delete(annotation.name(), path, json, method, outPath, tag);
		    return;
		default:
		}
	    }
	}
    }

    public static void returnBuild(Method method, JSONObject json) {
	JSONObject res = Factory.get();
	json.put("responses", res);
	JSONObject resDetail = Factory.get();
	res.put("200", resDetail);
	resDetail.put("description", "successful operation");
	JSONObject schema = Factory.get();
	resDetail.put("schema", schema);
	schema.put("type", "object");
	schema.put("title", "YDoc");
	JSONObject properties = Factory.get();

	Class<?> returnType = method.getReturnType();
	String desc = returnType.getName();
	if (returnType.isAnnotationPresent(ParamDesc.class)) {
	    ParamDesc annotation = returnType.getAnnotation(ParamDesc.class);
	    desc = annotation.value();
	}
	if (returnType.equals(List.class) || returnType.equals(Set.class)) {
	    schema.put("type", "array");
	    Type genericReturnType = method.getGenericReturnType();
	    if (genericReturnType instanceof ParameterizedType) {
		ParameterizedType genericReturnType1 = (ParameterizedType) genericReturnType;
		for (Type actualTypeArgument : genericReturnType1.getActualTypeArguments()) {
		    if (checkJavaType(actualTypeArgument.getTypeName())) {
			JSONObject jsonObject = Factory.get();
			properties.put(actualTypeArgument.getTypeName(), jsonObject);
			jsonObject.put("description", desc);
			jsonObject.put("type", convertType(actualTypeArgument.getTypeName()));
			schema.put("properties", properties);
		    } else {
			try {
			    Class<?> clazz = Class.forName(actualTypeArgument.getTypeName());
			    for (Field declaredField : clazz.getDeclaredFields()) {
				if (Modifier.isFinal(declaredField.getModifiers())) {
				    // final不处理
				    continue;
				}
				properties.put(declaredField.getName(), deepObject(Factory.get(), declaredField));
			    }
			    schema.put("properties", properties);
			} catch (ClassNotFoundException e) {
			    e.printStackTrace();
			}
		    }
		}
	    }
	} else if (checkJavaType(returnType.getName())) {
	    schema.put("type", convertType(returnType.getSimpleName()));
	    JSONObject jsonObject = Factory.get();
	    properties.put(returnType.getSimpleName(), jsonObject);
	    jsonObject.put("description", desc);
	    jsonObject.put("type", RequestBodyType.of(returnType.getSimpleName()).type);
	    schema.put("properties", properties);
	} else {
	    // 尝试从定义中获取，如果没有再去组装properties，这种情况可能对于使用了内部类不太友好解析不出来内容
	    if (returnType.getName().contains("$")) {
		schema.put("$ref", "#/definitions/"
			+ returnType.getName().substring(returnType.getName().lastIndexOf(".") + 1).replace("$", "."));
	    } else {
		schema.put("$ref", "#/definitions/" + returnType.getSimpleName());
	    }

	    for (Field declaredField : returnType.getDeclaredFields()) {
		properties.put(declaredField.getName(), deepObject(Factory.get(), declaredField));
	    }
	    schema.put("properties", properties);
	}

    }

    private static void get(String name, String path, JSONObject api, Method method, String outPath, String tag) {
	// 方法对象
	JSONObject apiMethod = Factory.get();
	api.put(outPath + path, apiMethod);
	// body
	JSONObject content = Factory.get();
	apiMethod.put("get", content);
	// restfulApi接口的描述/功能
	content.put("summary", name);
	content.put("description", name);
	// 处理get参数 1.如果不是pojo则必须带上@RequestParam用来获取参数描述信息
	List<JSONObject> parametersJson = new ArrayList<>();
	content.put("parameters", parametersJson);
	content.put("tags", Collections.singletonList(tag));
	// 循环方法内参数
	returnBuild(method, content);
	Parameter[] parameters = method.getParameters();
	for (Parameter parameterType : parameters) {
	    if (parameterType.isAnnotationPresent(ParamIgnore.class)) {
		continue;
	    }
	    boolean isObject = Boolean.FALSE;
	    JSONObject param = Factory.get();
	    // 参数名
	    String paramName = parameterType.getName();
	    // 参数对象类型
	    String paramType = parameterType.getType().getSimpleName();
	    // 参数描述
	    String paramDesc = parameterType.getName();
	    String in = "query";
	    boolean required = Boolean.FALSE;
	    // 1 参数有注解
	    if (parameterType.isAnnotationPresent(PathVariable.class)) {
		PathVariable annotation = parameterType.getAnnotation(PathVariable.class);
		paramName = annotation.value();
		required = Boolean.TRUE;
		paramDesc = StringUtils.hasText(annotation.name()) ? annotation.name() : "path参数";
		if (parameterType.isAnnotationPresent(ParamDesc.class)) {
		    ParamDesc pd = parameterType.getAnnotation(ParamDesc.class);
		    paramDesc = pd.value();
		}
		paramType = "string";
		in = "path";
	    } else if (parameterType.isAnnotationPresent(ParamDesc.class)) {
		ParamDesc annotation = parameterType.getAnnotation(ParamDesc.class);
		paramDesc = annotation.value();
		required = annotation.required();
	    } else {
		// 如果有其他注解则不对其生成操作
		if (parameterType.getDeclaredAnnotations().length > 1) {
		    continue;
		}
		// 2 参数无注解则有可能没加，或是pojo只获取第一层的参数
		if (!checkJavaType(parameterType.getParameterizedType().getTypeName())) {
		    isObject = Boolean.TRUE;
		    Field[] declaredFields = parameterType.getType().getDeclaredFields();
		    for (Field field : declaredFields) {
			if (Modifier.isFinal(field.getModifiers())) {
			    // final不处理
			    continue;
			}
			JSONObject bodyField = Factory.get();
			bodyField.put("name", field.getName());
			bodyField.put("in", "query");
			bodyField.put("type", convertType(field.getType().getSimpleName()));
			if (field.isAnnotationPresent(ParamDesc.class)) {
			    ParamDesc annotation = field.getAnnotation(ParamDesc.class);
			    bodyField.put("required", annotation.required());
			    bodyField.put("description", annotation.value());
			} else {
			    bodyField.put("required", Boolean.TRUE);
			    bodyField.put("description", field.getName());
			}
			parametersJson.add(bodyField);
		    }
		}
	    }
	    if (!isObject) {
		param.put("name", paramName);
		param.put("in", in);
		param.put("required", required);
		param.put("description", paramDesc);
		param.put("type", RequestBodyType.of(paramType).type);
		parametersJson.add(param);
	    }
	}
    }

    private static void post(String name, String path, JSONObject api, Method method, String outPath, String tag) {
	// 方法对象
	JSONObject apiMethod = Factory.get();
	api.put(outPath + path, apiMethod);
	// body
	JSONObject content = Factory.get();
	apiMethod.put("post", content);
	content.put("tags", Collections.singletonList(tag));
	// restfulApi接口的描述/功能
	content.put("summary", name);
	content.put("description", name);
	content.put("consumes", Collections.singleton("application/json"));
	// 处理get参数 1.如果不是pojo则必须带上@RequestParam用来获取参数描述信息
	List<JSONObject> parametersJson = new ArrayList<>();
	content.put("parameters", parametersJson);
	returnBuild(method, content);
	// restfulApi接口的描述/功能
	baseRequestBody(method.getParameters(), parametersJson);
    }

    private static void delete(String name, String path, JSONObject api, Method method, String outPath, String tag) {
	// 方法对象
	JSONObject apiMethod = Factory.get();
	api.put(outPath + path, apiMethod);
	// body
	JSONObject content = Factory.get();
	apiMethod.put("delete", content);
	content.put("tags", Collections.singletonList(tag));
	// restfulApi接口的描述/功能
	content.put("summary", name);
	content.put("description", name);
	content.put("consumes", Collections.singleton("application/json"));
	// 处理get参数 1.如果不是pojo则必须带上@RequestParam用来获取参数描述信息
	List<JSONObject> parametersJson = new ArrayList<>();
	content.put("parameters", parametersJson);
	returnBuild(method, content);
	// restfulApi接口的描述/功能
	baseRequestBody(method.getParameters(), parametersJson);
    }

    private static void put(String name, String path, JSONObject api, Method method, String outPath, String tag) {
	// 方法对象
	JSONObject apiMethod = Factory.get();
	api.put(outPath + path, apiMethod);
	// body
	JSONObject content = Factory.get();
	apiMethod.put("put", content);
	content.put("tags", Collections.singletonList(tag));
	// restfulApi接口的描述/功能
	content.put("summary", name);
	content.put("description", name);
	content.put("consumes", Collections.singleton("application/json"));
	// 处理get参数 1.如果不是pojo则必须带上@RequestParam用来获取参数描述信息
	List<JSONObject> parametersJson = new ArrayList<>();
	content.put("parameters", parametersJson);
	returnBuild(method, content);
	// restfulApi接口的描述/功能
	baseRequestBody(method.getParameters(), parametersJson);
    }

    private static void baseRequestBody(Parameter[] parameters, List<JSONObject> list) {
	for (Parameter parameter : parameters) {
	    if (parameter.isAnnotationPresent(PathVariable.class)) {
		PathVariable annotation = parameter.getAnnotation(PathVariable.class);
		JSONObject api = Factory.get();
		api.put("name", annotation.value());
		api.put("in", "path");
		api.put("required", Boolean.TRUE);
		api.put("description", StringUtils.hasText(annotation.name()) ? annotation.name() : "path参数");
		api.put("type", "string");
		list.add(api);
	    } else if (parameter.isAnnotationPresent(RequestBody.class)) {
		Class<?> type = parameter.getType();
		Field[] declaredFields = type.getDeclaredFields();
		JSONObject api = Factory.get();
		api.put("name", type.getSimpleName());
		api.put("in", "body");
		JSONObject schema = Factory.get();
		api.put("schema", schema);
		schema.put("title", type.getSimpleName());
		JSONObject properties = Factory.get();
		// schema.put("properties",properties);
		schema.put("$ref", "#/definitions/" + type.getSimpleName());
		JSONObject clone = (JSONObject) schema.clone();
		clone.put("type", RequestBodyType.OBJECT.type);
		clone.remove("$ref");
		Factory.definitions.put(type.getSimpleName(), clone);
		// 对象内properties第一层
		for (Field declaredField : declaredFields) {
		    if (declaredField.isAnnotationPresent(ParamIgnore.class)) {
			continue;
		    }
		    properties.put(declaredField.getName(), deepObject(Factory.get(), declaredField));
		}
		clone.put("properties", properties);
		list.add(api);
	    }
	}

    }

    private static JSONObject deepObject(JSONObject json, Field declaredField) {
	String desc = declaredField.getName();
	if (declaredField.isAnnotationPresent(ParamDesc.class)) {
	    ParamDesc annotation = declaredField.getAnnotation(ParamDesc.class);
	    desc = annotation.value();
	}
	if (declaredField.getType().isEnum()) {
	    // 常规类型
	    json.put("type", "integer");
	    Object[] enumConstants = declaredField.getType().getEnumConstants();
	    Set<String> jsonArray = new HashSet<>();
	    for (Object enumConstant : enumConstants) {
		jsonArray.add(enumConstant.toString());
	    }
	    json.put("description", JSON.toJSONString(jsonArray));
	    return json;
	}
	if (declaredField.getType().equals(List.class) || declaredField.getType().equals(Set.class)) {

	    Type genericType = declaredField.getGenericType();
	    ParameterizedType pt = (ParameterizedType) genericType;
	    Class<?> actualTypeArgument = (Class<?>) pt.getActualTypeArguments()[0];
	    json.put("type", RequestBodyType.ARRAY.type);
	    if (checkJavaType(actualTypeArgument.getTypeName())) {
		// 如果是普通类型
		JSONObject jsonObject = Factory.get();
		jsonObject.put("type", convertType(actualTypeArgument.getTypeName()));
		jsonObject.put("description", desc);
		json.put("items", jsonObject);
	    } else {
		// 如果是对象
		REF.add(actualTypeArgument.getName());
		JSONObject jsonObject = Factory.get();
		jsonObject.put("type", RequestBodyType.OBJECT.type);
		JSONObject filedObject = Factory.get();

		for (Field field : actualTypeArgument.getDeclaredFields()) {
		    if (Modifier.isFinal(field.getModifiers())) {
			// final不处理
			continue;
		    }
		    if (actualTypeArgument.equals(field.getType())) {
			break;
		    }
		    if (field.getType().equals(declaredField.getType())) {
			// User 里有 list<User> 会死递归
			break;
		    } else {
			// 对象嵌套
			if (REF.contains(actualTypeArgument.getName()) && !checkJavaType(field.getType().getName())) {
			    JSONObject refs = Factory.get();
			    refs.put("type", RequestBodyType.OBJECT.type);
			    json.put("items", refs);
			    if (actualTypeArgument.getName().contains("$")) {
				refs.put("$ref",
					"#/definitions/" + actualTypeArgument.getName()
						.substring(actualTypeArgument.getName().lastIndexOf(".") + 1)
						.replace("$", "."));
			    } else {
				refs.put("$ref", "#/definitions/" + actualTypeArgument.getSimpleName());
			    }
			    return json;
			}
			filedObject.put(field.getName(), deepObject(Factory.get(), field));
		    }

		}

		json.put("items", jsonObject);
		JSONObject clone = (JSONObject) filedObject.clone();
		clone.remove("$ref");
		JSONObject innerRef = new JSONObject();
		innerRef.put("properties", clone);
		innerRef.put("type", RequestBodyType.OBJECT.type);
		if (actualTypeArgument.getName().contains("$")) {
		    // 处理匿名内部类
		    Factory.definitions.put(actualTypeArgument.getName()
			    .substring(actualTypeArgument.getName().lastIndexOf(".") + 1).replace("$", "."), innerRef);
		    jsonObject.put("$ref", "#/definitions/" + actualTypeArgument.getName()
			    .substring(actualTypeArgument.getName().lastIndexOf(".") + 1).replace("$", "."));
		} else {
		    Factory.definitions.put(actualTypeArgument.getSimpleName(), innerRef);
		    jsonObject.put("$ref", "#/definitions/" + actualTypeArgument.getSimpleName());
		}

		json.put("description", desc);
	    }
	    return json;
	} else if (checkJavaType(declaredField.getType().getTypeName())) {
	    // 常规类型
	    json.put("type", convertType(declaredField.getType().getSimpleName()));
	    json.put("description", desc);
	    return json;
	} else {
	    // 修复 https://github.com/NoBugBoy/YDoc/issues/1
	    Class<?> declaringClass = declaredField.getType();
	    if (REF.contains(declaringClass.getName())) {
		if (declaringClass.getName().contains("$")) {
		    json.put("$ref", "#/definitions/" + declaringClass.getName()
			    .substring(declaringClass.getName().lastIndexOf(".") + 1).replace("$", "."));
		} else {
		    json.put("$ref", "#/definitions/" + declaringClass.getSimpleName());
		}
		return json;
	    }
	    REF.add(declaringClass.getName());
	    // json.put("type", RequestBodyType.OBJECT.type);
	    JSONObject objectTypeJSON = Factory.get();
	    for (Field field : declaringClass.getDeclaredFields()) {
		objectTypeJSON.put(field.getName(), deepObject(Factory.get(), field));
	    }
	    if (!declaringClass.getName().toLowerCase().contains("json")) {
		JSONObject jsonObject = Factory.get();
		jsonObject.put("properties", objectTypeJSON);
		jsonObject.put("type", RequestBodyType.OBJECT.type);
		jsonObject.put("title", declaringClass.getSimpleName());
		if (declaringClass.getName().contains("$")) {
		    // 处理匿名内部类
		    Factory.definitions.put(declaringClass.getName()
			    .substring(declaringClass.getName().lastIndexOf(".") + 1).replace("$", "."), jsonObject);
		    json.put("$ref", "#/definitions/" + declaringClass.getName()
			    .substring(declaringClass.getName().lastIndexOf(".") + 1).replace("$", "."));
		} else {
		    Factory.definitions.put(declaringClass.getSimpleName(), jsonObject);
		    json.put("$ref", "#/definitions/" + declaringClass.getSimpleName());
		}

	    } else {
		json.put("properties", objectTypeJSON);
	    }

	    json.put("description", desc);
	    return json;
	}

    }

    /**
     * 解决如果不是包装类型不是java开头的问题
     * 
     * @param name className
     * @return boolean
     */
    static boolean checkJavaType(String name) {
	if (name.startsWith("java")) {
	    return true;
	}
	switch (name.toLowerCase()) {
	case "int":
	case "long":
	case "short":
	case "double":
	case "float":
	case "byte":
	case "char":
	case "boolean":
	    return true;
	}
	return false;
    }

    static String convertType(String type) {
	type = type.toLowerCase();
	if (type.contains("java.lang")) {
	    type = type.substring(type.lastIndexOf(".") + 1);
	}
	if ("int".equals(type)) {
	    return "integer";
	}
	if (type.contains("time") || type.contains("date")) {
	    return "string(date-time)";
	}
	return RequestBodyType.of(type).type;
    }
}
