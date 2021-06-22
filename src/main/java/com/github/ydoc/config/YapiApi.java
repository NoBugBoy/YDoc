package com.github.ydoc.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.ydoc.alert.Alerts;
import com.github.ydoc.swagger.AutoTest;
import com.github.ydoc.swagger.Factory;
import com.github.ydoc.swagger.TestProject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * author yujian
 * description
 * create 2021-04-28 12:04
 **/
@Slf4j
public class YapiApi {
    public void importDoc(boolean cloud,String token,String host,String json){
        if(!StringUtils.hasText(token)){
            log.error("YApi token is null");
            return;
        }
        if(!StringUtils.hasText(host)){
            log.error("YApi host is null");
            return;
        }
        if(!StringUtils.hasText(json)){
            log.error("json is null");
            return;
        }
        RestTemplate restTemplate = new RestTemplate();
        JSONObject   param        = Factory.get();
        param.put("type", "swagger");
        param.put("merge", cloud?"good":"merge");
        param.put("token", token);
        param.put("json", json);
        HttpHeaders headers = new HttpHeaders();
        MediaType   type    = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        HttpEntity<JSONObject> formEntity = new HttpEntity<>(param,headers);
        ResponseEntity<String> forEntity = restTemplate.postForEntity(
            host + "/api/open/import_data", formEntity, String.class);
        if(forEntity.getStatusCode() != HttpStatus.OK){
            log.error("导入失败： " + JSON.toJSONString(forEntity));
        }
    }
    @Async
    public void autoTest(String yapiUserEmail,String yapiUserPassword,String token,String host,String uid,String accessToken, List<String> testName){
        if(!StringUtils.hasText(token)){
            log.error("YApi token is null");
            return;
        }
        if(!StringUtils.hasText(host)){
            log.error("YApi host is null");
            return;
        }
        if(!StringUtils.hasText(uid)){
            log.error("object id is null");
            return;
        }
        RestTemplate restTemplate = new RestTemplate();
        JSONObject param0 = Factory.get();
        param0.put("email", yapiUserEmail);
        param0.put("password", yapiUserPassword);
        HttpHeaders headers = new HttpHeaders();
        MediaType   type    = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        headers.set("Cookie","grafana_session=cbb335231019f3ca0dd5e2d69f1361ae");
        HttpEntity<JSONObject> formEntity = new HttpEntity<>(param0,headers);
        ResponseEntity<HashMap> login = restTemplate.postForEntity(
            host + "/api/user/login", formEntity, HashMap.class);
        if(login.getStatusCodeValue() == 200){
            List<String> strings = login.getHeaders().get("Set-Cookie");
            if(strings.size()>0 && StringUtils.hasText(strings.get(0))){
                Map dataObj = (Map)login.getBody().get("data");
                Integer     userId    = (Integer)dataObj.get("uid");
                HttpHeaders getHeaders = new HttpHeaders();
                getHeaders.set("Cookie","grafana_session=cbb335231019f3ca0dd5e2d69f1361ae; " + strings.get(0).split(";")[0]+"; _yapi_uid="+userId);
                ResponseEntity<TestProject> entity = restTemplate.exchange(
                    host + "/api/col/list?project_id="+uid,HttpMethod.GET,new HttpEntity<>(getHeaders), TestProject.class);
                if(entity.getStatusCode() != HttpStatus.OK){
                    log.error("获取测试id失败： " + JSON.toJSONString(entity));
                    return;
                }
                if(entity.getBody()!=null && !CollectionUtils.isEmpty(entity.getBody().getData())){
                    Map<String, Object>   param        = new HashMap<>(2);
                    for (TestProject.TestId data : entity.getBody().getData()) {
                        if(testName.contains(data.getName())){
                            param.put("id", data.get_id());
                            param.put("token", token);
                            ResponseEntity<AutoTest> forEntity = restTemplate.getForEntity(
                                host + "api/open/run_auto_test?id={id}&token={token}&mode=json", AutoTest.class,param);
                            if(forEntity.getStatusCode() != HttpStatus.OK){
                                log.error("自动化测试执行失败： " + JSON.toJSONString(forEntity));
                            }
                            AutoTest body = forEntity.getBody();
                            if(body != null){
                                log.info("YDoc自动化测试"+data.getName()+"api如下:");
                                for (AutoTest.Source source : body.getList()) {
                                    log.info("api: [" + source.getName() +"] path: ["+ source.getPath() +"]");
                                }
                                if(body.getMessage().getFailedNum() == 0){
                                    log.info(body.getMessage().getMsg() + "消耗时间:" + body.getRunTime());
                                }else{
                                    log.warn(body.getMessage().getMsg() + "消耗时间:" + body.getRunTime());
                                }
                                if(StringUtils.hasText(accessToken)){
                                    Alerts.hookDing(data.getName(),body.getMessage().getMsg(),host,accessToken);
                                }

                            }
                        }
                    }
                }
            }
            }
        }

}
