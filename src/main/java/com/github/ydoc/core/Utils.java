package com.github.ydoc.core;

import com.alibaba.fastjson.JSONObject;

import java.util.function.Function;

/**
 * @author nobugboy
 **/
public class Utils {
    private static final String JAVA = "java";

    private Utils() {
    }

    public static boolean isPrimitive(Class<?> clazz) {
	if (null == clazz) {
	    return false;
	}
	if (clazz.getName().startsWith(JAVA)) {
	    return true;
	}
	if (clazz.isPrimitive() || String.class.isAssignableFrom(clazz)) {
	    return true;
	}
	try {
	    return ((Class<?>) clazz.getField("TYPE").get(null)).isPrimitive();
	} catch (Exception e) {
	    return false;
	}

    }

    public static String page;
    private static final String SED = "/";

    public static JSONObject definitions = new JSONObject();

    public static Function<String, String> pathFormat = path -> {
	if (!path.startsWith(SED)) {
	    path = SED + path;
	}
	if (path.endsWith(SED)) {
	    path = path.substring(0, path.length() - 1);
	}
	return path;
    };

    public static String kv(String desc) {
	return desc + "(dynamic json K,V)";
    }
}
