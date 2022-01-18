package com.github.ydoc.config;

import springfox.documentation.spi.DocumentationType;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

import java.util.Collections;
import java.util.List;

/**
 * @author nobugboy
 **/
public class SwaggerResourcesConfig implements SwaggerResourcesProvider {

    @Override
    public List<SwaggerResource> get() {
	SwaggerResource swaggerResource = swaggerResource("/swagger-json");
	return Collections.singletonList(swaggerResource);
    }

    private SwaggerResource swaggerResource(String location) {
	SwaggerResource swaggerResource = new SwaggerResource();
	swaggerResource.setLocation(location);
	swaggerResource.setName("default");
	swaggerResource.setSwaggerVersion(DocumentationType.SWAGGER_2.getVersion());
	return swaggerResource;
    }
}