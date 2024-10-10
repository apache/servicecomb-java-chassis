# Java Chassis | [English](README.md) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.apache.servicecomb/java-chassis-core/badge.svg)](http://search.maven.org/#search%7Cga%7C1%7Corg.apache.servicecomb) [![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

Apache ServiceComb Java Chassis 给开发者提供一个快速构建微服务的JAVA SDK。它包含如下特性：

* 基于Open API的契约优先(API First)开发模式，满足开发过程可管理、开发规范可验证要求。
* 多种开发风格，REST(JAX-RS、Spring MVC）和RPC等，高效支持遗留系统迁移和新系统开发场景。
* 多种通信协议, HTTP over Vert.x、Http Over Servlet、Highway等，满足不同场景对于性能、韧性的需求。
* 统一一致的服务提供者、服务消费者处理链，易于扩展新功能。
* 提供服务发现、配置管理、熔断、限流、灰度发布等开箱即用的服务治理能力。

可以通过[设计选型参考](https://servicecomb.apache.org/references/java-chassis/zh_CN/start/design.html) 了解更多特性和设计原理。

# 发布版本

| 版本火车           | 最新版本   | 编译的JDK版本   | 支持的JDK版本          | Open API | 备注              |
|----------------|--------|------------|-------------------|----------|-----------------|
| Java Chassis 3 | 3.2.2  | OpenJDK 17 | OpenJDK 17        | 3.0.x    | 依赖Spring Boot 3 |
| Java Chassis 2 | 2.8.20 | OpenJDK 8  | OpenJDK 8, 11, 17 | 2.0.x    | 依赖Spring 5      |
| Java Chassis 1 | 1.3.11 | OpenJDK 8  | OpenJDK 8         | 2.0.x    | 停止更新            |

>>>NOTICE: Open API 3.0.x 不兼容 2.0.x， 因此Java Chassis 2、Java Chassis 1不能与Java Chassis 3共存互访. 升级Java Chassis 3, 需要将相关的消费者、提供者和边缘服务同时升级.

>>>NOTICE: Java Chassis 1 第一个版本于2018发布，已经停止更新.

# 快速开始

* 定义服务契约

```java
@RequestMapping(path = "/provider")
public interface ProviderService {
  @GetMapping("/sayHello")
  String sayHello(@RequestParam("name") String name);
}
```

* 定义提供者

```java
@RestSchema(schemaId = "ProviderController", schemaInterface = ProviderService.class)
public class ProviderController implements ProviderService {
  @Override
  public String sayHello(String name) {
    return "Hello " + name;
  }
}
```

* 定义消费者

```java
@Configuration
public class ProviderServiceConfiguration {
  @Bean
  public ProviderService providerService() {
    return Invoker.createProxy("provider", "ProviderController", ProviderService.class);
  }
}
```

使用RPC方式访问提供者。

```java
@RestSchema(schemaId = "ConsumerController", schemaInterface = ConsumerService.class)
public class ConsumerController implements ConsumerService {
  private ProviderService providerService;

  @Autowired
  public void setProviderService(ProviderService providerService) {
    this.providerService = providerService;
  }

  @Override
  public String sayHello(String name) {
    return providerService.sayHello(name);
  }
}
```

下载并体验上述[示例项目](https://servicecomb.apache.org/references/java-chassis/zh_CN/start/first-sample.html) .

# 用户文档

请访问 [ServiceComb Java Chassis 开发指南][java-chassis-developer-guide].

[java-chassis-developer-guide]:  https://servicecomb.apache.org/references/java-chassis/zh_CN/

# 编译 Java Chassis

开发者可以通过MAVEN仓库使用Java Chassis。 如果需要构建项目，需要使用JDK 17版本，并预先安装maven。

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

# 给项目加星

如果喜欢这个项目，请给项目加星，把它推荐给更多人。

[![Star History Chart](https://api.star-history.com/svg?repos=apache/servicecomb-java-chassis&type=Date)](https://star-history.com/#apache/servicecomb-java-chassis&Date)

# License
Licensed under an [Apache 2.0 license](LICENSE).
