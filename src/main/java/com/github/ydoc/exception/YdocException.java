package com.github.ydoc.exception;

/**
 * author NoBugBoY description ydoc异常 create 2021-06-24 16:50
 **/
public class YdocException extends RuntimeException {
    private static final long serialVersionUID = -3371453484855642908L;

    public YdocException(String exception, Throwable throwable) {
	super(exception, throwable);
    }

    public YdocException(String exception) {
	super(exception);
    }
}
