package com.github.ydoc.swagger;

import lombok.Data;

import java.util.List;

/**
 * author yujian
 * description
 * create 2021-06-22 13:46
 **/
@Data
public class AutoTest {
    private AutoTest.Message message;
    private String       runTime;
    private List<Source> list;
    @Data
    public static class Message{
        private String msg;
        private Integer successNum;
        private Integer failedNum;
    }
    @Data
    public static class Source{
        private String name;
        private String path;
        private String method;
    }
}
