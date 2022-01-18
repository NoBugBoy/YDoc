package com.github.ydoc.core.kv;

import com.github.ydoc.core.consts.Constans;
import com.google.common.base.Strings;

import java.util.*;

/**
 * gen fixed format kv
 * 
 * @author nobugboy
 **/
public class KvFactory implements DynamicJsonFactory {
    private static final KvFactory JSON = new KvFactory();

    public static KvFactory get() {
	return JSON;
    }

    @Override
    public Kv empty() {
	return new Kv();
    }

    @Override
    public Kv simple(String type, String desc) {
	return new Kv() {
	    {
		put("type", type);
		put("desc", desc);
	    }
	};
    }

    @Override
    public Kv body(String name, String in, Kv schema) {
	return new Kv() {
	    {
		put("name", name);
		put("in", in);
		put("schema", schema);
	    }
	};
    }

    @Override
    public Kv bodyScheme(String title, String ref) {
	return new Kv() {
	    {
		put("title", title);
		put("$ref", Constans.Other.DEFINE + ref);
	    }
	};
    }

    @Override
    public Kv innerRef(Kv property, String type) {

	return new Kv() {
	    {
		put("type", type);
		put("properties", property);
	    }
	};
    }

    @Override
    public Kv titleKv(String title, Kv property, String type) {
	return new Kv() {
	    {
		put("type", type);
		put("title", title);
		put("properties", property);
	    }
	};
    }

    @Override
    public Kv lv2Content(String method, Kv apiMethod, String summary, String description, List<Kv> parameters,
	    List<String> tags, String consumes) {
	Kv content = new Kv() {
	    {
		put("summary", summary);
		put("description", description);
		put("parameters", parameters);
		put("tags", tags);
		if (!Strings.isNullOrEmpty(consumes)) {
		    put("consumes", Collections.singleton("application/json"));
		}
	    }
	};
	apiMethod.put(method, content);
	return content;
    }

    @Override
    public Kv lv3Params(String name, String in, boolean required, String description, String type) {
	return new Kv() {
	    {
		put("name", name);
		put("in", in);
		put("required", required);
		put("description", description);
		put("type", type);
	    }
	};
    }

    @Override
    public Kv lv3ResponseSchema(Kv content) {
	Kv schema = new Kv() {
	    {
		put("type", "object");
		put("title", "YDoc");
	    }
	};

	content.put("responses", new Kv() {
	    {
		put("200", new Kv() {
		    {
			put("description", "successful operation");
			put("schema", schema);
		    }
		});
	    }
	});
	return schema;
    }

    @Override
    public Kv lv3ArrayItem(String type, String ref) {
	return new Kv() {
	    {
		put("type", type);
		if (!Strings.isNullOrEmpty(ref)) {
		    put("$ref", ref);
		}
	    }
	};
    }

    @Override
    public List<Kv> lv2Parameters() {
	return new ArrayList<>();
    }
}
