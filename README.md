# Java Chassis [中文](README_ZH.md) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.apache.servicecomb/java-chassis-core/badge.svg)](http://search.maven.org/#search%7Cga%7C1%7Corg.apache.servicecomb) [![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

Apache ServiceComb Java Chassis is a Software Development Kit (SDK) for rapid development of microservices in Java, providing service registration, service discovery, dynamic routing, and service management features. 

# releases

| Release Train | Latest Version | Compiled JDK Version | Tested JDK Version | Open API | Notes                    |
|---------------|----------------|----------------------|--------------------|----------|--------------------------|
| 3.x.x         | 3.0.0          | OpenJDK 17           | OpenJDK 17         | 3.0.x    | Depends on Spring Boot 3 |
| 2.x.x         | 2.8.13         | OpenJDK 8            | OpenJDK 8, 11, 17  | 2.0.x    | Depends on Spring 5      |
| 1.x.x         | 1.3.11         | OpenJDK 8            | OpenJDK 8          | 2.0.x    | End of Support           |

>>>NOTICE: Open API 3.0.x is not compatible with 2.0.x and Java Chassis 2.x.x can not 
> work together with 3.x.x. All related consumers, providers and edge service need use the 3.x.x version when upgrading.

>>>NOTICE: Java Chassis 1.x.x reached its end of support now after it's first release from 2018. 

# Why use Java Chassis

- **High performance**

  The transport capability of Java Chassis is based on [Vert.x](https://vertx.io), which enables Java Chassis to process
  massive requests with relatively less hardware resources, and support [reactive develop style](https://www.reactivemanifesto.org).

- **Native support for OpenAPI**

  Java Chassis describes the APIs of the microservices via [Swagger](https://swagger.io) natively, to help
  developers to design microservices that comply to [OpenAPI standard](https://swagger.io/specification/v3/).

- **Flexible develop style**

  Java Chassis allows developers to develop their microservice APIs in `SpringMVC`/`JAX-RS`/`Transparent RPC` styles,
  and to send the request in `RPC`/`RestTemplate` styles. And there are three kind of build-in transport mode:
  `Rest over Vertx`/`Rest over Servlet`/`Highway`. All of these features can be combined and replaced easily,
  because they are decoupled and all based on the Swagger schema, which can provide high flexibility.

- **Out-of-box microservice governance features**

  Java Chassis provides a lot of features for microservice governance and monitor.

# Quick Start (Spring MVC)

Provider service:
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

Consumer service:

Declare Provider service interface to match the Provider method signature. (Method name, return type, parameter name, parameter type)
```java
public interface ProviderService {
  String sayHello(String name);
}
```

Invoke Provider service with RPC
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

# Documentation

Project documentation is available on the [ServiceComb Java Chassis Developer Guide][java-chassis-developer-guide].

[java-chassis-developer-guide]: https://servicecomb.apache.org/references/java-chassis/en_US/

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

# License
Licensed under an [Apache 2.0 license](LICENSE).
