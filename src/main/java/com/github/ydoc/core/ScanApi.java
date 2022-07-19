package com.github.ydoc.core;

import com.alibaba.fastjson.JSON;
import com.github.ydoc.config.YDocPropertiesConfig;
import com.github.ydoc.config.YapiApi;
import com.github.ydoc.core.consts.Constans;
import com.github.ydoc.core.kv.DocApi;
import com.github.ydoc.core.store.DefinitionsMap;
import com.github.ydoc.core.swagger.Swagger;
import com.github.ydoc.core.yapi.YapiAccess;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.bcel.Const;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.service.Documentation;
import springfox.documentation.spring.web.DocumentationCache;
import springfox.documentation.swagger2.mappers.ServiceModelToSwagger2Mapper;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * YDoc 扫描api入口类
 * 
 * @author nobugboy
 **/
@EnableConfigurationProperties(YDocPropertiesConfig.class)
@Slf4j
public class ScanApi implements ApplicationContextAware, EnvironmentAware, InitializingBean, CommandLineRunner {
    private final YapiApi yapiApi;

    public ScanApi(YapiApi yapiApi) {
	this.yapiApi = yapiApi;
    }

    @Autowired
    YDocPropertiesConfig propertiesConfig;
    @Autowired(required = false)
    DocumentationCache documentationCache;
    @Autowired(required = false)
    ServiceModelToSwagger2Mapper map;

    private ApplicationContext applicationContext;
    private Environment e;

    private static final List<String> IGNORES;

    static {
	IGNORES = new ArrayList<String>(3) {
	    {
		add("swaggerApi");
		add("swagger2ControllerWebMvc");
		add("apiResourceController");
	    }
	};
    }

    Supplier<String> basePath = () -> StringUtils.hasText(e.getProperty("server.servlet.context-path"))
	    ? e.getProperty("server.servlet.context-path")
	    : "/";

    /**
     * Generate the core entry of the API
     */
    public void scan() {
	Map<String, Object> restControllerMap = applicationContext.getBeansWithAnnotation(RestController.class);
	Swagger swagger = Swagger.initialize();
	swagger.setBoost(propertiesConfig.isBoost());
	swagger.setBasePath(basePath.get());
	List<Swagger.Tag> tags = new ArrayList<>();
	swagger.setTags(tags);
	DocApi paths = DocApi.DOC_API;
	// 配置固定headers
	paths.setHeaders(propertiesConfig.getHeaders());
	for (Map.Entry<String, Object> restApi : restControllerMap.entrySet()) {
	    // aopUtils.isAop ? false
	    Class<?> controllerClass = restApi.getValue().getClass();
	    if (isAopProxy(controllerClass)) {
		controllerClass = AopUtils.getTargetClass(restApi.getValue());
	    }

	    // 如果有外层路径需要加上
	    String outPath = buildBaseUrl(controllerClass);
	    if ("/swagger-json".equals(outPath) || outPath.contains(Constans.Other.DOLLAR)) {
		continue;
	    }
	    if (IGNORES.stream().anyMatch((key) -> restApi.getKey().equals(key))) {
		continue;
	    }
	    outPath = Utils.pathFormat.apply(outPath);
	    // controller分组
	    tags.add(new Swagger.Tag(restApi.getKey(), restApi.getKey()));
	    // 循环所有的restfulApi
	    Method[] restMethods = controllerClass.getDeclaredMethods();
	    for (Method method : restMethods) {
		// 生成api入口
		StrategySelector.matchAndGenerateApi(paths.update(method, outPath, restApi.getKey()));
	    }
	}
	DefinitionsMap.get().setSwaggerJson(JSON.toJSONString(swagger));

	if (propertiesConfig.isPrint()) {
	    print();
	}
	if (enableImport()) {
	    importToYapi();
	}

    }

    @Override
    public void run(String... args) throws Exception {
	if (propertiesConfig.isSwaggerNative() && documentationCache != null && map != null) {
	    // 这里暂时只考虑一个group的时候，因为大多数情况都是只有一个default
	    printBanner();
	    if (!documentationCache.all().values().isEmpty()) {
		Documentation documentation = new ArrayList<>(documentationCache.all().values()).get(0);
		io.swagger.models.Swagger swagger = this.map.mapDocumentation(documentation);
		DefinitionsMap.get().setSwaggerJson(JSON.toJSONString(swagger));
		if (propertiesConfig.isPrint()) {
		    print();
		}
		if (enableImport()) {
		    importToYapi();
		}
	    } else {
		log.warn("未发现任何Api,可能未配置Swagger2 Config....");
	    }
	}
	// access调用链
	if (access()) {
	    YapiAccess login = yapiApi.login();
	    // 异步执行即可
	    if (propertiesConfig.isAutoTest()) {
		yapiApi.autoTest(login);
	    }
	}
	// no access

    }

    /**
     * is aop proxy class ?
     *
     * @see org.springframework.aop.support.AopUtils
     * @param clazz proxy class
     * @return boolean
     */
    public boolean isAopProxy(Class<?> clazz) {
	return clazz.getName().contains("$$");
    }

    /**
     * print banner
     */
    public void print() {
	log.info(DefinitionsMap.get().getSwaggerJson());
    }

    public boolean enableImport() {
	return StringUtils.hasText(propertiesConfig.getHost()) && StringUtils.hasText(propertiesConfig.getToken());
    }

    public synchronized void importToYapi() {
	yapiApi.importDoc(propertiesConfig.isCloud(), propertiesConfig.getToken(), propertiesConfig.getHost(),
		DefinitionsMap.get().getSwaggerJson());
	Utils.definitions.clear();
    }

    public String buildBaseUrl(Class<?> aClass) {
	String basePath = "";
	if (aClass.isAnnotationPresent(RequestMapping.class)) {
	    RequestMapping annotation = aClass.getAnnotation(RequestMapping.class);
	    if (annotation.value().length > 0) {
		basePath = annotation.value()[0];
	    }
	}
	return basePath;
    }

    @Override
    public void setEnvironment(Environment environment) {
	this.e = environment;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
	this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
	if (propertiesConfig.isEnable()) {
	    if (!propertiesConfig.isSwaggerNative()) {
		printBanner();
		scan();
		log.info(" >>> YDoc Sync Api Successful !<<<");
	    }
	}
    }

    /**
     * 某些方法必须login才ok
     */
    private boolean access() {
	return StringUtils.hasText(propertiesConfig.getYapiUserEmail())
		&& StringUtils.hasText(propertiesConfig.getYapiUserPassword());
    }

    private void printBanner() {
	System.out.println(" _  _  ____  _____  ___ ");
	System.out.println("( \\/ )(  _ \\(  _  )/ __)");
	System.out.println(" \\  /  )(_) ))(_)(( (__ ");
	System.out.println(" (__) (____/(_____)\\___)");
	System.out.println("                v" + Core.getVersion() + "   ");

    }
}
