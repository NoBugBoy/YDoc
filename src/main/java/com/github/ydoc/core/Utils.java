package com.github.ydoc.core;

import com.alibaba.fastjson.JSONObject;

import java.util.function.Function;

/**
 * @author nobugboy
 **/
public class Utils {

    private Utils() {
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
}
