package com.github.ydoc.core.handler.middleware;

import com.github.ydoc.core.RequestBodyType;
import com.github.ydoc.core.consts.Constans;
import com.github.ydoc.core.handler.Middleware;
import com.github.ydoc.core.kv.Kv;
import com.github.ydoc.core.kv.KvFactory;
import org.springframework.web.bind.annotation.RequestHeader;

import java.lang.reflect.Parameter;
import java.util.List;

/**
 * @author nobugboy
 **/
public class RequestHeaderMiddleware implements Middleware<RequestHeader> {

    @Override
    public void doHandle(List<Kv> target, Parameter parameter, RequestHeader requestHeader) {
	target.add(KvFactory.get().lv3Params(parameter.getName(), Constans.In.HEADER, requestHeader.required(),
		parameter.getName(), RequestBodyType.of(Constans.Type.STRING).type));
    }
}
