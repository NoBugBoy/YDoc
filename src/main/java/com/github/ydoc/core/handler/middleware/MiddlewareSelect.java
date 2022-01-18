package com.github.ydoc.core.handler.middleware;

import com.github.ydoc.anno.None;
import com.github.ydoc.anno.ParamDesc;
import com.github.ydoc.core.handler.Middleware;
import com.github.ydoc.core.kv.Kv;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author nobugboy
 **/
public class MiddlewareSelect {
    private static final Middleware<RequestParam> REQUEST_PARAM_REQUEST_ANNO_HANDLER;
    private static final Middleware<PathVariable> PATH_VARIABLE_REQUEST_ANNO_HANDLER;
    private static final Middleware<ParamDesc> PARAM_DESC_REQUEST_ANNO_HANDLER;
    private static final Middleware<RequestHeader> REQUEST_HEADER_REQUEST_ANNO_HANDLER;
    private static final Middleware<RequestBody> REQUEST_BODY_REQUEST_ANNO_HANDLER;
    private static final Middleware<None> NONE_REQUEST_ANNO_HANDLER;
    static {
	REQUEST_PARAM_REQUEST_ANNO_HANDLER = new RequestParamMiddleware();
	PATH_VARIABLE_REQUEST_ANNO_HANDLER = new PathVariableMiddleware();
	PARAM_DESC_REQUEST_ANNO_HANDLER = new ParamsDescMiddleware();
	REQUEST_HEADER_REQUEST_ANNO_HANDLER = new RequestHeaderMiddleware();
	REQUEST_BODY_REQUEST_ANNO_HANDLER = new RequestBodyMiddleware();
	NONE_REQUEST_ANNO_HANDLER = new NoneAnnoMiddleware();
    }

    /**
     * adapter request handler
     * 
     * @param parameter java parameter
     * @param target    parameter kv
     * @return nonAnnotation ? pojo params
     */
    public static boolean select(Parameter parameter, List<Kv> target) {
	PathVariable pathVariable;
	ParamDesc queryDesc;
	RequestParam requestParam;
	RequestHeader requestHeader;
	RequestBody requestBody;
	if (Objects.nonNull(requestBody = AnnotationUtils.getAnnotation(parameter, RequestBody.class))) {
	    REQUEST_BODY_REQUEST_ANNO_HANDLER.doHandle(target, parameter, requestBody);
	} else if (Objects.nonNull(pathVariable = AnnotationUtils.getAnnotation(parameter, PathVariable.class))) {
	    PATH_VARIABLE_REQUEST_ANNO_HANDLER.doHandle(target, parameter, pathVariable);
	} else if (Objects.nonNull(requestParam = AnnotationUtils.getAnnotation(parameter, RequestParam.class))) {
	    REQUEST_PARAM_REQUEST_ANNO_HANDLER.doHandle(target, parameter, requestParam);
	} else if (Objects.nonNull(requestHeader = AnnotationUtils.getAnnotation(parameter, RequestHeader.class))) {
	    REQUEST_HEADER_REQUEST_ANNO_HANDLER.doHandle(target, parameter, requestHeader);
	} else if (Objects.nonNull(queryDesc = AnnotationUtils.getAnnotation(parameter, ParamDesc.class))) {
	    PARAM_DESC_REQUEST_ANNO_HANDLER.doHandle(target, parameter, queryDesc);
	} else {
	    NONE_REQUEST_ANNO_HANDLER.doHandle(target, parameter, null);
	}
	return true;
    }
}
