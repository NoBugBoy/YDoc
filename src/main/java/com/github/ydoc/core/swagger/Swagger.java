package com.github.ydoc.core.swagger;

import com.github.ydoc.core.Core;
import com.github.ydoc.core.kv.DocApi;
import com.github.ydoc.core.kv.Kv;
import com.github.ydoc.core.store.DefinitionsMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

/**
 * swagger information
 * 
 * @author nobugboy
 **/
@Getter
@Setter
public class Swagger {
    private String swagger;
    private boolean boost;
    private Info info;
    private String basePath;
    private List<Tag> tags;
    private List<String> schemes;
    private DocApi paths;
    private Kv definitions;

    @Getter
    @Setter
    public static class Info {
	private String title = "YDoc(同时支持SwaggerUi和YApi的一款RestfulApi文档生成器)";
	private String version = Core.getVersion();
	private String description = "YDoc生成的RestfulApi文档";
	private Author contact = new Author();
	private String termsOfService = "https://github.com/NoBugBoy/YDoc";
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Tag {
	private String name;
	private String description;
    }

    public static Swagger initialize() {
	Swagger swagger = new Swagger();
	swagger.setDefinitions(DefinitionsMap.get());
	swagger.setPaths(DocApi.DOC_API);
	// help gc 只保留最后的json string对象就可以了
	WeakReference<Swagger> weakReference = new WeakReference<>(swagger);
	swagger.schemes = Collections.singletonList("http");
	swagger.swagger = "2.0";
	swagger.info = new Info();
	return weakReference.get();
    }

    @Getter
    @Setter
    public static class Author {
	private String email = "daydaynotbug@163.com";
	private String url = "https://github.com/NoBugBoy/YDoc";
	private String name = "NoBugBoy";
    }
}
