package com.github.ydoc.core.handler.middleware;

import com.github.ydoc.anno.None;
import com.github.ydoc.anno.ParamDesc;
import com.github.ydoc.core.Utils;
import com.github.ydoc.core.consts.Constans;
import com.github.ydoc.core.Core;
import com.github.ydoc.core.kv.Kv;
import com.github.ydoc.core.kv.KvFactory;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Objects;

/**
 * empty annotation , but parameter is pojo
 * 
 * @author yujian
 **/
public class NoneMiddleware extends AbstractMiddleware<None> {

    @Override
    public void doHandle(List<Kv> target, Parameter parameter) {
	if (parameter.getDeclaredAnnotations().length > 1) {
	    return;
	}
	if (!Utils.isPrimitive(parameter.getParameterizedType().getClass())) {
	    for (Field field : Core.getAllFiled(parameter.getType())) {
		String paramDesc;
		boolean required;
		if (Modifier.isFinal(field.getModifiers())) {
		    continue;
		}
		ParamDesc pd;
		if (Objects.nonNull(pd = AnnotationUtils.getAnnotation(field, ParamDesc.class))) {
		    required = pd.required();
		    paramDesc = pd.value();
		} else {
		    required = Boolean.TRUE;
		    paramDesc = field.getName();
		}
		target.add(KvFactory.get().lv3Params(field.getName(), Constans.In.QUERY, required, paramDesc,
			Core.convertType(field.getType().getSimpleName())));
	    }
	}
    }

    @Override
    public int getOrder() {
	return 1;
    }

}
