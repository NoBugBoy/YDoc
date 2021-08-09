package com.github.ydoc.core;

import com.alibaba.fastjson.JSONObject;

import java.util.function.Function;

/**
 * author NoBugBoY description create 2021-04-27 10:30
 **/
public class Factory {
    public static String json;
    public static String page;

    public static JSONObject get() {
	return new JSONObject();
    }

    public static JSONObject definitions = new JSONObject();

    public static Function<String, String> pathFormat = path -> {
	if (!path.startsWith("/")) {
	    path = "/" + path;
	}
	if (path.endsWith("/")) {
	    path = path.substring(0, path.length() - 1);
	}
	return path;
    };
}
