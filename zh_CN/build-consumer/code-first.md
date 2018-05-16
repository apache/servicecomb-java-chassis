## 场景描述

服务消费者可以不用显式地将契约存放在项目目录中，当程序启动时，ServiceComb框架会自动根据microservice.yaml文件中配置的服务提供者的微服务名称和版本号，从服务中心拉取契约信息。

## 涉及API

使用隐式契约可用于RestTemplate、透明RPC两种服务消费者开发模式，使用RestTemplate的开发方式参见4.3 使用RestTemplate开发服务消费者。

## 示例代码

本小节以透明RPC开发模式为例展示如何使用隐式契约开发服务消费者。

服务消费者的示例代码如下：

```java
import org.springframework.stereotype.Component;
import org.apache.servicecomb.foundation.common.utils.BeanUtils;
 import org.apache.servicecomb.foundation.common.utils.Log4jUtils;
 import org.apache.servicecomb.provider.pojo.RpcReference;
 import org.apache.servicecomb.samples.common.schema.Hello;
 import org.apache.servicecomb.samples.common.schema.models.Person;
@Component
 public class CodeFirstConsumerMain {
@RpcReference(microserviceName = "codefirst", schemaId = "codeFirstHello")
 private static Hello hello;
public static void main(String[] args) throws Exception {
 init();
 System.out.println(hello.sayHi("Java Chassis"));
 Person person = new Person();
 person.setName("ServiceComb/Java Chassis");
 System.out.println(hello.sayHello(person));
 }
public static void init() throws Exception {
 Log4jUtils.init();
 BeanUtils.init();
 }
 }
```

在以上代码中，服务消费者已经取得了服务提供者的服务接口Hello，并在代码中声明一个Hello类型的成员。通过在hello上使用RPCReference注解指明微服务名称和schemaId，ServiceComb框架可以在程序启动时从服务中心获取到对应的服务提供者实例信息，并且生成一个代理注入到hello中，用户可以像调用本地类一样调用远程服务。
