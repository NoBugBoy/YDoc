package com.github.ydoc.core.handler.api;

import com.github.ydoc.core.kv.DocApi;
import com.github.ydoc.core.strategy.AbstractHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author nobugboy
 **/
public class PostHandler extends AbstractHandler<PostMapping, DocApi> {

    private static final String METHOD = "post";
    private static final String CONSUMER = "application/json";

    @Override
    public void generateApi(DocApi api) {
	super.init(api, new RequestMethod[] { RequestMethod.POST }, getProxy().value(), getProxy().name(), METHOD,
		CONSUMER);
    }
}
