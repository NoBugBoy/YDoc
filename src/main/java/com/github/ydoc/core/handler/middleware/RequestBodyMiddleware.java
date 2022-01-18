package com.github.ydoc.core.handler.middleware;

import com.github.ydoc.anno.ParamIgnore;
import com.github.ydoc.core.consts.Constans;
import com.github.ydoc.core.Core;
import com.github.ydoc.core.handler.Middleware;
import com.github.ydoc.core.kv.Kv;
import com.github.ydoc.core.kv.KvFactory;
import com.github.ydoc.core.store.DefinitionsMap;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.List;

/**
 * @author nobugboy
 **/
public class RequestBodyMiddleware implements Middleware<RequestBody> {
    @Override
    public void doHandle(List<Kv> target, Parameter parameter, RequestBody annotation) {
	Class<?> type = parameter.getType();
	Field[] declaredFields = Core.getAllFiled(type);
	Kv schema = KvFactory.get().bodyScheme(type.getSimpleName(), type.getSimpleName());
	Kv api = KvFactory.get().body(type.getSimpleName(), Constans.In.BODY, schema);
	Kv properties = KvFactory.get().empty();
	Kv clone = (Kv) schema.clone();
	clone.put("type", Constans.Type.OBJECT);
	clone.remove(Constans.Other.REF);
	DefinitionsMap.get().put(type.getSimpleName(), clone);
	for (Field declaredField : declaredFields) {
	    if (declaredField.isAnnotationPresent(ParamIgnore.class)) {
		continue;
	    }
	    properties.put(declaredField.getName(), Core.deepObject(KvFactory.get().empty(), declaredField));
	}

	clone.put("properties", properties);
	Core.bodyRequired(clone, properties);
	target.add(api);
    }
}
