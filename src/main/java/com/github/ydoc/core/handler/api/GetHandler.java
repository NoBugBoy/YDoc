package com.github.ydoc.core.handler.api;

import com.github.ydoc.core.DocApi;
import com.github.ydoc.core.strategy.AbstractHandler;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author nobugboy
 **/
public class GetHandler extends AbstractHandler<GetMapping, DocApi> {
    private static final String METHOD = "get";

    @Override
    public void generateApi(GetMapping anno, DocApi api) {
	super.init(api, anno.value(), anno.name(), METHOD, "");
    }

}
