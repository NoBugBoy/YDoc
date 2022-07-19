package com.github.ydoc.config;

import com.github.ydoc.core.Utils;
import com.github.ydoc.core.store.DefinitionsMap;
import com.github.ydoc.plugin.mc.McStore;
import com.github.ydoc.plugin.mc.MethodIgnore;
import org.springframework.boot.configurationprocessor.MetadataStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author nobugboy
 **/
@RestController

public class SwaggerApi {
    @GetMapping("/swagger-json")
    @MethodIgnore
    public String swaggerJson() {
	return DefinitionsMap.get().getSwaggerJson();
    }

    @GetMapping("/getMc")
    @MethodIgnore
    public McStore getMc() {
	return McStore.MC_STORE;
    }

    @GetMapping("/check-boost")
    @MethodIgnore
    public McStore checkBoost() {
	return McStore.MC_STORE;
    }

    @GetMapping("/test-page")
    @MethodIgnore
    public String tesPage() {
	return Utils.page;
    }
}
