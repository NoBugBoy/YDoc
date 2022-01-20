package com.github.ydoc.core.strategy;

import com.alibaba.fastjson.JSONObject;

import com.github.ydoc.anno.ParamDesc;
import com.github.ydoc.anno.ParamIgnore;
import com.github.ydoc.core.DocApi;
import com.github.ydoc.core.Factory;
import com.github.ydoc.core.RequestBodyType;
import com.github.ydoc.core.consts.Constans;
import com.github.ydoc.core.Core;
import com.github.ydoc.core.handler.middleware.MiddlewareSelect;
import com.github.ydoc.core.kv.Kv;
import com.github.ydoc.core.kv.KvFactory;
import com.github.ydoc.core.store.DefinitionsMap;
import com.github.ydoc.core.store.RefSet;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

import java.util.stream.Collectors;

/**
 * @author nobugboy
 **/
public abstract class IAbstractStrategy<T extends Annotation, O extends JSONObject> implements IStrategy<T, O> {

    protected Kv rebuildPath(DocApi docApi, String... paths) {
	String path = "";
	if (paths.length > 0) {
	    // base拼接restfulApi的路径
	    path = Factory.pathFormat.apply(path);
	}
	Kv apiMethod = KvFactory.get().empty();
	if (docApi.containsKey(docApi.getOutPath() + path)) {
	    // 路径已经存在
	    apiMethod = (Kv) docApi.get(docApi.getOutPath() + path);
	} else {
	    docApi.put(docApi.getOutPath() + path, apiMethod);
	}
	return apiMethod;
    }

    protected void addHeader(List<Kv> parametersJson, List<String> headers) {
		if(headers != null){
			List<Kv> commonsHeader = headers.stream().map(
					header -> KvFactory.get().lv3Params(header, Constans.In.HEADER, false, header, Constans.Type.STRING))
				.collect(Collectors.toList());
			parametersJson.addAll(commonsHeader);
		}
    }

    protected Kv deepObject(Kv json, Field declaredField, Type... t) {
	return Core.deepObject(json, declaredField, t);
    }

    /**
     * 解决如果不是包装类型不是java开头的问题
     *
     * @param name className
     * @return boolean
     */
    protected boolean checkJavaType(String name) {
	return Core.checkJavaType(name);
    }

    protected String convertType(String type) {
	return Core.convertType(type);
    }

    protected Field[] getAllFiled(Class<?> clazz) {
	return Core.getAllFiled(clazz);
    }

    protected Class<?> typeToClass(Type src) {
	return Core.typeToClass(src);
    }

    @Override
    public void baseResponse(DocApi docApi, Kv content) {
	Kv schema = KvFactory.get().lv3ResponseSchema(content);
	Kv properties = KvFactory.get().empty();
	Method method = docApi.getMethod();
	Class<?> returnType = method.getReturnType();
	String desc = returnType.getName();
	ParamDesc paramDesc;
	if (Objects.nonNull(paramDesc = AnnotationUtils.getAnnotation(returnType, ParamDesc.class))) {
	    desc = paramDesc.value();
	}
	if (returnType.equals(List.class) || returnType.equals(Set.class)) {
	    schema.put("type", Constans.Type.ARRAY);
	    Type genericReturnType = method.getGenericReturnType();
	    ParameterizedType pt = (ParameterizedType) genericReturnType;
	    Class<?> actualTypeArgument = (Class<?>) pt.getActualTypeArguments()[0];
	    content.put("type", RequestBodyType.ARRAY.type);
	    if (checkJavaType(actualTypeArgument.getTypeName())) {
		// 如果是普通类型
		schema.put("items", KvFactory.get().lv3ArrayItem(convertType(actualTypeArgument.getTypeName()), null));
	    } else {
		// 如果是对象
		Kv filedObject = KvFactory.get().empty();
		Kv objectKv = KvFactory.get().lv3ArrayItem(RequestBodyType.OBJECT.type, null);
		for (Field field : actualTypeArgument.getDeclaredFields()) {
		    if (Modifier.isFinal(field.getModifiers())) {
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
			if (!checkJavaType(field.getType().getName())) {
			    RefSet.get().flushRef(content, actualTypeArgument.getName(),
				    actualTypeArgument.getSimpleName());
			}
			filedObject.put(field.getName(), deepObject(KvFactory.get().empty(), field));
		    }

		}
		schema.put("items", objectKv);
		Kv clone = (Kv) filedObject.clone();
		clone.remove(Constans.Other.REF);
		Kv innerRef = KvFactory.get().innerRef(clone, Constans.Type.OBJECT);
		DefinitionsMap.get().putIfAbsent(actualTypeArgument.getSimpleName(), innerRef);
		objectKv.putReference(actualTypeArgument.getName(), actualTypeArgument.getSimpleName());
		schema.put("description", desc);
	    }
	} else if (checkJavaType(returnType.getName())) {
	    schema.put("type", convertType(returnType.getSimpleName()));
	    Kv jsonObject = KvFactory.get().simple(RequestBodyType.of(returnType.getSimpleName()).type, desc);
	    properties.put(returnType.getSimpleName(), jsonObject);
	    schema.put("properties", properties);
	} else {
	    // 判断是不是泛型
	    Type genericReturnType = method.getGenericReturnType();
	    Type objectType = null;
	    if (genericReturnType instanceof ParameterizedType) {
		Type[] actualTypeArguments = ((ParameterizedType) genericReturnType).getActualTypeArguments();
		// 暂时只处理单泛型，将该泛型指向object上
		if (actualTypeArguments.length > 0) {
		    objectType = actualTypeArguments[0];
		}
	    }
	    Kv objectTypeJson = KvFactory.get().empty();
	    for (Field declaredField : getAllFiled(returnType)) {
		// 临时支持单泛型返回值 https://github.com/NoBugBoy/YDoc/issues/8
		if (objectType != null && "Object".equals(declaredField.getType().getSimpleName())) {
		    // 将该类型指向给Object
		    objectTypeJson.put(declaredField.getName(),
			    deepObject(KvFactory.get().empty(), declaredField, objectType));
		} else {
		    objectTypeJson.put(declaredField.getName(), deepObject(KvFactory.get().empty(), declaredField));
		}

	    }
	    if (!returnType.getName().toLowerCase().contains("json")) {
		Kv jsonObject = KvFactory.get().titleKv(returnType.getSimpleName(), objectTypeJson,
			Constans.Type.OBJECT);
		DefinitionsMap.get().putAnonymous(returnType.getName(), returnType.getSimpleName(), jsonObject,
			objectType, schema);
	    } else {
		schema.put("properties", objectTypeJson);
	    }
	}
    }

    @Override
    public void baseRequest(DocApi docApi, List<Kv> queryParams) {
	Parameter[] parameters = docApi.getMethod().getParameters();
	for (Parameter parameter : parameters) {
	    if (parameter.isAnnotationPresent(ParamIgnore.class)) {
		continue;
	    }
	    MiddlewareSelect.select(parameter, queryParams);
	}
    }
}
