package com.github.ydoc.core.kv;

import com.alibaba.fastjson.JSONObject;
import com.github.ydoc.core.consts.Constans;
import org.springframework.core.ParameterizedTypeReference;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * YDoc json object 扩展类型
 * 
 * @author nobugboy
 **/
public class Kv extends JSONObject implements Cloneable {
    public Kv(Map<String, Object> map) {
	super(map);
    }

    /**
     * use kv factory gen kv
     */
    public Kv() {
    }

    public void putReference(String value, String simpleName) {
	value = value.contains(Constans.Other.DOLLAR) ? value.substring(value.lastIndexOf(Constans.Other.DOT) + 1)
		.replace(Constans.Other.DOLLAR, Constans.Other.DOT) : simpleName;
	super.put(Constans.Other.REF, Constans.Other.DEFINE + value);
    }

    @Override
    public Kv clone() {
	return new Kv(
		super.getInnerMap() instanceof LinkedHashMap ? new LinkedHashMap<String, Object>(super.getInnerMap())
			: new HashMap<String, Object>(super.getInnerMap()));
    }
}
