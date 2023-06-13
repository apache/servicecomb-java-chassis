# Java Chassis [中文](README_ZH.md) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.apache.servicecomb/java-chassis-core/badge.svg)](http://search.maven.org/#search%7Cga%7C1%7Corg.apache.servicecomb) [![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

Apache ServiceComb Java Chassis is a Software Development Kit (SDK) for rapid development of microservices in Java, providing service registration, service discovery, dynamic routing, and service management features

# releases

| Release Train | Latest Version | Compiled JDK Version | Tested JDK Version            |
|---------------|----------------|----------------------|-------------------------------|
| 2.x.x         | 2.8.3          | OpenJDK 8            | OpenJDK 8, 11, 17 | 
| 1.x.x         | 1.3.10         | OpenJDK 8            | OpenJDK 8         |

# Why use Java Chassis

- **High performance**

  The transport capability of Java Chassis is based on [Vert.x](https://vertx.io), which enables Java Chassis to process
  massive requests with relatively less hardware resources, and support [reactive develop style](https://www.reactivemanifesto.org).

- **Native support for OpenAPI**

  Java Chassis describes the APIs of the microservices via [Swagger](https://swagger.io) natively, to help
  developers to design microservices that comply to [OpenAPI standard](https://swagger.io/specification/v2/).

- **Flexible develop style**

  Currently Java Chassis allow developers to develop their microservice APIs in `SpringMVC`/`JAX-RS`/`transparent RPC` styles,
  and to send the request in `RPC`/`RestTemplate` styles. And there are three kind of build-in transport mode:
  `Rest over Vertx`/`Rest over Servlet`/`Highway`. All of these features can be combined and replaced easily,
  because they are decoupled and all based on the Swagger schema, which can provide high flexibility.

- **Out-of-box microservice governance features**

  Java Chassis provides a lot of features for microservice governance and monitor.

# Quick Start

Provider service:
```java
import org.apache.servicecomb.*;
@RpcSchema(schemaId = "helloworld")
public class HelloWorldProvider implements HelloWorld {
    public String sayHello(String name) {
        return "Hello " + name;
    }
}
```

Consumer service:
```java
import org.apache.servicecomb.*;
@Component
public class HelloWorldConsumer  {
	@RpcReference(microserviceName = "pojo", schemaId = "helloworld")
	private static HelloWorld helloWorld;

	public static void main(String[] args) {
		helloWorld.sayHello("Tank");
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

# Export Notice

This distribution includes cryptographic software. The country in which you currently reside may have restrictions on the import, possession, use, and/or re-export to another country, of encryption software. BEFORE using any encryption software, please check your country's laws, regulations and policies concerning the import, possession, or use, and re-export of encryption software, to see if this is permitted. See <http://www.wassenaar.org/> for more information.

The Apache Software Foundation has classified this software as Export Commodity Control Number (ECCN) 5D002, which includes information security software using or performing cryptographic functions with asymmetric algorithms. The form and manner of this Apache Software Foundation distribution makes it eligible for export under the "publicly available" Section 742.15(b) exemption (see the BIS Export Administration Regulations, Section 742.15(b)) for both object code and source code.

The following provides more details on the included cryptographic software:

  * Vertx transport can be configured for secure communications
