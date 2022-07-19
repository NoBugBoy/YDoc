package com.github.ydoc.core.handler.middleware;

import com.github.ydoc.core.handler.Middleware;

import java.lang.annotation.Annotation;

/**
 * @author nobugboy
 **/
public abstract class AbstractMiddleware<T extends Annotation> implements Middleware<T> {
    protected T proxy;

    @Override
    public void setProxy(T proxy) {
	this.proxy = proxy;
    }

    @Override
    public T getProxy() {
	return proxy;
    }

}
