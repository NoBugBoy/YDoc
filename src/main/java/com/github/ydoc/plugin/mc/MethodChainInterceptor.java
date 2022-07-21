package com.github.ydoc.plugin.mc;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author nobugboy
 **/
public class MethodChainInterceptor implements MethodInterceptor, Ordered {
    final ThreadLocal<Stack<MethodPoint>> threadLocal = new ThreadLocal<>();
    final ThreadLocal<AtomicInteger> count = new ThreadLocal<>();

    final Map<Integer, MethodPoint> map = new HashMap<>();

    public MethodChainInterceptor() {
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
	Class<?> targetClass = invocation.getThis() != null ? AopUtils.getTargetClass(invocation.getThis()) : null;
	Method specificMethod = ClassUtils.getMostSpecificMethod(invocation.getMethod(), targetClass);
	if (targetClass == null || targetClass.getName().contains("spring")
		|| specificMethod.isAnnotationPresent(MethodIgnore.class)) {
	    return invocation.proceed();
	}

	if (count.get() == null) {
	    count.set(new AtomicInteger());
	}

	final MethodPoint mp = this.initAndSet(specificMethod.getName(), targetClass.getName(),
		targetClass.getPackage().getName());
	Object proceed;
	try {
	    mp.setStartNum(count.get().incrementAndGet());
	    proceed = invocation.proceed();
	    mp.setEndNum(count.get().incrementAndGet());

	    mp.setEndTime(System.currentTimeMillis());
	} catch (Throwable e) {
	    mp.setEndTime(System.currentTimeMillis());
	    mp.setHasError(true);
	    mp.setSimpleErrorMessage(e.getMessage());
	    throw e;
	} finally {
	    if (mp.isFirst()) {
		System.out.println(targetClass.getName());
		print();
		buildTrace();
		threadLocal.remove();
		count.remove();
	    }
	}

	return proceed;

    }

    public synchronized MethodPoint initAndSet(String methodName, String className, String packageName) {
	Stack<MethodPoint> stack = threadLocal.get();
	MethodPoint mp;
	if (stack == null) {
	    stack = new Stack<>();
	    mp = new MethodPoint(methodName, className, packageName, true);
	    threadLocal.set(stack);
	} else {
	    mp = new MethodPoint(methodName, className, packageName, false);
	}
	stack.push(mp);
	return mp;
    }

    @Override
    public int getOrder() {
	return Ordered.HIGHEST_PRECEDENCE;
    }

    public synchronized void print() {
	Stack<MethodPoint> stack = threadLocal.get();
	if (stack != null) {
	    MethodPoint timeStruct = stack.elementAt(0);
	    if (timeStruct != null && timeStruct.getEndTime() != 0L) {
		String name = "";
		int maxCharLength = stack.stream().mapToInt(x -> x.getSimpleMethodName().length()).max().orElse(0);
		String header = "| %1$-" + maxCharLength + "s";
		System.out.printf(header + " | %2$-5s | %3$-5s | %4$-5s | \n", "method", "time", "startNum", "endNum");
		while (!stack.isEmpty()) {
		    MethodPoint pop = stack.pop();
		    map.put(pop.getStartNum(), pop);
		    name = pop.getSimpleMethodName();
		    System.out.printf(header + " | %2$-5s | %3$-5s | %4$-5s | \n", name,
			    pop.getEndTime() - pop.getStartTime(), pop.getStartNum(), pop.getEndNum());

		}

	    }
	}
    }

    public synchronized void buildTrace() {
	List<MethodPoint> collect = map.values().stream().sorted(Comparator.comparing(MethodPoint::getEndNum))
		.collect(Collectors.toList());
	final List<LinkedList<MethodPoint>> allList = new LinkedList<>();
	Iterator<MethodPoint> iterator = collect.iterator();
	while (iterator.hasNext()) {
	    int startNum = 1;
	    MethodPoint next = iterator.next();
	    LinkedList<MethodPoint> list = new LinkedList<>();
	    while (startNum < next.getEndNum()) {
		MethodPoint methodPoint = map.get(startNum);
		if (methodPoint != null) {
		    list.add(methodPoint);
		}
		startNum++;
	    }
	    allList.add(list);
	    map.remove(next.getStartNum());
	    iterator.remove();
	}
	McStore.MC_STORE.add(allList);
	map.clear();
    }
}
