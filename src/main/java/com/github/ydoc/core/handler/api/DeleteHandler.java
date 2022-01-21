package com.github.ydoc.core.handler.api;

import com.github.ydoc.core.kv.DocApi;
import com.github.ydoc.core.strategy.AbstractHandler;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author nobugboy
 **/
public class DeleteHandler extends AbstractHandler<DeleteMapping, DocApi> {

    private static final String METHOD = "delete";
    private static final String CONSUMER = "application/json";

    @Override
    public void generateApi(DocApi api) {
	super.init(api, new RequestMethod[] { RequestMethod.DELETE }, getProxy().value(), getProxy().name(), METHOD,
		CONSUMER);
    }

}
