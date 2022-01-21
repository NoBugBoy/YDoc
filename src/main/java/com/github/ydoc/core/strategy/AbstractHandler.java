package com.github.ydoc.core.strategy;

import com.alibaba.fastjson.JSONObject;
import com.github.ydoc.core.kv.DocApi;
import com.github.ydoc.core.Utils;
import com.github.ydoc.core.kv.Kv;
import com.github.ydoc.core.kv.KvFactory;
import com.google.common.base.Strings;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

/**
 * @author yujian
 * @see com.github.ydoc.core.strategy.AbstractStrategy
 **/
public abstract class AbstractHandler<T extends Annotation, O extends JSONObject> extends AbstractStrategy<T, O>
	implements ResponseStrategy<DocApi>, RequestStrategy<DocApi> {

    protected void init(DocApi api, RequestMethod[] requestMethods, String[] path, String name, String method,
	    String consumer) {
	if (requestMethods.length > 0) {
	    api.setMethodName(requestMethods[0]);
	}
	if (path.length > 0) {
	    addPath(path[0], api);
	}
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

    public static void addPath(String path, DocApi docApi) {
	if (!Strings.isNullOrEmpty(path)) {
	    // base拼接restfulApi的路径
	    path = Utils.pathFormat.apply(path);
	    docApi.setOutPath(docApi.getOutPath() + path);
	}
    }
}
