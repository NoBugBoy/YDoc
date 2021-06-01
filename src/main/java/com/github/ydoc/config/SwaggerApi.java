package com.github.ydoc.config;

import com.github.ydoc.swagger.Factory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * author yujian
 * description
 * create 2021-06-01 18:25
 **/
@RestController
@RequestMapping("/swagger-json")
public class SwaggerApi {
    @GetMapping("")
    public String swaggerJson(){
        return Factory.json;
    }
}
