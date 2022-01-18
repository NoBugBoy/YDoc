package com.github.ydoc.core.handler.middleware;

import com.github.ydoc.core.DocApi;
import com.github.ydoc.core.handler.api.GetHandler;
import com.github.ydoc.core.strategy.IStrategy;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yujian
 **/
public class MiddlewareFactory {
    private static final Map<Class<?>, IStrategy<?, DocApi>> FINAL_MAP;

    static {
	FINAL_MAP = new HashMap<Class<?>, IStrategy<?, DocApi>>(4) {
	    {
		put(GetMapping.class, new GetHandler());
		put(PostMapping.class, new GetHandler());
		put(PutMapping.class, new GetHandler());
		put(DeleteMapping.class, new GetHandler());
	    }
	};
    }

    public static <T extends Annotation> IStrategy<T, DocApi> getHandler(Annotation annotation) {
	return (IStrategy<T, DocApi>) FINAL_MAP.get(annotation.getClass());
    }
}
