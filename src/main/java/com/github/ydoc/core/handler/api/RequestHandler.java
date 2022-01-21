package com.github.ydoc.core.handler.api;

import com.github.ydoc.core.kv.DocApi;
import com.github.ydoc.core.strategy.AbstractHandler;
import org.springframework.web.bind.annotation.*;

/**
 * @author nobugboy
 **/
public class RequestHandler extends AbstractHandler<RequestMapping, DocApi> {
    private static final String CONSUMER = "application/json";

    @Override
    public void generateApi(DocApi api) {
	RequestMethod requestMethod = api.getMethodName();

	if (getProxy() != null) {
	    switch (requestMethod) {
	    case GET:
		super.init(api, getProxy().method(), getProxy().value(), getProxy().name(), "get", "");
		return;
	    case PUT:
		super.init(api, getProxy().method(), getProxy().value(), getProxy().name(), "put", CONSUMER);
		return;
	    case POST:
		super.init(api, getProxy().method(), getProxy().value(), getProxy().name(), "post", CONSUMER);
		return;
	    case DELETE:
		super.init(api, getProxy().method(), getProxy().value(), getProxy().name(), "delete", CONSUMER);
		return;
	    default:
	    }
	}
    }
}
