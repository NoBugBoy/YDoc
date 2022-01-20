package com.github.ydoc.core;

import java.util.*;

import com.github.ydoc.core.handler.api.*;
import com.github.ydoc.core.strategy.IStrategy;
import com.google.common.base.Strings;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.*;

/**
 * @author nobugboy
 **/

public class RequestTypeMatchingSwagger {
    private static final IStrategy<GetMapping,DocApi> GET_HANDLER;
    private static final IStrategy<PostMapping,DocApi> POST_HANDLER;
    private static final IStrategy<PutMapping,DocApi> PUT_HANDLER;
    private static final IStrategy<DeleteMapping,DocApi> DELETE_HANDLER;
    private static final IStrategy<RequestMapping,DocApi> REQUEST_HANDLER;
    static {
	GET_HANDLER = new GetHandler();
	POST_HANDLER = new PostHandler();
	PUT_HANDLER = new PutHandler();
	DELETE_HANDLER = new DeleteHandler();
	REQUEST_HANDLER = new RequestHandler();
    }

    public static void matching(DocApi docApi) {
	GetMapping getMapping;
	PostMapping postMapping;
	DeleteMapping deleteMapping;
	PutMapping putMapping;
	RequestMapping requestMapping;
	if (Objects.nonNull(getMapping = AnnotationUtils.getAnnotation(docApi.getMethod(), GetMapping.class))) {
	    if (getMapping.value().length > 0) {
		addPath(getMapping.value()[0], docApi);
	    }
	    GET_HANDLER.generateApi(getMapping, docApi);
	} else if (Objects
		.nonNull(postMapping = AnnotationUtils.getAnnotation(docApi.getMethod(), PostMapping.class))) {
	    if (postMapping.value().length > 0) {
		addPath(postMapping.value()[0], docApi);
	    }
	    POST_HANDLER.generateApi(postMapping, docApi);
	} else if (Objects.nonNull(putMapping = AnnotationUtils.getAnnotation(docApi.getMethod(), PutMapping.class))) {
	    if (putMapping.value().length > 0) {
		addPath(putMapping.value()[0], docApi);
	    }
	    PUT_HANDLER.generateApi(putMapping, docApi);
	} else if (Objects
		.nonNull(deleteMapping = AnnotationUtils.getAnnotation(docApi.getMethod(), DeleteMapping.class))) {
	    if (deleteMapping.value().length > 0) {
		addPath(deleteMapping.value()[0], docApi);
	    }
	    DELETE_HANDLER.generateApi(deleteMapping, docApi);
	} else if (Objects
		.nonNull(requestMapping = AnnotationUtils.getAnnotation(docApi.getMethod(), RequestMapping.class))) {
		RequestMethod requestMethod = null;
		if (requestMapping.method().length > 0) {
			requestMethod = requestMapping.method()[0];
			docApi.setMethodName(requestMethod);
		}
		if (requestMapping.value().length > 0) {
			addPath(requestMapping.value()[0], docApi);
		}
		REQUEST_HANDLER.generateApi(requestMapping,docApi);
	    }
	}


    public static void addPath(String path, DocApi docApi) {
	if (!Strings.isNullOrEmpty(path)) {
	    // base拼接restfulApi的路径
	    path = Factory.pathFormat.apply(path);
	    docApi.setOutPath(docApi.getOutPath() + path);
	}
    }
}
