package com.github.ydoc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.swagger2.web.Swagger2Controller;

/**
 * author yujian
 */
@SpringBootApplication(exclude = Swagger2Controller.class)
public class AutomaticGenerationApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutomaticGenerationApplication.class, args);
    }

}
