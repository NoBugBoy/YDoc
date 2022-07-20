package com.github.ydoc.core.handler.middleware;

import com.github.ydoc.anno.ParamIgnore;
import com.github.ydoc.core.Utils;
import com.github.ydoc.core.consts.Constans;
import com.github.ydoc.core.Core;
import com.github.ydoc.core.kv.Kv;
import com.github.ydoc.core.kv.KvFactory;
import com.github.ydoc.core.store.DefinitionsMap;
import org.springframework.core.ResolvableType;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author nobugboy
 **/
public class RequestBodyMiddleware extends AbstractMiddleware<RequestBody> {
    @Override
    public void doHandle(List<Kv> target, Parameter parameter) {
	if (parameter.isAnnotationPresent(ParamIgnore.class)) {
	    return;
	}
	Class<?> type = parameter.getType();
	Kv schema = KvFactory.get().bodyScheme(type.getSimpleName(), type.getSimpleName());
	Kv api = KvFactory.get().body(type.getSimpleName(), Constans.In.BODY, schema);
	Kv properties = KvFactory.get().empty();
	Kv clone = (Kv) schema.clone();
	clone.put("type", Constans.Type.OBJECT);
	clone.remove(Constans.Other.REF);
	if (Map.class.isAssignableFrom(type)) {
	    // noop
	    api.put(Constans.Key.DESCRIPTION, Utils.kv(""));
	    api.put("type", Constans.Type.OBJECT);
	    api.put("schema", KvFactory.get().titleKv("map", properties, Constans.Type.OBJECT));
	} else if (Collection.class.isAssignableFrom(type)) {
	    // 集合
	    api.put("type", Constans.Type.ARRAY);
	    Type parameterizedType = parameter.getParameterizedType();
	    Core.collectionProcess(properties, parameterizedType, "");
	    api.put("schema", properties);
	} else {
	    Field[] declaredFields = Core.getAllFiled(type);
	    DefinitionsMap.get().put(type.getSimpleName(), clone);
	    for (Field declaredField : declaredFields) {
		if (declaredField.isAnnotationPresent(ParamIgnore.class)) {
		    continue;
		}
		properties.put(declaredField.getName(), Core.deepObject(KvFactory.get().empty(), declaredField));
	    }
	    clone.put("properties", properties);
	}

	Core.bodyRequired(clone, properties);
	target.add(api);
    }

    @Override
    public int getOrder() {
	return 999;
    }
}
