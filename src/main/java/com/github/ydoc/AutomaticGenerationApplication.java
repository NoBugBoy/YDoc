package com.github.ydoc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * author yujian
 */
@SpringBootApplication
public class AutomaticGenerationApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutomaticGenerationApplication.class, args);
    }

}
