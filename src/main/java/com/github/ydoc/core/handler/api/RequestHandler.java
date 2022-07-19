package com.github.ydoc.core.handler.api;

import com.github.ydoc.core.kv.DocApi;
import com.github.ydoc.core.strategy.AbstractHandler;
import com.github.ydoc.exception.YdocException;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Collections;

/**
 * @author nobugboy
 **/
public class RequestHandler extends AbstractHandler<RequestMapping, DocApi> {
    private static final String CONSUMER = "application/json";

    @Override
    public void generateApi(DocApi api) {

	if (getProxy() != null) {
	    if (getProxy().method().length == 0) {
		throw new YdocException("method attribute not found in @RequestMapping");
	    }
	    RequestMethod requestMethod = getProxy().method()[0];

	    switch (requestMethod) {
	    case GET:
		super.init(api, new RequestMethod[] { RequestMethod.GET }, getProxy().value(), getProxy().name(), "get",
			"");
		return;
	    case PUT:
		super.init(api, new RequestMethod[] { RequestMethod.PUT }, getProxy().value(), getProxy().name(), "put",
			CONSUMER);
		return;
	    case POST:
		super.init(api, new RequestMethod[] { RequestMethod.POST }, getProxy().value(), getProxy().name(),
			"post", CONSUMER);
		return;
	    case DELETE:
		super.init(api, new RequestMethod[] { RequestMethod.DELETE }, getProxy().value(), getProxy().name(),
			"delete", CONSUMER);
		return;
	    default:
	    }
	}
    }
}
