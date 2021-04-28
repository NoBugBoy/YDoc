package com.github.ydoc.yapi;

import java.util.UUID;

/**
 * author yujian
 * description
 * create 2021-04-23 17:57
 **/
public class IdAuto {
    public static String get(){
        return UUID.randomUUID().toString().replaceAll("-","").substring(0,24);
    }
}
