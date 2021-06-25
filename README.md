# YDoc一款低侵入性、简洁并同时支持SwaggerUi和YApi的文档生成器

> 充分利用springboot自有注解来对文档进行描述的一款文档生成器。
> 让我们抛弃大量注解和配置一起拥抱简洁。

### [YDoc图文版使用教程，方便更好的理解和使用。](https://blog.csdn.net/Day_Day_No_Bug/article/details/117512788?spm=1001.2014.3001.5501)
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
Ydoc是一款基于spring-boot-starter的依赖库，轻量级，无Ui界面，不依赖注释，少量依赖注解和使用规范，整体依赖于YApi接口可视化平台，可使用YApi的所有特征，与程序部署分离可集中化管理，支持权限控制等，集成快速高效，配置依赖，加上两行配置程序启动时即可自动生成。

- 优点是可使用YApi平台的特性，侵入性更低，0配置，引入依赖添加配置文件即可。
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
### 版本说明
1.0.1支持YDoc方式生成YApi文档
1.0.2支持YDoc方式生成Swagger文档
1.0.3支持Swagger原生注解生成Swagger文档,并同步导入到YApi（**方便已经使用了swagger原生注解，但是还想快速导入YApi的用户，拒绝使用YApi轮训导入从我做起**）
1.0.4支持自动化测试和微服务
```xml
       <dependency>
            <groupId>com.github.nobugboy</groupId>
            <artifactId>ydoc-spring-boot-starter</artifactId>
            <version>1.0.4</version>
        </dependency>
```

| 配置名 | 值 | 是否必须|
|--|--| -- |
| ydoc.token | YApi项目中生成的token |否|
| ydoc.host | YApi的url,例http://localhost:3000 |否|
| ydoc.enable | 程序启动时是否同步到YApi平台 |是|
| ydoc.print | 程序启动时是否打印离线JSON（可手动导入到YApi或其他文档平台） |否|
| ydoc.swagger-native | 是否启用Swagger原生配置生成文档（方便已经集成了swagger得用户） |否|

注意当开启了swagger-native，需要将原工程的swagger依赖删除即可。（YDoc内部使用swagger3.0）

### 3. YApi的使用步骤
1. 在搭建好的YApi平台上创建好对应工程的项目
2. 点开项目，设置-token配置，复制好token粘贴到Java工程的对应配置上
3. 启动应用即可
4. 回到YApi对应的项目下，此时文档已经生成


### 4. 参数描述
抛弃大量Swagger注解和配置，仅需在SpringBootWeb开发时必须注解上加额外参数对api或参数进行描述，大大减少了对工程的侵入性，简化开发流程


| 注解 | 值 |
|--|--|
| @RestController | 注解中value描述controller的作用，默认为controller名 |
| @RequestMapping |注解中name描述该api的作用，在类上加跟路径时不需要name |
| @RequestParam |注解中name描述该参数的作用 |
|@PathVariable|注解中name描述该参数的作用 |
| @GetMapping | 注解中name描述该api的作用 |
| @PostMapping | 注解中name描述该api的作用 |
| @DeleteMapping | 注解中name描述该api的作用 |
| @PutMapping | 注解中name描述该api的作用 |

@ParamDesc与@ParamIgnore为YDoc额外的自有注解，其余为SpringBoot的注解，@ParamDesc用来标识实体中参数的描述，@ParamIgnore用来忽略参数不参与生成文档，如不加参数描述则默认为参数的名称。
| 注解 | 值 |
|--|--|
| @ParamDesc| 注解中value描述该参数的描述，required是否必须 |
| @ParamIgnore| 忽略参数 |

### 5. Enum类型
对于Swagger中比较头疼的Enum类型参数描述做了一些优化处理，如果enum类型值，需要在enum中重写toString方法,返回一个字符串作为描述参数，该方式自由度较高，对于code和message可以很好地进行描述。


### 6.使用YDoc规范建议
1. 无论是什么请求，如果使用对象接收对象内的基本类型建议使用包装类
2. 建议在参数上加@ParamDesc用来描述作用
3. spring提供的注解再原有开发习惯上加name，用来描述RestController或Api的作用
4. 如有特殊参数例如Bindingresult，登录的用户实体等使用@ParamIgnore进行忽略


### 7.话外音
[详细步骤请参考YDoc使用图文教程](https://blog.csdn.net/Day_Day_No_Bug/article/details/117512788?spm=1001.2014.3001.5501)




