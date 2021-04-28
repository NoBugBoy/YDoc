package com.github.ydoc.yapi;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.List;

/**
 * author yujian
 * description post put delete 请求参数json
 * create 2021-04-23 18:01
 **/
@Data
public class RequestBodyJson {
    private String       type = RequestBodyType.OBJECT.type;
    private String       title = "empty object";
    private JSONObject       properties;
    private List<String> required;
}
