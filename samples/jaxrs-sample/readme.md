# JAX RS

It's sample of jax rs

## How to write server code 




## How to run sample

### service interface

```java
public interface HelloWorld {
    String sayHello(String name);
}
```

### service implementation

write code with jax rs annotations.

```java
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/helloworld")
@Produces(MediaType.APPLICATION_JSON)
public class HelloWorldImpl implements HelloWorld {

    @Path("/sayhello")
    @GET
    public String sayHello(@QueryParam("name") String name) {
        return "hello " + name;
    }

}
```

### service publish

add @RestSchema to implementation class

```java
import com.huawei.paas.cse.provider.rest.common.RestSchema;

......
@RestSchema(schemaId = "helloworld")
public class HelloWorldImpl implements HelloWorld {

    ......

}
```

make file hello.bean.xml to directory resources/META-INF/spring

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:p="http://www.springframework.org/schema/p"
    xmlns:util="http://www.springframework.org/schema/util" xmlns:context="http://www.springframework.org/schema/context"
    xsi:schemaLocation="
        http://www.springframework.org/schema/beans classpath:org/springframework/beans/factory/xml/spring-beans-3.0.xsd
        http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-3.0.xsd">

    <context:component-scan base-package="com.huawei.paas.cse.helloworld" />

</beans>

```




### 1. Start the ServiceComb/Service Center

[how to start the service center](http://servicecomb.io/docs/start-sc/)

### 2.Start the Microservice server

```bash
mvn test -Pserver
```

### 3.Start the Microservice client

```bash
mvn test -Pclient
```