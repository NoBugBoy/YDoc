package com.github.ydoc.core;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.ydoc.anno.ParamDesc;
import com.github.ydoc.core.RequestBodyType;
import com.github.ydoc.core.consts.Constans;
import com.github.ydoc.core.kv.Kv;
import com.github.ydoc.core.kv.KvFactory;
import com.github.ydoc.core.store.DefinitionsMap;
import com.github.ydoc.core.store.RefSet;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author nobugboy
 **/
public class Core {

    private static Kv enumProcess(Kv json, Object[] enumValues) {
	json.put("type", Constans.Type.INTEGER);
	Set<String> jsonArray = new HashSet<>();
	for (Object enumConstant : enumValues) {
	    jsonArray.add(enumConstant.toString());
	}
	json.put("description", JSON.toJSONString(jsonArray));
	return json;
    }

    public static void bodyRequired(Kv parent, Kv properties) {
	List<String> requireds = properties.keySet().stream().map(key -> {
	    JSONObject booleanObject = properties.getJSONObject(key);
	    Boolean r = booleanObject.getBoolean("required");
	    if (r != null && r) {
		return key;
	    }
	    return null;
	}).filter(Objects::nonNull).collect(Collectors.toList());
	parent.put("required", requireds);
    }

    private static Kv collectionProcess(Kv json, Field declaredField, String desc) {
	Type genericType = declaredField.getGenericType();
	ParameterizedType pt = (ParameterizedType) genericType;
	Class<?> actualTypeArgument = (Class<?>) pt.getActualTypeArguments()[0];
	json.put("type", RequestBodyType.ARRAY.type);
	if (checkJavaType(actualTypeArgument.getTypeName())) {
	    // 如果是普通类型
	    Kv jsonObject = KvFactory.get().simple(convertType(actualTypeArgument.getTypeName()), desc);
	    json.put("items", jsonObject);
	    return json;
	} else {
	    // 如果是对象
	    Kv jsonObject = KvFactory.get().simple(Constans.Type.OBJECT, desc);
	    Kv filedObject = KvFactory.get().empty();
	    for (Field field : actualTypeArgument.getDeclaredFields()) {
		if (Modifier.isFinal(field.getModifiers())) {
		    continue;
		}
		if (actualTypeArgument.equals(field.getType())) {
		    break;
		} else {
		    filedObject.put(field.getName(), deepObject(KvFactory.get().empty(), field));
		}

	    }
	    json.put("items", jsonObject);
	    Kv clone = (Kv) filedObject.clone();
	    clone.remove("$ref");
	    Kv innerRef = KvFactory.get().innerRef(clone, Constans.Type.OBJECT);
	    bodyRequired(innerRef, clone);
	    DefinitionsMap.get().putIfAbsent(actualTypeArgument.getSimpleName(), innerRef);
	    jsonObject.putReference(actualTypeArgument.getName(), actualTypeArgument.getSimpleName());
	    json.put("description", desc);
	}
	return json;
    }

    public static Kv deepObject(Kv json, Field declaredField, Type... t) {
	String desc = declaredField.getName();
	if (declaredField.isAnnotationPresent(ParamDesc.class)) {
	    ParamDesc annotation = declaredField.getAnnotation(ParamDesc.class);
	    desc = annotation.value();
	    json.put("required", annotation.required());
	}
	if (declaredField.getType().isEnum()) {
	    return enumProcess(json, declaredField.getType().getEnumConstants());
	}
	if (declaredField.getType().equals(List.class) || declaredField.getType().equals(Set.class)) {
	    return collectionProcess(json, declaredField, desc);
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
	    if (RefSet.get().contains(declaringClass.getName())) {
		json.putReference(declaringClass.getName(), declaringClass.getSimpleName());
		return json;
	    }
	    RefSet.get().add(declaringClass.getName());
	    Kv objectTypeJSON = KvFactory.get().empty();
	    for (Field field : getAllFiled(declaringClass)) {
		// final 禁序列化和 class不处理
		objectTypeJSON.put(field.getName(), deepObject(KvFactory.get().empty(), field));
	    }
	    if (!declaringClass.getName().toLowerCase().contains("json")) {
		Kv jsonObject = KvFactory.get().titleKv(declaringClass.getSimpleName(), objectTypeJSON,
			Constans.Type.OBJECT);
		bodyRequired(jsonObject, objectTypeJSON);
		DefinitionsMap.get().putIfAbsent(declaringClass.getSimpleName(), jsonObject);
		json.putReference(declaringClass.getName(), declaringClass.getSimpleName());
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
    public static boolean checkJavaType(String name) {
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
	default:
	}
	return false;
    };

    public static String convertType(String type) {
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

    public static Field[] getAllFiled(Class<?> clazz) {
	List<Field> fieldList = new ArrayList<>();
	while (clazz != null) {
	    fieldList.addAll(new ArrayList<>(Arrays.asList(clazz.getDeclaredFields())));
	    clazz = clazz.getSuperclass();
	}
	Field[] fields = new Field[fieldList.size()];
	return fieldList.toArray(fields);
    };

    public static Class<?> typeToClass(Type src) {
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
