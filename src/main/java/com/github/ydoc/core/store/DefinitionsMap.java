package com.github.ydoc.core.store;

import com.github.ydoc.core.consts.Constans;
import com.github.ydoc.core.kv.Kv;
import com.google.common.base.Strings;

import java.lang.reflect.Type;

/**
 * definition ref store
 * 
 * @author nobugboy
 **/
public class DefinitionsMap extends Kv {
    private static final String SWAGGER_JSON_KEY = "swagger_json";
    private static final DefinitionsMap DM;
    static {
	DM = new DefinitionsMap();
    }

    public DefinitionsMap() {
	super();
    }

    public static DefinitionsMap get() {
	return DM;
    }

    public void putIfAbsent(String key, Kv value) {
	if (key.contains(Constans.Other.DOLLAR)) {
	    key = key.substring(key.lastIndexOf(Constans.Other.DOT) + 1).replace(Constans.Other.DOLLAR,
		    Constans.Other.DOT);
	}
	put(key, value);
    }

    public void putAnonymous(String key, String simpleKey, Kv value, Type type, Kv schema) {
	if (key.contains(Constans.Other.DOLLAR)) {
	    String str = key.substring(key.lastIndexOf(Constans.Other.DOT) + 1);
	    if (type != null) {
		put(str.replace(Constans.Other.DOLLAR, Constans.Other.DOT) + Constans.Other.LEFT + type.getTypeName()
			+ Constans.Other.RIGHT, value);
		schema.put(Constans.Other.REF,
			Constans.Other.DEFINE + str.replace(Constans.Other.DOLLAR, Constans.Other.DOT)
				+ Constans.Other.LEFT + type.getTypeName() + Constans.Other.RIGHT);
	    } else {
		put(str.replace(Constans.Other.DOLLAR, Constans.Other.DOT), value);
	    }

	} else {
	    if (type != null) {
		put(simpleKey + Constans.Other.LEFT + type.getTypeName() + Constans.Other.RIGHT, value);
		schema.put(Constans.Other.REF, Constans.Other.DEFINE + simpleKey + Constans.Other.LEFT
			+ type.getTypeName() + Constans.Other.RIGHT);
	    } else {
		put(simpleKey, value);
	    }
	}
    }

    public void setSwaggerJson(String swaggerJson) {
	if (!Strings.isNullOrEmpty(swaggerJson)) {
	    put(SWAGGER_JSON_KEY, swaggerJson);
	}
    }

    public String getSwaggerJson() {
	return getString(SWAGGER_JSON_KEY);
    }
}
