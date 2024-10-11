# Java Chassis [中文](README_ZH.md) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.apache.servicecomb/java-chassis-core/badge.svg)](http://search.maven.org/#search%7Cga%7C1%7Corg.apache.servicecomb) [![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

Apache ServiceComb Java Chassis is a Software Development Kit (SDK) for rapid development of microservices in Java, providing service registration, service discovery, dynamic routing, and service management features.

# releases

| Release Train  | Latest Version | Compiled JDK Version | Tested JDK Version | Open API | Notes                    |
|----------------|----------------|----------------------|--------------------|----------|--------------------------|
| Java Chassis 3 | 3.2.2          | OpenJDK 17           | OpenJDK 17         | 3.0.x    | Depends on Spring Boot 3 |
| Java Chassis 2 | 2.8.20         | OpenJDK 8            | OpenJDK 8, 11, 17  | 2.0.x    | Depends on Spring 5      |
| Java Chassis 1 | 1.3.11         | OpenJDK 8            | OpenJDK 8          | 2.0.x    | End of Support           |

>>>NOTICE: Since Open API 3.0.x is not compatible with 2.0.x, Java Chassis 2 and Java Chassis 1 can not
> work together with Java Chassis 3. All related consumers, providers and edge service need use Java Chassis 3 when upgrading.

>>>NOTICE: Java Chassis 1 reached its end of support now after it's first release from 2018.

# Quick Start

* Define API
```java
@RequestMapping(path = "/provider")
public interface ProviderService {
  @GetMapping("/sayHello")
  String sayHello(@RequestParam("name") String name);
}
```

* Provider service
```java
@RestSchema(schemaId = "ProviderController", schemaInterface = ProviderService.class)
public class ProviderController implements ProviderService {
  @Override
  public String sayHello(String name) {
    return "Hello " + name;
  }
}
```

* Consumer service
```java
@Configuration
public class ProviderServiceConfiguration {
  @Bean
  public ProviderService providerService() {
    return Invoker.createProxy("provider", "ProviderController", ProviderService.class);
  }
}
```

Invoke Provider service with RPC
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

Try out this example [here](https://servicecomb.apache.org/references/java-chassis/zh_CN/start/first-sample.html) .

# Documentation

Project documentation is available on the [ServiceComb Java Chassis Developer Guide][java-chassis-developer-guide].

[java-chassis-developer-guide]: https://servicecomb.apache.org/references/java-chassis/zh_CN/

# Building

  You don’t need to build from source to use Java Chassis (binaries in apache nexus ), but if you want to try out the latest and greatest, Java Chassis can be easily built with the maven.  You also need JDK 17.

      mvn clean install

The first build may take a longer than expected as Maven downloads all the dependencies.

# Automated Testing

  To build the docker image and run the integration tests with docker, you can use maven docker profile

      mvn clean install -Pdocker -Pit

  If you are using docker machine, please use the following command

      mvn clean install -Pdocker -Pit -Pdocker-machine

# Contact

Bugs: [issues](https://issues.apache.org/jira/browse/SCB)

mailing list: [subscribe](mailto:dev-subscribe@servicecomb.apache.org)  [dev](https://lists.apache.org/list.html?dev@servicecomb.apache.org)


# Contributing

See [CONTRIBUTING](http://servicecomb.apache.org/developers/contributing) for details on submitting patches and the contribution workflow.

# Star this project

If you like this project, do not forget star it.

[![Star History Chart](https://api.star-history.com/svg?repos=apache/servicecomb-java-chassis&type=Date)](https://star-history.com/#apache/servicecomb-java-chassis&Date)

# License
Licensed under an [Apache 2.0 license](LICENSE).
