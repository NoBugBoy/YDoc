package com.github.ydoc.core.handler.middleware;

import com.github.ydoc.anno.ParamDesc;
import com.github.ydoc.core.RequestBodyType;
import com.github.ydoc.core.consts.Constans;
import com.github.ydoc.core.kv.Kv;
import com.github.ydoc.core.kv.KvFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Objects;

/**
 * @author nobugboy
 **/
public class PathVariableMiddleware extends AbstractMiddleware<PathVariable> {

    private static final String DEFAULT_DESC = "Path参数";

    @Override
    public void doHandle(List<Kv> target, Parameter parameter) {

	String paramDesc = StringUtils.hasText(getProxy().name()) ? getProxy().name() : DEFAULT_DESC;
	ParamDesc pd = AnnotationUtils.getAnnotation(parameter, ParamDesc.class);
	if (Objects.nonNull(pd)) {
	    paramDesc = pd.value();
	}
	target.add(KvFactory.get().lv3Params(getProxy().value(), Constans.In.PATH, getProxy().required(), paramDesc,
		RequestBodyType.of(Constans.Type.STRING).type));
    }

    @Override
    public int getOrder() {
	return 777;
    }
}
