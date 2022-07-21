package com.github.ydoc.core.handler.middleware;

import com.github.ydoc.core.Core;
import com.github.ydoc.core.handler.Middleware;
import com.github.ydoc.core.kv.Kv;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 *
 * @author nobugboy
 **/
public class MiddlewareSelect {
    private static final Map<String, Middleware<? extends Annotation>> MIDDLEWARE_SELECTOR;
    private static final int FIRST = 0;
    static {
	MIDDLEWARE_SELECTOR = new HashMap<String, Middleware<? extends Annotation>>() {
	    {
		put("RequestParam", new RequestParamMiddleware());
		put("PathVariable", new PathVariableMiddleware());
		put("ParamDesc", new ParamsDescMiddleware());
		put("RequestHeader", new RequestHeaderMiddleware());
		put("RequestBody", new RequestBodyMiddleware());
		put("None", new NoneMiddleware());
	    }
	};
    }

    /**
     * adapter request handler , nonAnnotation ? pojo params
     * 
     * @param parameter java parameter
     * @param target    parameter kv
     */
    public static void selectAndRun(Parameter parameter, List<Kv> target) {
	Annotation[] annotations = parameter.getAnnotations();
	List<Middleware<? extends Annotation>> sortMiddleware = new LinkedList<>();
	for (Annotation annotation : annotations) {
	    Middleware middleware = MIDDLEWARE_SELECTOR.get(Core.proxyToTargetClassName(annotation));
	    middleware.setProxy(annotation);
	    sortMiddleware.add(middleware);
	}
	sortMiddleware.sort(Comparator.comparing(Middleware::getOrder));
	Collections.reverse(sortMiddleware);
	if (!sortMiddleware.isEmpty()) {
	    sortMiddleware.get(FIRST).doHandle(target, parameter);
	}

    }
}
