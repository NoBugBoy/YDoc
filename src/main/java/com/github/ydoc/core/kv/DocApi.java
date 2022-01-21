package com.github.ydoc.core.kv;

import com.alibaba.fastjson.JSONObject;
import lombok.*;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.reflect.Method;
import java.util.List;

/**
 * json object 扩展类
 * 
 * @author nobugboy
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class DocApi extends JSONObject {
    private transient Method method;
    private transient RequestMethod methodName;
    private transient String outPath;
    private transient String tag;
    private transient List<String> headers;

    // 全局唯一即可
    public static final DocApi DOC_API = new DocApi();

    public DocApi update(Method method, String outPath, String tag) {
	DOC_API.setMethod(method);
	DOC_API.setOutPath(outPath);
	DOC_API.setTag(tag);
	return DOC_API;
    }
}
