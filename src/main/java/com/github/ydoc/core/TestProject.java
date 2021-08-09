package com.github.ydoc.core;

import lombok.Data;

import java.util.List;

/**
 * author NoBugBoY description create 2021-06-22 13:46
 **/
@Data
public class TestProject {
    private List<TestId> data;

    @Data
    public static class TestId {
	private String name;
	private Integer _id;
    }

}
