package com.github.ydoc.plugin.mc;

import lombok.Data;
import lombok.ToString;

/**
 * @author nobugboy
 **/
@Data
@ToString
public class MethodPoint {

    private long startTime = 0;
    private long endTime = 0;
    private String simpleMethodName;
    private String packageName;
    private String className;
    private transient int startNum = -1;
    private transient int endNum = -1;
    private boolean hasError = false;
    private boolean first;
    private String simpleErrorMessage;

    public MethodPoint(String methodName, String className, String packageName, boolean first) {
	this.startTime = System.currentTimeMillis();
	this.simpleMethodName = methodName;
	this.packageName = packageName;
	this.first = first;
	this.className = className;
    }

}
