package com.github.ydoc.core.consts;

/**
 * @author nobugboy
 **/
public interface Constans {

    interface In {
	String HEADER = "header";
	String QUERY = "query";
	String PATH = "path";
	String BODY = "body";
    }

    interface Type {
	String STRING = "string";
	String ARRAY = "array";
	String OBJECT = "object";
	String INTEGER = "integer";
    }

    interface Other {
	String DOLLAR = "$";
	String DOT = ".";
	String LEFT = "<";
	String RIGHT = ">";
	String REF = "$ref";
	String DEFINE = "#/definitions/";
    }
}
