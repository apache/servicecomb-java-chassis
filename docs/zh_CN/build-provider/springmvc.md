## 概念阐述

ServiceComb支持SpringMVC注解，允许使用SpringMVC风格开发微服务。

## 开发示例

### 步骤 1定义服务接口。

根据开发之前定义好的契约，编写Java业务接口，代码如下。定义接口不是必须的，但是 一个好习惯，可以简化客户端使用RPC方式编写代码。

```java
public interface Hello {
    String sayHi(String name);
    String sayHello(Person person);
}
```



### 步骤 2实现服务。

使用Spring MVC注解开发业务代码，Hello的服务实现如下。在服务的实现类上打上注解@RestSchema，指定schemaId，schemaId必须保证微服务范围内唯一。

```java
@RestSchema(schemaId = "springmvcHello")
@RequestMapping(path = "/springmvchello", produces = MediaType.APPLICATION_JSON)
public class SpringmvcHelloImpl implements Hello {
    @Override
    @RequestMapping(path = "/sayhi", method = RequestMethod.POST)
    public String sayHi(@RequestParam(name = "name") String name) {
        return "Hello " + name;
    }

    @Override
    @RequestMapping(path = "/sayhello", method = RequestMethod.POST)
    public String sayHello(@RequestBody Person person) {
        return "Hello person " + person.getName();
    }
}
```

### 步骤 3发布服务

在`resources/META-INF/spring`目录下创建`springmvcHello.bean.xml`文件，命名规则为`\*.bean.xml`，配置spring进行服务扫描的base-package，文件内容如下：

```xml
<?xml version="1.0" encoding="UTF-8"?>

<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans classpath:org/springframework/beans/factory/xml/spring-beans-3.0.xsd
       http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-3.0.xsd">

    <context:component-scan base-package="org.apache.servicecomb.samples.springmvc.povider"/>
</beans>
```

## 涉及API

Spring MVC开发模式当前支持org.springframework.web.bind.annotation包下的如下注解，所有注解的使用方法参考[Spring MVC官方文档](https://docs.spring.io/spring/docs/current/spring-framework-reference/html/mvc.html)。

### 表1-1 Spring MVC注解支持汇总

| 注解 | 位置 | 描述 |
| :--- | :--- | :--- |
| RequestMapping | schema/operation | 支持标注path/method/produces三种数据，operation默认继承schema上的produces |
| PathVariable | parameter | 从path中获取参数 |
| RequestParam | parameter | 从query中获取参数 |
| RequestHeader | parameter | 从header中获取参数 |
| RequestBody | parameter | 从body中获取参数，每个operation只能有一个body参数 |



