package com.github.ydoc.config;

import com.github.ydoc.core.Factory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * author yujian
 * description
 * create 2021-06-01 18:25
 **/
@RestController
public class SwaggerApi {
    @GetMapping("/swagger-json")
    public String swaggerJson(){
        return Factory.json;
    }

    @GetMapping("/test-page")
    public String tesPage(){
        return Factory.page;
    }
}
