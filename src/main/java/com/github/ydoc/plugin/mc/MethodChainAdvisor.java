package com.github.ydoc.plugin.mc;

import org.aopalliance.aop.Advice;
import org.jetbrains.annotations.NotNull;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.ComposablePointcut;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author nobugboy
 **/
public class MethodChainAdvisor extends AbstractPointcutAdvisor {
    private Advice advice;
    private Pointcut pointcut;
    private final Set<Class<? extends Annotation>> ANNO;

    public MethodChainAdvisor() {
	ANNO = new LinkedHashSet<>(3);
	ANNO.add(RestController.class);
	ANNO.add(Service.class);
	ANNO.add(Repository.class);

	this.pointcut = this.buildPointcut();
	this.advice = this.buildAdv();
    }

    protected Pointcut buildPointcut() {
	ComposablePointcut result = null;
	AnnotationMatchingPointcut mpc;
	for (Iterator var3 = ANNO.iterator(); var3.hasNext(); result = result.union(mpc)) {
	    Class<? extends Annotation> asyncAnnotationType = (Class) var3.next();
	    Pointcut cpc = new AnnotationMatchingPointcut(asyncAnnotationType, true);
	    mpc = new AnnotationMatchingPointcut(null, asyncAnnotationType, true);
	    if (result == null) {
		result = new ComposablePointcut(cpc);
	    } else {
		result.union(cpc);
	    }
	}

	return (Pointcut) (result != null ? result : Pointcut.TRUE);
    }

    protected Advice buildAdv() {
	return new MethodChainInterceptor();
    }

    @Override
    public @NotNull Pointcut getPointcut() {
	return pointcut;
    }

    @Override
    public @NotNull Advice getAdvice() {
	return advice;
    }
}
