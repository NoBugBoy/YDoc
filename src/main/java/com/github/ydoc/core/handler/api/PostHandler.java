package com.github.ydoc.core.handler.api;

import com.github.ydoc.core.DocApi;
import com.github.ydoc.core.strategy.AbstractHandler;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @author nobugboy
 **/
public class PostHandler extends AbstractHandler<PostMapping, DocApi> {

    private static final String METHOD = "post";
    private static final String CONSUMER = "application/json";

    @Override
    public void generateApi(PostMapping anno, DocApi api) {
	super.init(api, anno.value(), anno.name(), METHOD, CONSUMER);
    }
}
