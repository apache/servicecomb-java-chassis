## 概念阐述

ServiceComb支持开发者使用JAX-RS注解，使用JAX-RS模式开发服务。

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

使用JAX-RS注解开发业务代码，Hello的服务实现如下：

```java
@RestSchema(schemaId = "jaxrsHello")
@Path("/jaxrshello")
@Produces(MediaType.APPLICATION_JSON)
public class JaxrsHelloImpl implements Hello {
    @Path("/sayhi")
    @POST
    @Override
    public String sayHi(String name) {
        return "Hello " + name;
    }

    @Path("/sayhello")
    @POST
    @Override
    public String sayHello(Person person) {
        return "Hello person " + person.getName();
    }
}
```

### 步骤 3发布服务。

在resources/META-INF/spring目录下创建jaxrsHello.bean.xml文件，配置spring进行服务扫描的base-package，文件内容如下。（注意修改package名称为正确名称）

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns=" http://www.springframework.org/schema/beans " xmlns:xsi=" http://www.w3.org/2001/XMLSchema-instance "
       xmlns:p=" http://www.springframework.org/schema/p " xmlns:util=" http://www.springframework.org/schema/util "
       xmlns:cse=" http://www.huawei.com/schema/paas/cse/rpc "
       xmlns:context=" http://www.springframework.org/schema/context "
       xsi:schemaLocation=" http://www.springframework.org/schema/beans classpath:org/springframework/beans/factory/xml/spring-beans-3.0.xsd http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-3.0.xsd http://www.huawei.com/schema/paas/cse/rpc classpath:META-INF/spring/spring-paas-cse-rpc.xsd">

    <context:component-scan base-package="org.apache.servicecomb.samples.jaxrs.provider"/>
</beans>
```

## 涉及API

JAX-RS开发模式当前支持如下注解，所有注解的使用方法参考[JAX-RS官方文档](https://jax-rs-spec.java.net/nonav/2.0-rev-a/apidocs/index.html)。

### 表1-1JAX-RS注解支持汇总

| 注解 | 位置 | 描述 |
| :--- | :--- | :--- |
| javax.ws.rs.Path | schema/operation | URL路径 |
| javax.ws.rs.Produces | schema/operation | 方法支持的编解码能力 |
| javax.ws.rs.DELETE | operation | http method |
| javax.ws.rs.GET | operation | http method |
| javax.ws.rs.POST | operation | http method |
| javax.ws.rs.PUT | operation | http method |
| javax.ws.rs.QueryParam | parameter | 从query string中获取参数 |
| javax.ws.rs.PathParam | parameter | 从path中获取参数，必须在path中定义该参数 |
| javax.ws.rs.HeaderParam | parameter | 从header中获取参数 |
| javax.ws.rs.CookieParam | parameter | 从cookie中获取参数 |

> **说明:**
>
> * 当方法参数没有注解，且不为HttpServletRequest类型参数时，默认为body类型参数，一个方法只支持最多一个body类型参数。
> * 打在参数上面的注解建议显式定义出value值，否则将直接使用契约中的参数名，例如应该使用`@QueryParam\("name"\) String name`，而不是`@QueryParam String name`。



