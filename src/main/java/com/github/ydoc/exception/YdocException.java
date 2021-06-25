package com.github.ydoc.exception;

/**
 * author yujian
 * description ydoc异常
 * create 2021-06-24 16:50
 **/
public class YdocException extends RuntimeException{
    public YdocException(String exception,Throwable throwable){
        super(exception,throwable);
    }
    public YdocException(String exception){
        super(exception);
    }
}
