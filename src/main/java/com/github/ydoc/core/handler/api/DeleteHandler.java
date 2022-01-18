package com.github.ydoc.core.handler.api;

import com.github.ydoc.core.DocApi;
import com.github.ydoc.core.kv.Kv;
import com.github.ydoc.core.kv.KvFactory;
import com.github.ydoc.core.strategy.AbstractHandler;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.Collections;
import java.util.List;

/**
 * @author nobugboy
 **/
public class DeleteHandler extends AbstractHandler<DeleteMapping, DocApi> {

    private static final String METHOD = "delete";
    private static final String CONSUMER = "application/json";

    @Override
    public void generateApi(DeleteMapping anno, DocApi api) {
	super.init(api, anno.value(), anno.name(), METHOD, CONSUMER);
	Kv apiMethod = rebuildPath(api, anno.value());
	List<Kv> parameters = KvFactory.get().lv2Parameters();
	Kv content = KvFactory.get().lv2Content(METHOD, apiMethod, anno.name(), anno.name(), parameters,
		Collections.singletonList(api.getTag()), CONSUMER);
	super.addHeader(parameters, api.getHeaders());
	this.processRequest(api, parameters);
	this.processResponse(api, content);
    }

}
