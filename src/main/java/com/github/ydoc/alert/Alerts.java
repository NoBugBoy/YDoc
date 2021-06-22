package com.github.ydoc.alert;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.ydoc.swagger.Factory;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

/**
 * author yujian
 * description
 * create 2021-06-22 14:35
 **/
public class Alerts {
    private static final String DingUrl = "https://oapi.dingtalk.com/robot/send?access_token=";
    public static void hookDing(String title,String msg,String host,String accessToken){
        RestTemplate restTemplate = new RestTemplate();
        JSONObject   param        = Factory.get();
        JSONObject   inner        = Factory.get();
        inner.put("title","YDoc自动化测试["+title+"]结果通知");
        inner.put("text","通知:" + msg);
        inner.put("picUrl","https://photo.16pic.com/00/65/09/16pic_6509905_b.png");
        inner.put("messageUrl",host);
        param.put("link",inner);
        param.put("msgtype", "link");
        HttpHeaders headers = new HttpHeaders();
        MediaType   type    = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        HttpEntity<JSONObject> formEntity = new HttpEntity<>(param,headers);
        ResponseEntity<String> forEntity = restTemplate.postForEntity(
            DingUrl + accessToken, formEntity, String.class);
    }
}
