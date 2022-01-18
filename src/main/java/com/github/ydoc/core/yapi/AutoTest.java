package com.github.ydoc.core.yapi;

import lombok.Data;

import java.util.List;

/**
 * @author nobugboy
 **/
@Data
public class AutoTest {
    private AutoTest.Message message;
    private String runTime;
    private List<Source> list;

    @Data
    public static class Message {
	private String msg;
	private Integer successNum;
	private Integer failedNum;
    }

    @Data
    public static class Source {
	private String name;
	private String path;
	private String method;
    }
}
