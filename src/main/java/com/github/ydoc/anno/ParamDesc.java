package com.github.ydoc.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * author NoBugBoY description 参数描述 create 2021-04-23 17:17
 **/
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ParamDesc {
    String value() default "";

    boolean required() default false;
}
