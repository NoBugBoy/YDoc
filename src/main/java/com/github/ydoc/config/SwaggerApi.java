package com.github.ydoc.config;

import com.github.ydoc.core.Factory;
import com.github.ydoc.core.store.DefinitionsMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.swing.plaf.basic.DefaultMenuLayout;

/**
 * @author nobugboy
 **/
@RestController
public class SwaggerApi {
    @GetMapping("/swagger-json")
    public String swaggerJson() {
	return DefinitionsMap.get().getSwaggerJson();
    }

    @GetMapping("/test-page")
    public String tesPage() {
	return Factory.page;
    }
}
