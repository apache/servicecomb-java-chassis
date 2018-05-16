# Java Chassis  
[![Build Status](https://travis-ci.org/apache/incubator-servicecomb-java-chassis.svg?branch=master)](https://travis-ci.org/apache/incubator-servicecomb-java-chassis?branch=master) [![Coverage Status](https://coveralls.io/repos/github/apache/incubator-servicecomb-java-chassis/badge.svg?branch=master)](https://coveralls.io/github/apache/incubator-servicecomb-java-chassis?branch=master) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.apache.servicecomb/java-chassis-core/badge.svg)](http://search.maven.org/#search%7Cga%7C1%7Corg.apache.servicecomb) [![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)  

Apache ServiceComb (incubating) Java Chassis is a Software Development Kit (SDK) for rapid development of microservices in Java, providing service registration, service discovery, dynamic routing, and service management features

## Quick Start

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

## Documentation

Project documentation is available on the [ServiceComb website][servicecomb-website].

[servicecomb-website]: http://servicecomb.incubator.apache.org/

## Building

You don’t need to build from source to use Java Chassis (binaries in apache nexus ), but if you want to try out the latest and greatest, Java Chassis can be easily built with the maven.  You also need JDK 1.8.

      mvn clean install 

If you want to build the release kits from the source

      mvn clean install  -Prelease,distribution

The first build may take a longer than expected as Maven downloads all the dependencies.

## Automated Testing

  To build the docker image and run the integration tests with docker, you can use maven docker profile

      mvn clean install -Pdocker -Pit

  If you are using docker machine, please use the following command

      mvn clean install -Pdocker -Pit -Pdocker-machine

## Contact

Bugs: [issues](https://issues.apache.org/jira/browse/SCB)

mailing list: [subscribe](mailto:dev-subscribe@servicecomb.incubator.apache.org)  [dev](https://lists.apache.org/list.html?dev@servicecomb.apache.org)


## Contributing

See CONTRIBUTING for details on submitting patches and the contribution workflow.

## Reporting Issues

See reporting bugs for details about reporting any issues.
