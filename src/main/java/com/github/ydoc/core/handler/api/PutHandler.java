package com.github.ydoc.core.handler.api;

import com.github.ydoc.core.DocApi;
import com.github.ydoc.core.strategy.AbstractHandler;
import org.springframework.web.bind.annotation.PutMapping;

/**
 * @author nobugboy
 **/
public class PutHandler extends AbstractHandler<PutMapping, DocApi> {

    private static final String METHOD = "put";
    private static final String CONSUMER = "application/json";

    @Override
    public void generateApi(PutMapping anno, DocApi api) {
	super.init(api, anno.value(), anno.name(), METHOD, CONSUMER);
    }
}
