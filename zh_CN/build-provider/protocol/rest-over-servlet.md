## 配置说明

REST over Servlet对应使用web容器模式部署运行，需要新建一个servlet工程将微服务包装起来，加载到web容器中启动运行，包装微服务的方法有两种，一种完全使用web.xml配置文件配置，另一种仅在web.xml文件中配置listener，在microservice.yaml文件中配置urlPattern，两种方式任选一种即可，配置方式如下所示：

* 在web.xml文件中完成全部配置  
  　　web.xml文件配置在项目的src/main/webapp/WEB\_INF目录，配置内容如下：

  ```xml
  <web-app xmlns="http://java.sun.com/xml/ns/javaee" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd"
         version="3.0">
    <context-param>
        <param-name>contextConfigLocation</param-name>
        <param-value>classpath*:META-INF/spring/*.bean.xml </param-value>
    </context-param>
    <listener>
        <listener-class>org.apache.servicecomb.transport.rest.servlet.RestServletContextListener</listener-class>
    </listener>
    <servlet>
        <servlet-name>RestServlet</servlet-name>
        <servlet-class>org.apache.servicecomb.transport.rest.servlet.RestServlet</servlet-class>
        <load-on-startup>1</load-on-startup>
        <async-supported>true</async-supported>
    </servlet>
    <servlet-mapping>
        <servlet-name>RestServlet</servlet-name>
        <url-pattern>/rest/*</url-pattern>
    </servlet-mapping>
  </web-app>
  ```

* 在web.xml文件中仅配置listener，在microservice.yaml文件中配置urlPattern

  ```xml
  <web-app xmlns="http://java.sun.com/xml/ns/javaee" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd"
         version="3.0">
    <context-param>
        <param-name>contextConfigLocation</param-name>
        <param-value>classpath*:META-INF/spring/*.bean.xml</param-value>
    </context-param>
    <listener>
        <listener-class>org.apache.servicecomb.transport.rest.servlet.RestServletContextListener</listener-class>
    </listener>
  </web-app>
  ```

  在microservice.yaml文件中需要增加一行配置来指定urlPattern：

  ```yaml
  servicecomb.rest.servlet.urlPattern: /rest/*
  ```

以上两种方式是等效的，两种方式都需要在maven pom文件中添加如下依赖：

```xml
<dependency>
    <groupId>org.apache.servicecomb</groupId>
    <artifactId>transport-rest-servlet</artifactId>
</dependency>
```

以上示例中：

* contextConfigLocation描述需要加载哪些spring xml，classpath\*:META-INF/spring/\*.bean.xml是内置规则，如果不写，内部也会自动加上，所以这里只需要关注业务级的特殊设置即可
* urlPattern也是根据业务自身的规划设置即可，不要求必须是/rest/\*

**Spring MVC用户的注意事项**

微服务默认不建议跟UI功能混合在一起，所以RestServletContextListener中并未初始化springmvc内置的spring context。如果业务有特殊需求，需要将UI跟微服务混合在一个进程中部署，并且需要使用springmvc中的view能力，开发者不能在web.xml中同时配置RestServletContextListener以及springmvc的DispatcherServlet。开发者可以将org.springframework.web.servlet.DispatcherServlet修改为org.apache.servicecomb.transport.rest.servlet.CseDispatcherServlet。

CseDispatcherServlet的功能在2018.2月以后发布的版本中才有，如果是之前的版本需要继承DispatcherServlet，再在web.xml中使用继承类：

```
@Override
protected WebApplicationContext createWebApplicationContext(ApplicationContext parent){
  setContextClass(CseXmlWebApplicationContext.class);
  return super.createWebApplicationContext(parent);
}
```

**使用Filter注意事项**

RestServlet工作于异步模式，根据servlet 3.0的标准，整条工作链都必须是异步模式，所以，如果业务在这个流程上增加了servlet filter，也必须将它配置为异步：

```xml
<filter>
  ......
  <async-supported>true</async-supported>
</filter>
```

## **配置项**

REST over Servlet在microservice.yaml文件中的配置项见表3-9。

表1-1 REST over Servlet配置项说明

| 配置项 | 默认值 | 取值范围 | 是否必选 | 含义 | 注意 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| servicecomb.rest.address | 0.0.0.0:8080 | - | 否 | 服务监听地址 | 必须配置为与web容器监听地址相同的地址 |
| servicecomb.rest.server.timeout | 3000 | - | 否 | 超时时间 | 单位为毫秒 |
| servicecomb.rest.servlet.urlPattern | 无 |  | 否 | 用于简化servlet+servlet mapping配置 | 只有在web.xml中未配置servlet+servlet mapping时，才使用此配置项，配置格式为：/\* 或  /path/\*，其中path可以是多次目录 |

microservice.yaml文件中的配置示例如下：

```yaml
servicecomb:
  rest:
    address: 0.0.0.0:8080
    server:
      timeout: 3000
```



