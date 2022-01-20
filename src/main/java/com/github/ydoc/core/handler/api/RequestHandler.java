package com.github.ydoc.core.handler.api;

import com.github.ydoc.core.DocApi;
import com.github.ydoc.core.strategy.AbstractHandler;
import org.springframework.web.bind.annotation.*;

/**
 * @author nobugboy
 **/
public class RequestHandler extends AbstractHandler<RequestMapping, DocApi> {
    private static final String CONSUMER = "application/json";
    @Override
    public void generateApi(RequestMapping anno, DocApi api) {
        RequestMethod requestMethod = api.getMethodName();
        if (anno != null) {
            switch (requestMethod) {
                case GET:
                    super.init(api, anno.value(), anno.name(), "get", "");
                    return;
                case PUT:
                    super.init(api, anno.value(), anno.name(), "put", CONSUMER);
                    return;
                case POST:
                    super.init(api, anno.value(), anno.name(), "post", CONSUMER);
                    return;
                case DELETE:
                    super.init(api, anno.value(), anno.name(), "delete", CONSUMER);
                    return;
                default:
            }
        }
    }
}
