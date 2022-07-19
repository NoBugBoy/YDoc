package com.github.ydoc.core;

import java.lang.annotation.Annotation;
import java.util.*;

import com.github.ydoc.core.handler.api.*;
import com.github.ydoc.core.kv.DocApi;
import com.github.ydoc.core.strategy.AbstractStrategy;

/**
 * @author nobugboy
 **/

public class StrategySelector {
    private static final Map<String, AbstractStrategy<? extends Annotation, DocApi>> STRATEGY_MAP;
    static {
	STRATEGY_MAP = new HashMap<String, AbstractStrategy<? extends Annotation, DocApi>>() {
	    {
		put("GetMapping", new GetHandler());
		put("PutMapping", new PutHandler());
		put("PostMapping", new PostHandler());
		put("DeleteMapping", new DeleteHandler());
		put("RequestMapping", new RequestHandler());
	    }
	};
    }

    @SuppressWarnings("unchecked")
    public static void matchAndGenerateApi(DocApi docApi) {
	Annotation[] annotations = docApi.getMethod().getAnnotations();
	stop: for (Annotation annotation : annotations) {
	    String key = Core.proxyToTargetClassName(annotation);
	    AbstractStrategy docApiAbstractStrategy = STRATEGY_MAP.get(key);
	    docApiAbstractStrategy.setProxy(annotation);
	    docApiAbstractStrategy.generateApi(docApi);
	    break stop;
	}
    }

}
