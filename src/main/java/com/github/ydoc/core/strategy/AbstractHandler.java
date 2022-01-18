package com.github.ydoc.core.strategy;

import com.alibaba.fastjson.JSONObject;
import com.github.ydoc.core.DocApi;
import com.github.ydoc.core.kv.Kv;
import com.github.ydoc.core.kv.KvFactory;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

/**
 * @author yujian
 * @see com.github.ydoc.core.strategy.IAbstractStrategy
 * @see com.github.ydoc.core.strategy.AbstractHandler
 **/
public abstract class AbstractHandler<T extends Annotation, O extends JSONObject> extends IAbstractStrategy<T, O>
	implements ResponseStrategy<DocApi>, RequestStrategy<DocApi> {

    protected void init(DocApi api, String[] path, String name, String method, String consumer) {
	Kv apiMethod = rebuildPath(api, path);
	List<Kv> parameters = KvFactory.get().lv2Parameters();
	Kv content = KvFactory.get().lv2Content(method, apiMethod, name, name, parameters,
		Collections.singletonList(api.getTag()), consumer);
	addHeader(parameters, api.getHeaders());
	this.processRequest(api, parameters);
	this.processResponse(api, content);
    }

    @Override
    public void processRequest(DocApi docApi, List<Kv> queryParams) {
	super.baseRequest(docApi, queryParams);
    }

    @Override
    public void processResponse(DocApi docApi, Kv content) {
	super.baseResponse(docApi, content);
    }
}
