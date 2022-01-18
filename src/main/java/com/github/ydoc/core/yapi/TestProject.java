package com.github.ydoc.core.yapi;

import lombok.Data;

import java.util.List;

/**
 * @author nobugboy
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
