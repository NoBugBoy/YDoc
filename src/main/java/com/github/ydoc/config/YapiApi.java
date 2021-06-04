package com.github.ydoc.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.ydoc.swagger.Factory;
import org.springframework.http.*;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;


/**
 * author yujian
 * description
 * create 2021-04-28 12:04
 **/
public class YapiApi {
    public static void importDoc(String token,String host,String json){
        if(!StringUtils.hasText(token)){
            System.err.println("YApi token is null");
            return;
        }
        if(!StringUtils.hasText(host)){
            System.err.println("YApi host is null");
            return;
        }
        if(!StringUtils.hasText(json)){
            System.err.println("json is null");
            return;
        }
        RestTemplate restTemplate = new RestTemplate();
        JSONObject   param        = Factory.get();
        param.put("type", "swagger");
        param.put("merge", "merge");
        param.put("token", token);
        param.put("json", json);
        HttpHeaders headers = new HttpHeaders();
        MediaType   type    = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        HttpEntity<JSONObject> formEntity = new HttpEntity<>(param,headers);
        ResponseEntity<String> forEntity = restTemplate.postForEntity(
            host + "/api/open/import_data", formEntity, String.class);
        if(forEntity.getStatusCode() != HttpStatus.OK){
            System.err.println("导入失败： " + JSON.toJSONString(forEntity));
        }
    }
}
