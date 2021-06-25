package com.github.ydoc.core;

import com.alibaba.fastjson.JSONObject;

/**
 * author yujian
 * description
 * create 2021-04-27 10:30
 **/
public class Factory {
    public static String json;
    public static String page;
    public static JSONObject get(){
        return new JSONObject();
    }
    public static JSONObject definitions = new JSONObject();
}
