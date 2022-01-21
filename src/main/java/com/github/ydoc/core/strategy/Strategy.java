package com.github.ydoc.core.strategy;

import com.alibaba.fastjson.JSONObject;
import com.github.ydoc.core.kv.DocApi;
import com.github.ydoc.core.kv.Kv;

import java.util.List;

/**
 * @author yujian 顶层接口
 **/
public interface Strategy<O extends JSONObject> {
    /**
     * 核心生成方法入口
     *
     * @param api api
     */
    void generateApi(O api);

    /**
     * 公共返回处理
     * 
     * @param docApi  doc
     * @param content content
     */
    void baseResponse(DocApi docApi, Kv content);

    /**
     * 公共参数处理
     * 
     * @param docApi      doc
     * @param queryParams params
     */
    void baseRequest(DocApi docApi, List<Kv> queryParams);

}
