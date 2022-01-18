package com.github.ydoc.core.handler;

import com.github.ydoc.core.kv.Kv;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.List;

/**
 * @author nobugboy
 **/
public interface Middleware<T extends Annotation> {

    /**
     * handle
     * 
     * @param target     target
     * @param parameter  java parameter
     * @param annotation spring mvc annotation
     */
    void doHandle(List<Kv> target, Parameter parameter, T annotation);
}
