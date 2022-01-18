package com.github.ydoc.core;

import java.util.*;

import com.github.ydoc.core.handler.api.DeleteHandler;
import com.github.ydoc.core.handler.api.GetHandler;
import com.github.ydoc.core.handler.api.PostHandler;
import com.github.ydoc.core.handler.api.PutHandler;
import com.google.common.base.Strings;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.*;

/**
 * @author nobugboy
 **/

public class RequestTypeMatchingSwagger {
    private static final GetHandler GET_HANDLER;
    private static final PostHandler POST_HANDLER;
    private static final PutHandler PUT_HANDLER;
    private static final DeleteHandler DELETE_HANDLER;
    static {
	GET_HANDLER = new GetHandler();
	POST_HANDLER = new PostHandler();
	PUT_HANDLER = new PutHandler();
	DELETE_HANDLER = new DeleteHandler();
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
	    }
	    if (requestMapping.value().length > 0) {
		addPath(requestMapping.value()[0], docApi);
	    }
	    if (requestMethod != null) {
		switch (requestMethod) {
		case GET:
		    GET_HANDLER.generateApi((GetMapping) requestMapping, docApi);
		    return;
		case PUT:
		    PUT_HANDLER.generateApi((PutMapping) requestMapping, docApi);
		    return;
		case POST:
		    POST_HANDLER.generateApi((PostMapping) requestMapping, docApi);
		    return;
		case DELETE:
		    DELETE_HANDLER.generateApi((DeleteMapping) requestMapping, docApi);
		    return;
		default:
		}
	    }
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
