package com.github.ydoc.core;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.ydoc.anno.ParamDesc;
import com.github.ydoc.core.consts.Constans;
import com.github.ydoc.core.kv.Kv;
import com.github.ydoc.core.kv.KvFactory;
import com.github.ydoc.core.store.DefinitionsMap;
import com.github.ydoc.core.store.RefSet;
import org.springframework.core.ResolvableType;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * core static method
 * 
 * @author nobugboy
 **/
public class Core {
    private Core() {
    }

    /**
     * parse java enum to string
     * 
     * @param json       target
     * @param enumValues enums
     * @return kv
     */
    private static Kv enumProcess(Kv json, Object[] enumValues) {
	json.put(Constans.Key.TYPE, Constans.Type.INTEGER);
	Set<String> jsonArray = new HashSet<>();
	for (Object enumConstant : enumValues) {
	    jsonArray.add(enumConstant.toString());
	}
	json.put(Constans.Key.DESCRIPTION, JSON.toJSONString(jsonArray));
	return json;
    }

    /**
     * parse request body
     * 
     * @param parent     target
     * @param properties property
     */
    public static void bodyRequired(Kv parent, Kv properties) {
	List<String> requireds = properties.keySet().stream().map(key -> {
	    try {
		JSONObject booleanObject = properties.getJSONObject(key);
		Boolean r = booleanObject.getBoolean(Constans.Key.REQUIRED);
		if (r != null && r) {
		    return key;
		}
	    } catch (Exception e) {
		// noop
	    }

	    return null;
	}).filter(Objects::nonNull).collect(Collectors.toList());
	parent.put(Constans.Key.REQUIRED, requireds);
    }

    /**
     * parse collection
     * 
     * @param json        target
     * @param genericType genericType
     * @param desc        description
     * @return kv
     */
    public static Kv collectionProcess(Kv json, Type genericType, String desc) {
	ParameterizedType pt = (ParameterizedType) genericType;
	Class<?> actualTypeArgument = null;
	if (pt.getActualTypeArguments()[0] instanceof Class<?>) {
	    actualTypeArgument = (Class<?>) pt.getActualTypeArguments()[0];
	} else {
	    return json;
	}

	json.put(Constans.Key.TYPE, RequestBodyType.ARRAY.type);
	if (Utils.isPrimitive(actualTypeArgument)) {
	    // 如果是普通类型
	    Kv jsonObject = KvFactory.get().simple(convertType(actualTypeArgument.getTypeName()), desc);
	    json.put(Constans.Key.ITEMS, jsonObject);
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
	    json.put(Constans.Key.ITEMS, jsonObject);
	    Kv clone = (Kv) filedObject.clone();
	    clone.remove(Constans.Other.REF);
	    Kv innerRef = KvFactory.get().innerRef(clone, Constans.Type.OBJECT);
	    bodyRequired(innerRef, clone);
	    DefinitionsMap.get().putIfAbsent(actualTypeArgument.getSimpleName(), innerRef);
	    jsonObject.putReference(actualTypeArgument.getName(), actualTypeArgument.getSimpleName());
	    json.put(Constans.Key.DESCRIPTION, desc);
	}
	return json;
    }

    public static Kv deepObject(Kv json, Class<?> declaringClass) {
	String desc = declaringClass.getName();
	if (declaringClass.isAnnotationPresent(ParamDesc.class)) {
	    ParamDesc annotation = declaringClass.getAnnotation(ParamDesc.class);
	    desc = annotation.value();
	    json.put(Constans.Key.REQUIRED, annotation.required());
	}
	if (RefSet.get().contains(declaringClass.getName())) {
	    json.putReference(declaringClass.getName(), declaringClass.getSimpleName());
	    return json;
	}
	if (Utils.isPrimitive(declaringClass)) {
	    json.put(Constans.Key.TYPE, convertType(declaringClass.getSimpleName()));
	    json.put(Constans.Key.DESCRIPTION, desc);
	    return json;
	}
	RefSet.get().add(declaringClass.getName());
	Kv objectTypeJson = KvFactory.get().empty();
	for (Field field : getAllFiled(declaringClass)) {
	    // final 禁序列化和 class不处理
	    objectTypeJson.put(field.getName(), deepObject(KvFactory.get().empty(), field));
	}
	if (!declaringClass.getName().toLowerCase().contains("json")) {
	    Kv jsonObject = KvFactory.get().titleKv(declaringClass.getSimpleName(), objectTypeJson,
		    Constans.Type.OBJECT);
	    bodyRequired(jsonObject, objectTypeJson);
	    DefinitionsMap.get().putIfAbsent(declaringClass.getSimpleName(), jsonObject);
	    json.putReference(declaringClass.getName(), declaringClass.getSimpleName());
	} else {
	    json.put(Constans.Key.PROPERTIES, objectTypeJson);
	}
	json.put(Constans.Key.DESCRIPTION, desc);
	return json;
    }

    /**
     * deep recursion field
     * 
     * @param json          target json
     * @param declaredField field
     * @param t             types
     * @return kv
     */
    public static Kv deepObject(Kv json, Field declaredField, Type... t) {
	String desc = declaredField.getName();
	if (declaredField.isAnnotationPresent(ParamDesc.class)) {
	    ParamDesc annotation = declaredField.getAnnotation(ParamDesc.class);
	    desc = annotation.value();
	    json.put(Constans.Key.REQUIRED, annotation.required());
	}
	// enum
	if (declaredField.getType().isEnum()) {
	    return enumProcess(json, declaredField.getType().getEnumConstants());
	}
	// map
	if (Map.class.isAssignableFrom(declaredField.getType())) {
	    return mapProcess(json, declaredField, desc);
	}

	// collection
	if ((t.length > 0 && (t[0].getTypeName().contains("List") || t[0].getTypeName().contains("Set")))
		|| Collection.class.isAssignableFrom(declaredField.getType())) {
	    return collectionProcess(json, t.length > 0 ? t[0] : declaredField.getGenericType(), desc);
	}
	// primitive
	if (t.length == 0 && Utils.isPrimitive(declaredField.getType())) {
	    // 常规类型
	    json.put(Constans.Key.TYPE, convertType(declaredField.getType().getSimpleName()));
	    json.put(Constans.Key.DESCRIPTION, desc);
	    return json;
	}
	// object
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

	Kv objectTypeJson = KvFactory.get().empty();
	for (Field field : getAllFiled(declaringClass)) {
	    // final 禁序列化和 class不处理
	    objectTypeJson.put(field.getName(), deepObject(KvFactory.get().empty(), field));
	}
	if (!declaringClass.getName().toLowerCase().contains("json")) {
	    Kv jsonObject = KvFactory.get().titleKv(declaringClass.getSimpleName(), objectTypeJson,
		    Constans.Type.OBJECT);
	    bodyRequired(jsonObject, objectTypeJson);
	    DefinitionsMap.get().putIfAbsent(declaringClass.getName(), jsonObject);
	    json.putReference(declaringClass.getName(), declaringClass.getSimpleName());
	} else {
	    json.put(Constans.Key.PROPERTIES, objectTypeJson);
	}
	json.put(Constans.Key.DESCRIPTION, desc);
	return json;

    }

    /**
     * map
     * 
     * @param json          kv
     * @param declaredField field
     * @param desc          desc
     * @return kv
     */
    private static Kv mapProcess(Kv json, Field declaredField, String desc) {
	json.put(Constans.Key.TYPE, RequestBodyType.OBJECT.type);
	json.put(Constans.Key.DESCRIPTION, Utils.kv(desc));
	return json;
    }

    /**
     * 解决如果不是包装类型不是java开头的问题 转移到了 Utils.isPrimitive
     * 
     * @param name className
     * @return boolean
     */
    @Deprecated
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
    }

    /**
     * java type to swagger type
     * 
     * @param type java type
     * @return swagger type
     */
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

    /**
     * get this class and super class all fields
     * 
     * @param clazz this class
     * @return all fields
     */
    public static Field[] getAllFiled(Class<?> clazz) {
	List<Field> fieldList = new ArrayList<>();
	while (clazz != null) {
	    fieldList.addAll(new ArrayList<>(Arrays.asList(clazz.getDeclaredFields())));
	    clazz = clazz.getSuperclass();
	}
	Field[] fields = new Field[fieldList.size()];
	return fieldList.toArray(fields);
    }

    /**
     * type converted to instance class
     * 
     * @param src type
     * @return instance class
     */
    public static Class<?> typeToClass(Type src) {
	Class<?> result = null;
	if (src instanceof Class) {
	    result = (Class<?>) src;
	} else if (src instanceof ParameterizedType) {
	    result = (Class<?>) ((ParameterizedType) src).getRawType();
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

    /**
     * any proxy annotation object converted to real annotation name
     * 
     * @param proxy proxy annotation object
     * @return annotation name
     */
    public static String proxyToTargetClassName(Object proxy) {
	Field h = null;
	try {
	    h = proxy.getClass().getSuperclass().getDeclaredField("h");
	    h.setAccessible(true);
	    Object h_instance = h.get(proxy);
	    Field type = h_instance.getClass().getDeclaredField(Constans.Key.TYPE);
	    type.setAccessible(true);
	    Object type_instance = type.get(h_instance);
	    Field name = type_instance.getClass().getDeclaredField("name");
	    name.setAccessible(true);
	    Object className = name.get(type_instance);
	    return className.toString().substring(className.toString().lastIndexOf(Constans.Other.DOT) + 1);
	} catch (NoSuchFieldException | IllegalAccessException e) {
	    return null;
	}

    }

    /**
     * get now impl version
     * 
     * @return pom xml version
     */
    public static String getVersion() {
	Package aPackage = Core.class.getPackage();
	return aPackage == null ? "" : aPackage.getImplementationVersion();
    }
}
