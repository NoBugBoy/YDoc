package com.github.ydoc.core.kv;

import java.util.List;

/**
 * 生成固定结构的Kv工厂
 * 
 * @author nobugboy
 **/
public interface DynamicJsonFactory {
    /**
     * empty kv
     * 
     * @return empty
     */
    Kv empty();

    /**
     * simple kv
     *
     * @param type type
     * @param desc desc
     * @return kv
     */
    Kv simple(String type, String desc);

    /**
     * body kv
     * 
     * @param name   name
     * @param in     in
     * @param scheme scheme
     * @return kv
     */
    Kv body(String name, String in, Kv scheme);

    /**
     * body scheme
     * 
     * @param title title
     * @param ref   ref
     * @return kv
     */
    Kv bodyScheme(String title, String ref);

    /**
     * ref kv
     * 
     * @param property property
     * @param type     type
     * @return kv
     */
    Kv innerRef(Kv property, String type);

    /**
     * title kv
     *
     * @param property property
     * @param title    title
     * @param type     type
     * @return kv
     */
    Kv titleKv(String title, Kv property, String type);

    /**
     * content kv
     * 
     * @param method      method
     * @param apiMethod   up kv
     * @param summary     summary
     * @param description desc
     * @param parameters  parameters kv list
     * @param tags        tag
     * @param consumer    consumer
     * @return content kv
     */
    Kv lv2Content(String method, Kv apiMethod, String summary, String description, List<Kv> parameters,
	    List<String> tags, String consumer);

    /**
     * params kv
     * 
     * @param name        key
     * @param in          parameter in where
     * @param required    require
     * @param description desc
     * @param type        parameter java type
     * @return parameter kv
     */
    Kv lv3Params(String name, String in, boolean required, String description, String type);

    /**
     * schema kv
     * 
     * @param content up content kv
     * @return schema kv
     */
    Kv lv3ResponseSchema(Kv content);

    /**
     * array item kv
     * 
     * @param type java type
     * @param ref  reference instance kv
     * @return array item kv
     */
    Kv lv3ArrayItem(String type, String ref);

    /**
     * list kv
     * 
     * @return list kv
     */
    List<Kv> lv2Parameters();
}
