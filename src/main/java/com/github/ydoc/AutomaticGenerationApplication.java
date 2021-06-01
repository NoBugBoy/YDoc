package com.github.ydoc;

import com.github.ydoc.config.SwaggerResourcesConfig;
import com.github.ydoc.swagger.Factory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import springfox.documentation.swagger.web.*;
import springfox.documentation.swagger2.web.Swagger2Controller;

import java.util.List;
import java.util.Optional;

/**
 * author yujian
 */
@SpringBootApplication(exclude = Swagger2Controller.class)
public class AutomaticGenerationApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutomaticGenerationApplication.class, args);
    }
    

    @Autowired(required = false)
    private SecurityConfiguration securityConfiguration;
    @Autowired(required = false)
    private UiConfiguration        uiConfiguration;
    @Autowired
    private SwaggerResourcesConfig swaggerResourcesConfig;
    @GetMapping("/configuration/security")
    public  ResponseEntity<SecurityConfiguration> security(){
        if(securityConfiguration!=null){
            return ResponseEntity.ok(securityConfiguration);
        }
        return ResponseEntity.ok(
            SecurityConfigurationBuilder.builder().build());
    }
    @GetMapping("/configuration/ui")
    public ResponseEntity<UiConfiguration> uiConfiguration() {
        return new ResponseEntity<>(
            Optional.ofNullable(uiConfiguration).orElse(UiConfigurationBuilder.builder().build()), HttpStatus.OK);
    }
    @GetMapping("/")
    public List<SwaggerResource> swaggerResources() {
        return swaggerResourcesConfig.get();
    }
    public SwaggerResource sr(String location){
        SwaggerResource swaggerResource = new SwaggerResource();
        swaggerResource.setLocation(location);
        swaggerResource.setSwaggerVersion("2.0");
        return swaggerResource;

    }

}
