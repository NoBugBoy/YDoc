# YDoc一款低侵入性、简洁并同时支持SwaggerUi和YApi的文档生成器

# 使用过程出现问题，可以提一个issues描述一下，看到后会更正。



使用Springboot 2.6.* + 的朋友需要在配置文件新增一条参数
```
spring.mvc.pathmatch.matching-strategy=ant_path_matcher
```

[final版本使用了新的ui库 ](https://github.com/NoBugBoy/YdocLuckyUi)

```xml 
       <dependency>
            <groupId>com.github.nobugboy</groupId>
            <artifactId>ydoc-spring-boot-starter</artifactId>
            <version>1.1.6.final</version>
        </dependency>
``` 

> 充分利用springboot自有注解来对文档进行描述的一款文档生成器。
> 让我们抛弃大量注解和配置一起拥抱简洁。

### [YDoc图文版使用教程，方便更好的理解和使用。](https://juejin.cn/post/6973962883918987295)

### [B站视频使用教程](https://www.bilibili.com/video/BV1S44y1B7Z3/)

# 引言

每当工程需要接口文档时，总会使用Swagger一类的restfulApi文档生成工具，相信很多人在使用时都会被它的一些注解和配置恶心到，如果工程对代码规范无要求还能作为注释来看，如果需要再写Javadoc注释就无形中增加了工作量，为了更加简单的使用我写了一款基于spring-boot-starter的依赖库，它基于YApi平台可以统一管理api文档和权限控制，mock数据等。

### 1. 对比其他文档生成器

这里举两种java平台常用的api文档生成器。

#### 1.JDoc

JDoc是根据文档注释去生成api文档的工具，需要遵循JDoc定义的文档规范进行书写才能生成对应的接口文档，不支持mock数据,权限管理等，不能集中管理Api，强制依赖于Javadoc规范实现，静态页面部署不方便，对离线支持好。

- 优点是只需要写Javadoc即可无需引入额外配置。
- 缺点是强制依赖于Javadoc规范,对api测试不友好，生成需额外操作。

#### 2.Swagger2

Swagger是基于注解形式生成api文档的工具，对接口的参数，返回值等都需要加对应的注释才能生成对应的api文档，支持api测试，不支持mock数据,权限管理等，不能集中管理Api，不方便离线，随程序部署而部署。

- 优点是操作简单，接口生成的比较好，随程序部署而部署使用方便。
- 缺点是集成麻烦，依赖大量注解，增加开发成本。

#### 3.YDoc

Ydoc是一款基于spring-boot-starter的依赖库，轻量级，不依赖注释，少量依赖注解和使用规范，整体依赖于YApi接口可视化平台，可使用YApi的所有特征，与程序部署分离可集中化管理，支持权限控制等，集成快速高效，配置依赖，加上两行配置程序启动时即可自动生成。

- 优点是可使用Swagger和YApi平台的特性，侵入性更低，快速集成，引入依赖配置参数即可，不需要额外配置类插拔更方便。
- 缺点是少量依赖注解和使用规范，仅支持spring-boot工程

### 2. YDoc的使用方法（基于YApi版本1.9.2）

1. 需搭建YApi文档平台（[YApi官方文档](https://hellosean1025.github.io/yapi/devops/index.html)）
2. 项目中引用ydoc-spring-boot-starter依赖
3. 配置 ydoc.enable = true
4. 如果想要使用YApi，需在配置文件中配置YApi的url，以及项目的token
5. 如果想要使用Swagger-bootstrap-ui 需加@EnableSwagger2，访问/doc.html即可使用
6. 都想要？以上都配置即可。
7. 额外配置？扫包？不存在的，启动即可使用

如果不能直接依赖说明还没有同步到中央maven（1.0.2才支持Swagger-bootstrap-ui ）

### YDoc版本更新说明

1. 1.0.1支持YDoc方式生成YApi文档
2. 1.0.2支持YDoc方式生成Swagger文档
3. 1.0.3支持Swagger原生注解生成Swagger文档,并同步导入到YApi（**方便已经使用了swagger原生注解，但是还想快速导入YApi的用户，拒绝使用YApi轮训导入从我做起**）
4. 1.0.4支持自动化测试，钉钉提醒和微服务
5. 1.0.5支持非native模式生成swagger文档，邮件提醒，web测试报告展示
6. 1.0.6修复bug
7. 1.0.7修复匿名内部类解析问题 参考 https://juejin.cn/post/6994640102974554143
8. 1.0.8修复issue问题 https://github.com/NoBugBoy/YDoc/issues/8 tag： https://github.com/NoBugBoy/YDoc/releases/tag/1.0.8
9. 1.0.9修复url被覆盖问题，缩小banner
10. 1.1.0修复返回值泛型被覆盖的问题 https://github.com/NoBugBoy/YDoc/issues/12
11. 1.1.1修复返回值R<List<T>>解析失败的问题&深层嵌套解析内部属性失败&修复返回值List<T>解析错误
12. 1.1.2新增通用公共headers配置，和解析@RequestHeader注解
13. 1.1.3修复不配置headers会空指针问题，和参数必填显示为false等问题
14. 1.1.4修复requestbody对象内参数使用ParamDesc的required修饰时没有正确的显示在文档上
15. 1.1.5修复代理类无法被正常生成api的问题&代码重构
16. 1.1.6修复1.1.5的bug
17. 1.1.7修复简单返回值无法显示问题，支持map、list优化部分逻辑

```xml 
       <dependency>
            <groupId>com.github.nobugboy</groupId>
            <artifactId>ydoc-spring-boot-starter</artifactId>
            <version>1.1.7</version>
        </dependency>
```

final版本是对应正式版本并使用了新的ui库(luck-ui) 建议尝试使用该版本
https://github.com/NoBugBoy/YdocLuckyUi

```xml 
       <dependency>
            <groupId>com.github.nobugboy</groupId>
            <artifactId>ydoc-spring-boot-starter</artifactId>
            <version>1.1.6.final</version>
        </dependency>
```       

| 配置名 | 值 | 是否必须| 
|--|--| -- |
| ydoc.token | YApi项目中生成的token |否|
| ydoc.host | YApi的url,例http://localhost:3000 |否|
| ydoc.headers | 配置所有api公共header参数（多个用,分割）|否| 
|ydoc.enable | 程序启动时是否同步到YApi平台 |是|
| ydoc.print | 程序启动时是否打印离线JSON（可手动导入到YApi或其他文档平台） |否| 
| ydoc.swagger-native |是否启用Swagger原生配置生成文档（方便已经集成了swagger得用户） |否|
| ydoc.cloud |是否开启微服务模式 |否| 
| ydoc.autoTest | 是否开启自动化测试 |否|
| ydoc.test.name| 自动化测试集合名称,可以多个(自动化测试时使用) |否|
| ydoc.id |yapi项目id,在设置中查看(自动化测试时使用) |否| 
| ydoc.yapi.user.email | yapi登录邮箱(自动化测试时使用) |否| 
| ydoc.yapi.user.password | yapi登录邮箱密码(自动化测试时使用) |否|
| ydoc.accessToken | 钉钉机器人token(自动化测试时使用) |否| 
| ydoc.email.host |邮件服务器(自动化测试时使用) |否|
| ydoc.email.password| 邮箱pop3,smtp密码(自动化测试时使用) |否|
| ydoc.email.password | 邮箱pop3,smtp密码(自动化测试时使用)|否|

注意当开启了swagger-native，需要将原工程的swagger依赖删除即可。（YDoc内部使用swagger3.0）

### 3. YApi的使用步骤（导入YApi，YDoc方式或者原生swagger都支持）

1. 在搭建好的YApi平台上创建好对应工程的项目
2. 点开项目，设置-token配置，复制好token粘贴到Java工程的对应配置上
3. 配置ydoc.enable = true，yapi-host,yapi-token
4. 启动应用即可
5. 回到YApi页面，文档生产完毕

### 4. Swaager3的使用步骤（YDoc生成方式）

1. 开启@EnableSwagger2
2. 使用YDoc注解生成方式配置项目
3. 配置ydoc.enable = true
4. 启动应用访问 /doc.html

### 5. 原生Swaager的使用（方便已经使用原生swagger注解的项目）

1. 和普通swagger一样配置即可，需要配置扫包和swagger注解等
2. 配置ydoc.enable = true，swagger-native=true
3. 启动应用访问 /doc.html

### 6. 自动化测试的使用

1. 配置测试集合，并定义名称（英文）
2. 配置ydoc.autoTest=true,ydoc.id,ydoc.test.name
3. 启动应用自动开始测试（异步）
4. 测试结束发送报告，参考下面的文章链接

（[1.0.4支持微服务模式，自动化测试，推送钉钉报告](https://juejin.cn/post/6976538974969921543)
（[1.0.5非原生模式也支持swagger页面，邮件推送web页面展示](https://juejin.cn/post/6977577714563678221)

### 5. YDoc方式生成描述

抛弃大量Swagger注解和配置，仅需在SpringBootWeb开发时必须注解上加额外参数对api或参数进行描述，大大减少了对工程的侵入性，简化开发流程

| 注解 | 值 |
|--|--| 
| @RestController | 注解中value描述controller的作用，默认为controller名 |
| @RequestMapping|注解中name描述该api的作用，在类上加跟路径时不需要name |
| @PathVariable|注解中name描述该参数的作用 | 
| @GetMapping | 注解中name描述该api的作用 |
| @PostMapping |注解中name描述该api的作用 | 
| @DeleteMapping | 注解中name描述该api的作用 | 
| @PutMapping | 注解中name描述该api的作用 |
|@RequestHeader | 注解中name描述该api的作用 |

@ParamDesc与@ParamIgnore为YDoc额外的自有注解，其余为SpringBoot的注解，@ParamDesc用来标识实体中参数的描述，@ParamIgnore用来忽略参数不参与生成文档，如不加参数描述则默认为参数的名称。
| 注解 | 值 | 
|--|--| 
| @ParamDesc| 注解中value描述该参数的描述，required是否必须 | 
| @ParamIgnore| 忽略参数 |

### 6. Enum类型

对于Swagger中比较头疼的Enum类型参数描述做了一些优化处理，如果enum类型值，需要在enum中重写toString方法,返回一个字符串作为描述参数，该方式自由度较高，对于code和message可以很好地进行描述。

### 7.使用YDoc规范建议

1. 无论是什么请求，如果使用对象接收对象内的基本类型建议使用包装类
2. 建议在参数上加@ParamDesc用来描述作用
3. spring提供的注解再原有开发习惯上加name，用来描述RestController或Api的作用
4. 如有特殊参数例如Bindingresult，登录的用户实体等使用@ParamIgnore进行忽略
5. 最好不要对象套对象自己比如U -> List <U> 这种

### 7.话外音

[详细步骤请参考YDoc使用图文教程](https://juejin.cn/post/6973962883918987295)




