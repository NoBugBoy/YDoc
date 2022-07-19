package com.github.ydoc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @author nobugboy
 **/
@Data
@ConfigurationProperties(prefix = "ydoc")
public class YDocPropertiesConfig {
    /**
     * yapi中创建项目的的token
     */
    private String token;

    /**
     * 开启ydoc增强功能
     */
    private boolean boost;
    /**
     * yapi服务url
     */
    private String host;
    /**
     * 所有接口通用header参数
     */
    private List<String> headers;
    /**
     * yapi项目id,在设置中查看(自动化测试时使用)
     */
    private String id;
    /**
     * yapi登录邮箱(自动化测试时使用,同时是发送者邮箱)
     */
    private String yapiUserEmail;
    /**
     * 邮箱的host(自动化测试时使用)
     */
    private String emailHost;
    /**
     * 邮箱pop3,smtp密码(自动化测试时使用)
     */
    private String emailPassword;
    /**
     * yapi密码(自动化测试时使用)
     */
    private String yapiUserPassword;
    /**
     * 自动化测试集合名称(自动化测试时使用)
     */
    private List<String> testName;
    /**
     * 邮箱报告接收人多个(自动化测试时使用)
     */
    private List<String> toEmails;
    /**
     * 钉钉机器人accessToken(自动化测试时使用)
     */
    private String accessToken;
    /**
     * 是否启动时开始导入更新api文档
     */
    private boolean enable = true;
    /**
     * 是否开启自动化测试
     */
    private boolean autoTest = false;
    /**
     * 是否是微服务模式
     */
    private boolean cloud = false;
    /**
     * 打印导入的json,方便离线导入
     */
    private boolean print = false;
    /**
     * 是否启用原生Swagger注解来生成api文档 需要配置swagger扫包等config,并且移除相关pom依赖 （YDoc默认为swagger3.0）
     */
    private boolean swaggerNative = false;

}
