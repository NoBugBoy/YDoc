package com.github.ydoc.config;

import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

import java.util.Collections;
import java.util.List;

/**
 * @author yujian
 */
public class SwaggerResourcesConfig implements SwaggerResourcesProvider {
    
        @Override
        public List<SwaggerResource> get() {
            SwaggerResource swaggerResource = swaggerResource("/swagger-json");
            return  Collections.singletonList(swaggerResource);
        }

        private SwaggerResource swaggerResource( String location){

            SwaggerResource swaggerResource = new SwaggerResource();
            swaggerResource.setLocation(location);
            swaggerResource.setName("default");
            swaggerResource.setSwaggerVersion("2.0");
            return swaggerResource;
        }
}