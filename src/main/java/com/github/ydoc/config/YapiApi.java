package com.github.ydoc.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.ydoc.alert.Alerts;
import com.github.ydoc.core.kv.Kv;
import com.github.ydoc.core.kv.KvFactory;
import com.github.ydoc.exception.YdocException;
import com.github.ydoc.core.yapi.AutoTest;
import com.github.ydoc.core.Utils;
import com.github.ydoc.core.yapi.TestProject;
import com.github.ydoc.core.yapi.YapiAccess;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author nobugboy
 **/
@Slf4j
public class YapiApi {
    private final YDocPropertiesConfig config;
    private final RestTemplate restTemplate;
    private final JavaMailSender javaMailSender;

    public YapiApi(JavaMailSender javaMailSender, YDocPropertiesConfig config, RestTemplate template) {
	this.config = config;
	this.restTemplate = template;
	this.javaMailSender = javaMailSender;
    }

    public void importDoc(boolean cloud, String token, String host, String json) {
	checkBefore();
	if (!StringUtils.hasText(json)) {
	    log.error("json is null, import yapi error");
	    return;
	}
	Kv param = KvFactory.get().empty();
	param.put("type", "swagger");
	param.put("merge", cloud ? "good" : "merge");
	param.put("token", token);
	param.put("json", json);
	HttpHeaders headers = new HttpHeaders();
	MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
	headers.setContentType(type);
	HttpEntity<JSONObject> formEntity = new HttpEntity<>(param, headers);
	ResponseEntity<String> forEntity = restTemplate.postForEntity(host + "/api/open/import_data", formEntity,
		String.class);
	if (forEntity.getStatusCode() != HttpStatus.OK) {
	    log.error("导入失败： " + JSON.toJSONString(forEntity));
	}
    }

    /**
     * 目前没有机会调用多次，只有启动时会调用所有先不缓存
     * 
     * @return YapiAccess
     */
    public YapiAccess login() {
	Kv param0 = KvFactory.get().empty();
	param0.put("email", config.getYapiUserEmail());
	param0.put("password", config.getYapiUserPassword());
	HttpHeaders headers = new HttpHeaders();
	MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
	headers.setContentType(type);
	headers.set("Cookie", "grafana_session=cbb335231019f3ca0dd5e2d69f1361ae");
	HttpEntity<JSONObject> formEntity = new HttpEntity<>(param0, headers);
	ResponseEntity<HashMap> login = restTemplate.postForEntity(config.getHost() + "/api/user/login", formEntity,
		HashMap.class);
	if (login.getStatusCodeValue() != 200 || login.getBody() == null) {
	    throw new YdocException("请填写正确的YApi邮箱和密码。");
	}
	List<String> strings = login.getHeaders().get("Set-Cookie");
	if (strings.size() > 0 && StringUtils.hasText(strings.get(0))) {
	    Map dataObj = (Map) login.getBody().get("data");
	    Integer userId = (Integer) dataObj.get("uid");
	    YapiAccess yapiAccess = new YapiAccess();
	    yapiAccess.setToken("grafana_session=cbb335231019f3ca0dd5e2d69f1361ae; " + strings.get(0).split(";")[0]
		    + "; _yapi_uid=" + userId);
	    yapiAccess.setUid(userId);
	    return yapiAccess;
	}
	throw new YdocException("认证失败。");

    }

    public void checkBefore() {
	if (!StringUtils.hasText(config.getToken())) {
	    throw new YdocException("YApi token can not be null");
	}
	if (!StringUtils.hasText(config.getHost())) {
	    throw new YdocException("YApi host can not be null");
	}
    }

    @Async
    public void autoTest(YapiAccess access) {
	checkBefore();
	if (!StringUtils.hasText(config.getId())) {
	    throw new YdocException("object id can not be null");
	}
	if (CollectionUtils.isEmpty(config.getTestName())) {
	    throw new YdocException("testName can not be empty");
	}
	HttpHeaders getHeaders = new HttpHeaders();
	getHeaders.set("Cookie", access.getToken());
	ResponseEntity<TestProject> entity = restTemplate.exchange(
		config.getHost() + "/api/col/list?project_id=" + config.getId(), HttpMethod.GET,
		new HttpEntity<>(getHeaders), TestProject.class);
	if (entity.getStatusCode() != HttpStatus.OK) {
	    log.error("获取测试id失败： " + JSON.toJSONString(entity));
	    return;
	}
	if (entity.getBody() != null && !CollectionUtils.isEmpty(entity.getBody().getData())) {
	    Map<String, Object> param = new HashMap<>(2);
	    for (TestProject.TestId data : entity.getBody().getData()) {
		if (config.getTestName().contains(data.getName())) {
		    param.put("id", data.get_id());
		    param.put("token", config.getToken());
		    ResponseEntity<AutoTest> forEntity = restTemplate.getForEntity(
			    config.getHost() + "api/open/run_auto_test?id={id}&token={token}&mode=json", AutoTest.class,
			    param);
		    if (forEntity.getStatusCode() != HttpStatus.OK) {
			log.error("自动化测试执行失败： " + JSON.toJSONString(forEntity));
		    }
		    AutoTest body = forEntity.getBody();
		    if (body != null) {
			log.info("YDoc自动化测试[" + data.getName() + "]如下:");
			for (AutoTest.Source source : body.getList()) {
			    log.info("api: [" + source.getName() + "] path: [" + source.getPath() + "]");
			}
			if (body.getMessage().getFailedNum() == 0) {
			    log.info(body.getMessage().getMsg() + "消耗时间:" + body.getRunTime());
			} else {
			    log.warn(body.getMessage().getMsg() + "消耗时间:" + body.getRunTime());
			}
			if (StringUtils.hasText(config.getAccessToken())) {
			    Alerts.hookDing(restTemplate, data.getName(), body.getMessage().getMsg(), config.getHost(),
				    config.getAccessToken());
			}
		    }
		    if (javaMailSender != null) {
			ResponseEntity<String> html = restTemplate.getForEntity(
				config.getHost() + "api/open/run_auto_test?id={id}&token={token}&mode=html",
				String.class, param);
			if (!CollectionUtils.isEmpty(config.getToEmails())) {
			    String utf8 = html.getBody().replace("utf8", "utf-8");
			    int start = utf8.indexOf("<div class=\"m-header\">");
			    int end = utf8.indexOf("<div class=\"g-doc\">");
			    utf8 = utf8.subSequence(0, start) + utf8.substring(end);
			    Utils.page = utf8;
			    Alerts.htmlEmail(javaMailSender, utf8, config.getYapiUserEmail(), config.getToEmails());
			}
		    }
		}
	    }
	}
    }
}
