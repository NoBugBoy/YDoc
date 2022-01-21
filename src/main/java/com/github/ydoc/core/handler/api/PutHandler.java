package com.github.ydoc.core.handler.api;

import com.github.ydoc.core.kv.DocApi;
import com.github.ydoc.core.strategy.AbstractHandler;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author nobugboy
 **/
public class PutHandler extends AbstractHandler<PutMapping, DocApi> {

    private static final String METHOD = "put";
    private static final String CONSUMER = "application/json";

    @Override
    public void generateApi(DocApi api) {
	super.init(api, new RequestMethod[] { RequestMethod.PUT }, getProxy().value(), getProxy().name(), METHOD,
		CONSUMER);
    }
}
