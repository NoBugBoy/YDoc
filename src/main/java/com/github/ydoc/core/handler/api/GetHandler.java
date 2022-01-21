package com.github.ydoc.core.handler.api;

import com.github.ydoc.core.kv.DocApi;
import com.github.ydoc.core.strategy.AbstractHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author nobugboy
 **/
public class GetHandler extends AbstractHandler<GetMapping, DocApi> {
    private static final String METHOD = "get";

    @Override
    public void generateApi(DocApi api) {
	super.init(api, new RequestMethod[] { RequestMethod.GET }, getProxy().value(), getProxy().name(), METHOD, "");
    }

}
