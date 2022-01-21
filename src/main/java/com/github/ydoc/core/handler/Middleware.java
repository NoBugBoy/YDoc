package com.github.ydoc.core.handler;

import com.github.ydoc.core.AnnotationProxy;
import com.github.ydoc.core.kv.Kv;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.List;

/**
 * @author nobugboy
 **/
public interface Middleware<T extends Annotation> extends AnnotationProxy<T> {

    /**
     * handle
     * 
     * @param target    target
     * @param parameter java parameter
     */
    void doHandle(List<Kv> target, Parameter parameter);

    /**
     * 优先级权重 order
     * 
     * @return order
     */
    int getOrder();
}
