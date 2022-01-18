package com.github.ydoc.core.strategy;

import com.alibaba.fastjson.JSONObject;
import com.github.ydoc.core.kv.Kv;

/**
 * 响应
 * 
 * @author yujian
 */
public interface ResponseStrategy<O extends JSONObject> {
    void processResponse(O o, Kv content);
}
