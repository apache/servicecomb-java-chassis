# POJO

It's sample of pojo

## How to write server code

### service interface

```java
public interface HelloWorld {
    String sayHello(String name);
}
```

### service implementation

```java
public class HelloWorldImpl implements HelloWorld {
    public String sayHello(String name) {
        return "hello " + name;
    }
}
```

### service publish

#### spring xml

make file "META-INF/spring/hello.bean.xml" to resources directory 

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:p="http://www.springframework.org/schema/p"
    xmlns:util="http://www.springframework.org/schema/util" xmlns:cse="http://www.huawei.com/schema/paas/cse/rpc"
    xsi:schemaLocation="
        http://www.springframework.org/schema/beans classpath:org/springframework/beans/factory/xml/spring-beans-3.0.xsd
        http://www.huawei.com/schema/paas/cse/rpc classpath:META-INF/spring/spring-paas-cse-rpc.xsd">

    <cse:rpc-schema schema-id="helloworld"
        implementation="com.huawei.paas.cse.helloworld.server.HelloWorldImpl"></cse:rpc-schema>
</beans>
```


#### annotation

```java
import com.huawei.paas.cse.provider.pojo.RpcSchema;

@RpcSchema(schemaId = "helloworld")
public class HelloWorldImpl implements HelloWorld {
...
}
```

make file "META-INF/spring/hello.bean.xml" to resources directory 

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:context="http://www.springframework.org/schema/context"
    xsi:schemaLocation="
        http://www.springframework.org/schema/beans classpath:org/springframework/beans/factory/xml/spring-beans-3.0.xsd 
        http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-3.0.xsd">

    <context:component-scan base-package="com.huawei.paas.cse.helloworld.server" />

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