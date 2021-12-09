package com.github.ydoc.core;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

import javafx.animation.FadeTransition;
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
    private static final Set<String> REF = new HashSet<>();

    private static final List<String> HEADERS = new ArrayList<>();

    public static void setHeaders(List<String> headers) {
    	if(headers != null){
			HEADERS.addAll(headers);
		}
    }

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

	    ParameterizedType pt = (ParameterizedType) genericReturnType;
	    Class<?> actualTypeArgument = (Class<?>) pt.getActualTypeArguments()[0];
	    json.put("type", RequestBodyType.ARRAY.type);
	    if (checkJavaType(actualTypeArgument.getTypeName())) {
		// 如果是普通类型
		JSONObject jsonObject = Factory.get();
		jsonObject.put("type", convertType(actualTypeArgument.getTypeName()));
		schema.put("items", jsonObject);
	    } else {
		// 如果是对象
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
		    if (actualTypeArgument.isAssignableFrom(field.getType())) {
			// User 里有 list<User> 会死递归
			break;
		    } else {
			// 对象嵌套
			if (!REF.contains(actualTypeArgument.getName()) && !checkJavaType(field.getType().getName())) {
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
			    REF.add(actualTypeArgument.getName());

			}
			filedObject.put(field.getName(), deepObject(Factory.get(), field));
		    }

		}

		schema.put("items", jsonObject);
		JSONObject clone = (JSONObject) filedObject.clone();
		clone.remove("$ref");
		JSONObject innerRef = new JSONObject();
		innerRef.put("properties", clone);
		innerRef.put("type", RequestBodyType.OBJECT.type);

		if (actualTypeArgument.getName().contains("$")) {
		    // 处理匿名内部类
		    Factory.definitions.putIfAbsent(actualTypeArgument.getName()
			    .substring(actualTypeArgument.getName().lastIndexOf(".") + 1).replace("$", "."), innerRef);
		    jsonObject.put("$ref", "#/definitions/" + actualTypeArgument.getName()
			    .substring(actualTypeArgument.getName().lastIndexOf(".") + 1).replace("$", "."));
		} else {
		    Factory.definitions.putIfAbsent(actualTypeArgument.getSimpleName(), innerRef);
		    jsonObject.put("$ref", "#/definitions/" + actualTypeArgument.getSimpleName());
		}

		schema.put("description", desc);
	    }
	} else if (checkJavaType(returnType.getName())) {
	    schema.put("type", convertType(returnType.getSimpleName()));
	    JSONObject jsonObject = Factory.get();
	    properties.put(returnType.getSimpleName(), jsonObject);
	    jsonObject.put("description", desc);
	    jsonObject.put("type", RequestBodyType.of(returnType.getSimpleName()).type);
	    schema.put("properties", properties);
	} else {
	    // 判断是不是泛型
	    Type genericReturnType = method.getGenericReturnType();
	    Type objectType = null;
	    if (genericReturnType instanceof ParameterizedType) {
		// 有泛型
		// 多泛型
		Type[] actualTypeArguments = ((ParameterizedType) genericReturnType).getActualTypeArguments();
		// 暂时只处理单泛型，将该泛型指向object上
		if (actualTypeArguments.length > 0) {
		    objectType = actualTypeArguments[0];
		}
	    }
	    JSONObject objectTypeJSON = Factory.get();
	    for (Field declaredField : getAllFiled(returnType)) {
		// 临时支持单泛型返回值 https://github.com/NoBugBoy/YDoc/issues/8
		if (objectType != null && "Object".equals(declaredField.getType().getSimpleName())) {
		    // 将该类型指向给Object
		    objectTypeJSON.put(declaredField.getName(), deepObject(Factory.get(), declaredField, objectType));
		} else {
		    objectTypeJSON.put(declaredField.getName(), deepObject(Factory.get(), declaredField));
		}

	    }
	    if (!returnType.getName().toLowerCase().contains("json")) {
		JSONObject jsonObject = Factory.get();
		jsonObject.put("properties", objectTypeJSON);
		jsonObject.put("type", RequestBodyType.OBJECT.type);
		jsonObject.put("title", returnType.getSimpleName());
		if (returnType.getName().contains("$")) {
		    if (objectType != null) {
			// 处理匿名内部类
			Factory.definitions
				.put(returnType.getName().substring(returnType.getName().lastIndexOf(".") + 1)
					.replace("$", ".") + "<" + objectType.getTypeName() + ">", jsonObject);
			schema.put("$ref",
				"#/definitions/" + returnType.getName()
					.substring(returnType.getName().lastIndexOf(".") + 1).replace("$", ".") + "<"
					+ objectType.getTypeName() + ">");
		    } else {
			Factory.definitions.put(returnType.getName()
				.substring(returnType.getName().lastIndexOf(".") + 1).replace("$", "."), jsonObject);
		    }
		} else {
		    if (objectType != null) {
			Factory.definitions.put(returnType.getSimpleName() + "<" + objectType.getTypeName() + ">",
				jsonObject);
			schema.put("$ref",
				"#/definitions/" + returnType.getSimpleName() + "<" + objectType.getTypeName() + ">");
		    } else {
			Factory.definitions.put(returnType.getSimpleName(), jsonObject);
			schema.put("$ref", "#/definitions/" + returnType.getSimpleName());
		    }
		}

	    } else {
		schema.put("properties", objectTypeJSON);
	    }
	}

    }

    private static void addHeader(List<JSONObject> parametersJson) {
	List<JSONObject> commonsHeader = HEADERS.stream().map(header -> {
	    JSONObject h = Factory.get();
	    h.put("name", header);
	    h.put("in", "header");
	    h.put("required", false);
	    h.put("description", header);
	    h.put("type", "string");
	    return h;
	}).collect(Collectors.toList());
	parametersJson.addAll(commonsHeader);
    }

    private static void get(String name, String path, JSONObject api, Method method, String outPath, String tag) {
	// 方法对象
	JSONObject apiMethod = Factory.get();
	if (api.containsKey(outPath + path)) {
	    // 路径已经存在
	    apiMethod = (JSONObject) api.get(outPath + path);
	} else {
	    api.put(outPath + path, apiMethod);
	}
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
	addHeader(parametersJson);
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
	    } else if (parameterType.isAnnotationPresent(RequestHeader.class)) {
		RequestHeader header = parameterType.getAnnotation(RequestHeader.class);
		paramName = header.name();
		required = header.required();
		paramType = "string";
		in = "header";

	    } else {
		// 如果有其他注解则不对其生成操作
		if (parameterType.getDeclaredAnnotations().length > 1) {
		    continue;
		}
		// 2 参数无注解则有可能没加，或是pojo只获取第一层的参数
		if (!checkJavaType(parameterType.getParameterizedType().getTypeName())) {
		    isObject = Boolean.TRUE;
		    Field[] declaredFields = getAllFiled(parameterType.getType());
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
	if (api.containsKey(outPath + path)) {
	    // 路径已经存在
	    apiMethod = (JSONObject) api.get(outPath + path);
	} else {
	    api.put(outPath + path, apiMethod);
	}
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
	addHeader(parametersJson);
	content.put("parameters", parametersJson);
	returnBuild(method, content);
	// restfulApi接口的描述/功能
	baseRequestBody(method.getParameters(), parametersJson);
    }

    private static void delete(String name, String path, JSONObject api, Method method, String outPath, String tag) {
	// 方法对象
	JSONObject apiMethod = Factory.get();
	if (api.containsKey(outPath + path)) {
	    // 路径已经存在
	    apiMethod = (JSONObject) api.get(outPath + path);
	} else {
	    api.put(outPath + path, apiMethod);
	}
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
	addHeader(parametersJson);
	content.put("parameters", parametersJson);
	returnBuild(method, content);
	// restfulApi接口的描述/功能
	baseRequestBody(method.getParameters(), parametersJson);
    }

    private static void put(String name, String path, JSONObject api, Method method, String outPath, String tag) {
	// 方法对象
	JSONObject apiMethod = Factory.get();
	if (api.containsKey(outPath + path)) {
	    // 路径已经存在
	    apiMethod = (JSONObject) api.get(outPath + path);
	} else {
	    api.put(outPath + path, apiMethod);
	}
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
	addHeader(parametersJson);
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
		Field[] declaredFields = getAllFiled(type);
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
	    } else if (parameter.isAnnotationPresent(RequestHeader.class)) {
		RequestHeader header = parameter.getAnnotation(RequestHeader.class);
		JSONObject api = Factory.get();
		api.put("name", header.name());
		api.put("in", "header");
		api.put("required", Boolean.FALSE);
		api.put("description", header.name());
		api.put("type", "string");
		list.add(api);
	    }
	}

    }

    private static JSONObject deepObject(JSONObject json, Field declaredField, Type... t) {
	String desc = declaredField.getName();
	if (declaredField.isAnnotationPresent(ParamDesc.class)) {
	    ParamDesc annotation = declaredField.getAnnotation(ParamDesc.class);
	    desc = annotation.value();
		json.put("required",annotation.required());
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
		return json;
	    } else {
		// 如果是对象
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
		    } else {
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
		    Factory.definitions.putIfAbsent(actualTypeArgument.getName()
			    .substring(actualTypeArgument.getName().lastIndexOf(".") + 1).replace("$", "."), innerRef);
		    jsonObject.put("$ref", "#/definitions/" + actualTypeArgument.getName()
			    .substring(actualTypeArgument.getName().lastIndexOf(".") + 1).replace("$", "."));
		} else {
		    Factory.definitions.putIfAbsent(actualTypeArgument.getSimpleName(), innerRef);
		    jsonObject.put("$ref", "#/definitions/" + actualTypeArgument.getSimpleName());
		}

		json.put("description", desc);
	    }
	    return json;
	} else if (t.length == 0 && checkJavaType(declaredField.getType().getTypeName())) {
	    // 常规类型
	    json.put("type", convertType(declaredField.getType().getSimpleName()));
	    json.put("description", desc);
	    return json;
	} else {
	    // 修复 https://github.com/NoBugBoy/YDoc/issues/1
	    Class<?> declaringClass = declaredField.getType();
	    if (t.length > 0) {
		if (t[0] instanceof ParameterizedType) {
		    ParameterizedType pt = (ParameterizedType) t[0];
		    declaringClass = (Class<?>) pt.getActualTypeArguments()[0];
		} else if (t[0] instanceof Class<?>) {
		    declaringClass = (Class<?>) t[0];
		} else {
		    declaringClass = typeToClass(t[0]);
		}
	    }
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
	    JSONObject objectTypeJSON = Factory.get();
	    for (Field field : getAllFiled(declaringClass)) {
		// final 禁序列化和 class不处理
		objectTypeJSON.put(field.getName(), deepObject(Factory.get(), field));
	    }
	    if (!declaringClass.getName().toLowerCase().contains("json")) {
		JSONObject jsonObject = Factory.get();
		jsonObject.put("properties", objectTypeJSON);
		jsonObject.put("type", RequestBodyType.OBJECT.type);
		jsonObject.put("title", declaringClass.getSimpleName());
		if (declaringClass.getName().contains("$")) {
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

    static Field[] getAllFiled(Class<?> clazz) {
	List<Field> fieldList = new ArrayList<>();
	while (clazz != null) {
	    fieldList.addAll(new ArrayList<>(Arrays.asList(clazz.getDeclaredFields())));
	    clazz = clazz.getSuperclass();
	}
	Field[] fields = new Field[fieldList.size()];
	return fieldList.toArray(fields);
    }

    private static Class<?> typeToClass(Type src) {
	Class<?> result = null;
	// 如果src是Class类型的实例则直接进行强制类型转换
	if (src instanceof Class) {
	    result = (Class<?>) src;
	    // 如果src是参数类型则获取其原始类型Class对象；
	} else if (src instanceof ParameterizedType) {
	    result = (Class<?>) ((ParameterizedType) src).getRawType();
	    //
	} else if (src instanceof GenericArrayType) {
	    Type componentType = ((GenericArrayType) src).getGenericComponentType();
	    if (componentType instanceof Class) {
		result = Array.newInstance((Class<?>) componentType, 0).getClass();
	    } else {
		Class<?> componentClass = typeToClass(componentType);
		result = Array.newInstance(componentClass, 0).getClass();
	    }
	}
	if (result == null) {
	    result = Object.class;
	}
	return result;
    }
}
