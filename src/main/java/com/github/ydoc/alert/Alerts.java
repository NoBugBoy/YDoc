package com.github.ydoc.alert;

import com.alibaba.fastjson.JSONObject;
import com.github.ydoc.core.kv.Kv;
import com.github.ydoc.core.kv.KvFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.client.RestTemplate;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.List;

/**
 * YApi api auto test
 * 
 * @author nobugboy
 **/
@Slf4j
public class Alerts {
    private static final String DING_URL = "https://oapi.dingtalk.com/robot/send?access_token=";

    public static void hookDing(RestTemplate restTemplate, String title, String msg, String host, String accessToken) {
	Kv param = KvFactory.get().empty();
	Kv inner = KvFactory.get().empty();
	inner.put("title", "YDoc自动化测试[" + title + "]结果通知");
	inner.put("text", "通知:" + msg);
	inner.put("picUrl", "https://photo.16pic.com/00/65/09/16pic_6509905_b.png");
	inner.put("messageUrl", host);
	param.put("link", inner);
	param.put("msgtype", "link");
	HttpHeaders headers = new HttpHeaders();
	MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
	headers.setContentType(type);
	HttpEntity<JSONObject> formEntity = new HttpEntity<>(param, headers);
	restTemplate.postForEntity(DING_URL + accessToken, formEntity, String.class);
    }

    public static void htmlEmail(JavaMailSender mailSender, String html, String from, List<String> to) {
	try {
	    MimeMessage message = mailSender.createMimeMessage();
	    MimeMessageHelper minehelper = new MimeMessageHelper(message, true);
	    ;
	    minehelper.setSubject("YDoc自动化测试报告");
	    minehelper.setFrom(from);
	    String[] arr = new String[to.size()];
	    minehelper.setTo(to.toArray(arr));
	    minehelper.setText(html, true);
	    mailSender.send(message);
	} catch (MessagingException e) {
	    log.error("发送邮件出错{}", e.getMessage());
	    // pass
	}
    }
}
