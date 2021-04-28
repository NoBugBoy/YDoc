package com.github.ydoc.yapi;

import lombok.Getter;

/**
 * @author yujian
 */

public enum Required {
    /**
     * 必须
     */
    TRUE("1"),
    /**
     * 非必须
     */
    FALSE("0");
    Required(String code){
        this.code = code;
    }
    @Getter
    private String code;
}
