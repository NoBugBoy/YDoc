package com.github.ydoc.core;

import com.alibaba.fastjson.JSONObject;
import lombok.*;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author nobugboy
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class DocApi extends JSONObject {
    private transient Method method;
    private transient String outPath;
    private transient String tag;
    private transient List<String> headers;
    public static final DocApi DOC_API = new DocApi();

    public DocApi update(Method method, String outPath, String tag) {
	DOC_API.setMethod(method);
	DOC_API.setOutPath(outPath);
	DOC_API.setTag(tag);
	return DOC_API;
    }
}
