Web开发方式和JAVA应用方式的开发步骤基本类似。

本项目[代码示例](https://github.com/huaweicse/servicecomb-java-chassis-samples/tree/master/spring-boot-web)


主要有如下区别：

* JAVA应用方式基于spring-boot-starter，而Web开发方式基于spring-boot-starter-web。

* JAVA应用方式依赖spring-boot-starter-provider，而Web开发方式依赖spring-boot-starter-transport。spring-boot-starter-web已经携带了hibernate-validator，不需要额外依赖。

* 在启动函数中，Web开发方式可以通过声明

```
@SpringBootApplication(exclude=DispatcherServletAutoConfiguration.class)
```

来关闭org.springframework.web.servlet.DispatcherServlet，通过@EnableServiceComb会启用org.apache.servicecomb.transport.rest.servlet.RestServlet。虽然排除DispatcherServlet不是必须的，但是大多数场景一个微服务里面存在多个REST框架都不是很好的主意，会造成很多使用上的误解。

* 在microservice.yaml文件中通过配置项servicecomb.rest.servlet.urlPattern来指定RestServlet的URL根路径。并且配置项servicecomb.rest.address里面的监听端口，必须和tomcat监听的端口保持一致（默认是8080，可以通过application.yml中增加server.port修改）





集成java chassis后，可以通过它的方式开发REST接口：

```
@RestSchema(schemaId = "hello")
@RequestMapping(path = "/")
public class HelloService {
    @RequestMapping(path = "hello", method = RequestMethod.GET)
    public String sayHello(@RequestParam(name="name") String name) {
        return "Hello " + name;
    }
}
```

然后可以通过：http://localhost:9093/hello?name=world来访问。

可以看到使用的标签和Spring MVC大部分是一样的。但也有少量不一样的地方，比如：

1. 通过RestSchema替换RestController

2. 需要显示声明@RequestMapping

如果业务代码不是新开发，而是基于Spring MVC做的开发，现在java chassis基于做改造，还需要注意在禁用DispatcherServlet后，和其有关的功能特性将不再生效。

在下面的章节，还会详细介绍在Spring MVC模式下两者的区别。

