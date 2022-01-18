package com.github.ydoc.core.strategy;

import com.alibaba.fastjson.JSONObject;
import com.github.ydoc.core.kv.Kv;

import java.util.List;

/**
 * 处理请求
 * 
 * @author yujian
 */
public interface RequestStrategy<O extends JSONObject> {

    void processRequest(O o, List<Kv> parameters);
}
