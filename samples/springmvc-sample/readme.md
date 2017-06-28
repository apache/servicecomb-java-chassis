# Spring MVC

It's sample of spring mvc


## How to write server code

### Service interface

```java
public interface HelloWorld {
    String sayHello(String name);
}
```

### Service implementation

write service code with spring mvc annotation

```java
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping(path = "/helloworld", produces = MediaType.APPLICATION_JSON_VALUE)
public class HelloWorldImpl implements HelloWorld {

    @RequestMapping(path = "/sayhello", method = RequestMethod.GET)
    public String sayHello(@RequestParam("name") String name) {
        return "hello " + name;
    }

}
```

### Service publish

add @RestSchema annotation to implementation class 

```java
import com.huawei.paas.cse.provider.rest.common.RestSchema;

......
@RestSchema(schemaId = "helloworld")
public class HelloWorldImpl implements HelloWorld {

    ......

}
```

make file hello.bean.xml to resources/META-INF/spring directory

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


## How to run sample

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