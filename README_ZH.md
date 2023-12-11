# Java Chassis | [English](README.md) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.apache.servicecomb/java-chassis-core/badge.svg)](http://search.maven.org/#search%7Cga%7C1%7Corg.apache.servicecomb) [![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html) 

Apache ServiceComb Java Chassis 给开发者提供一个快速构建微服务的JAVA SDK。它包含如下特性：

* 多种开发风格，REST(JAX-RS、Spring MVC）和RPC
* 多种通信协议, HTTP over Vert.x、Http Over Servlet、Highway等
* 统一一致的服务提供者、服务消费者处理链，以及基于契约的开箱即用的服务治理能力

开发者可以通过[设计选型参考][design]了解更多特性和设计原理。

[design]: https://servicecomb.apache.org/references/java-chassis/zh_CN/start/design.html

# releases

| 版本火车  | 最新版本   | 编译的JDK版本   | 支持的JDK版本          | Open API | 备注              |
|-------|--------|------------|-------------------|----------|-----------------|
| 3.x.x | 3.0.0  | OpenJDK 17 | OpenJDK 17        | 3.0.x    | 依赖Spring Boot 3 |
| 2.x.x | 2.8.13 | OpenJDK 8  | OpenJDK 8, 11, 17 | 2.0.x    | 依赖Spring 5      |
| 1.x.x | 1.3.11 | OpenJDK 8  | OpenJDK 8         | 2.0.x    | 停止更新            |

>>>NOTICE: Open API 3.0.x 不兼容 2.0.x， 因此Java Chassis 2.x.x不能与3.x.x共存互访. 升级3.x.x，需要将相关的消费者、提供者和边缘服务同时升级.

>>>NOTICE: Java Chassis 1.x.x 第一个版本于2018发布，目前已经停止维护.

# 为什么使用Java Chassis

- **高性能**

  Java Chassis 网络层基于 [Vert.x](https://vertx.io) 实现, 支持开发者使用[响应式编程](https://www.reactivemanifesto.org), 开发者在使用熟悉的REST风格设计业务接口的时候，也能够获取到非常高性能的吞吐量。同时还提供了Highway协议，满足更高性能场景的要求。

- **原生支持OpenAPI**

  Java Chassis 的接口开发、服务治理都基于 [Swagger](https://swagger.io) ，并通过接口语义检查，使得接口定义符合 [OpenAPI 规范](https://swagger.io/specification/v3/). 

- **灵活的开发方式**

  开发者可以使用 `SpringMVC`/`JAX-RS`/`Transparent RPC` 任意一种方式定义服务端接口, 并使用`RPC`/`RestTemplate` 等方式访问这些接口. 得益于Java Chassis的通信层与开发方式分离的设计，开发者可以在 `Rest over Vertx`/`Rest over Servlet`/`Highway`等通信模式下自由切换.

- **开箱即用的服务治理能力**

  Java Chassis 提供了大量开箱即用的服务治理能力，包括服务发现、熔断容错、负载均衡、流量控制等。


# 快速开始 (Spring MVC)

定义提供者。

```java
@RestSchema(schemaId = "ProviderController")
@RequestMapping(path = "/")
public class ProviderController {
  @GetMapping("/sayHello")
  public String sayHello(@RequestParam("name") String name) {
    return "Hello " + name;
  }
}
```

定义消费者。首先定义一个接口，与需要访问的提供者的方法拥有一样的签名(方法名、返回值类型、参数名称和参数类型)。

```java
public interface ProviderService {
  String sayHello(String name);
}
```

使用RPC方式访问提供者。 

```java
@RestSchema(schemaId = "ConsumerController")
@RequestMapping(path = "/")
public class ConsumerController {
  @RpcReference(schemaId = "ProviderController", microserviceName = "provider")
  private ProviderService providerService;

  // consumer service which delegate the implementation to provider service.
  @GetMapping("/sayHello")
  public String sayHello(@RequestParam("name") String name) {
    return providerService.sayHello(name);
  }
}
```

# 用户文档

请访问 [ServiceComb Java Chassis 开发指南][java-chassis-developer-guide].

[java-chassis-developer-guide]:  https://servicecomb.apache.org/references/java-chassis/zh_CN/

# 编译Java Chassis

开发者可以通过公有mavan仓库使用Java Chassis。 如果需要构建项目，需要使用JDK 8版本，并预先安装maven。 

      mvn clean install

# 运行测试用例

开发者需要预先安装docker。

      mvn clean install -Pdocker -Pit

使用docker machine。

      mvn clean install -Pdocker -Pit -Pdocker-machine

# 联系我们

报告缺陷: [issues](https://issues.apache.org/jira/browse/SCB)

邮件列表: [subscribe](mailto:dev-subscribe@servicecomb.apache.org)  [dev](https://lists.apache.org/list.html?dev@servicecomb.apache.org)

# 参与代码提交

参考 [如何做贡献](http://servicecomb.apache.org/cn/developers/contributing).

# License
Licensed under an [Apache 2.0 license](LICENSE).
