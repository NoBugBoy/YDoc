package com.github.ydoc.core.handler.middleware;

import com.github.ydoc.core.RequestBodyType;
import com.github.ydoc.core.consts.Constans;
import com.github.ydoc.core.handler.Middleware;
import com.github.ydoc.core.kv.Kv;
import com.github.ydoc.core.kv.KvFactory;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.reflect.Parameter;
import java.util.List;

/**
 * @author nobugboy
 **/
public class RequestParamMiddleware implements Middleware<RequestParam> {

    @Override
    public void doHandle(List<Kv> target, Parameter parameter, RequestParam requestParam) {
	target.add(KvFactory.get().lv3Params(parameter.getName(), Constans.In.QUERY, requestParam.required(),
		requestParam.value(), RequestBodyType.of(parameter.getType().getSimpleName()).type));
    }
}
