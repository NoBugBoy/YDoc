package com.github.ydoc.core.handler.middleware;

import com.github.ydoc.anno.ParamDesc;
import com.github.ydoc.core.RequestBodyType;
import com.github.ydoc.core.consts.Constans;
import com.github.ydoc.core.handler.Middleware;
import com.github.ydoc.core.kv.Kv;
import com.github.ydoc.core.kv.KvFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;

import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Objects;

/**
 * @author nobugboy
 **/
public class PathVariableMiddleware implements Middleware<PathVariable> {

    private static final String DEFAULT_DESC = "Path参数";

    @Override
    public void doHandle(List<Kv> target, Parameter parameter, PathVariable pathVariable) {
	String paramDesc = StringUtils.hasText(pathVariable.name()) ? pathVariable.name() : DEFAULT_DESC;
	ParamDesc pd = AnnotationUtils.getAnnotation(parameter, ParamDesc.class);
	if (Objects.nonNull(pd)) {
	    paramDesc = pd.value();
	}
	target.add(KvFactory.get().lv3Params(pathVariable.value(), Constans.In.PATH, pathVariable.required(), paramDesc,
		RequestBodyType.of(Constans.Type.STRING).type));
    }
}
