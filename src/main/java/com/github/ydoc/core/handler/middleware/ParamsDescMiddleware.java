package com.github.ydoc.core.handler.middleware;

import com.github.ydoc.anno.ParamDesc;
import com.github.ydoc.core.RequestBodyType;
import com.github.ydoc.core.consts.Constans;
import com.github.ydoc.core.kv.Kv;
import com.github.ydoc.core.kv.KvFactory;

import java.lang.reflect.Parameter;
import java.util.List;

/**
 * @author nobugboy
 **/
public class ParamsDescMiddleware extends AbstractMiddleware<ParamDesc> {

    @Override
    public void doHandle(List<Kv> target, Parameter parameter) {
	target.add(KvFactory.get().lv3Params(parameter.getName(), Constans.In.QUERY, getProxy().required(),
		getProxy().value(), RequestBodyType.of(parameter.getType().getSimpleName()).type));
    }

    @Override
    public int getOrder() {
	return -1;
    }
}
