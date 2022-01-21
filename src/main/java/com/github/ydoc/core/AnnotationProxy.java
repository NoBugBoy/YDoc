package com.github.ydoc.core;

import java.lang.annotation.Annotation;

/**
 * 注解的实例是代理类型，需要做特殊处理
 * 
 * @author nobugboy
 */
public interface AnnotationProxy<T extends Annotation> {
    /**
     * set proxy
     * 
     * @param proxy any annotation
     */
    void setProxy(T proxy);

    /**
     * get (T) proxy
     * 
     * @return proxy
     */
    T getProxy();
}
