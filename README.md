<<<<<<< HEAD
# Java Chassis [![Build Status](https://travis-ci.org/ServiceComb/java-chassis.svg?branch=master)](https://travis-ci.org/ServiceComb/java-chassis.svg?branch=master)[![Coverage Status](https://coveralls.io/repos/github/WillemJiang/java-chassis/badge.svg?branch=master)](https://coveralls.io/github/WillemJiang/java-chassis?branch=master)

=======
# Java Chassis [![Build Status](https://travis-ci.org/ServiceComb/java-chassis.svg?branch=master)](https://travis-ci.org/WillemJiang/java-chassis)[![Coverage Status](https://coveralls.io/repos/github/WillemJiang/java-chassis/badge.svg?branch=master)](https://coveralls.io/github/WillemJiang/java-chassis?branch=master)
>>>>>>> a6891685f01681c2dc744e7aa46e9c68a3994390
ServiceComb Java Chassis is a Software Development Kit (SDK) for rapid development of microservices in Java, providing service registration, service discovery, dynamic routing, and service management features

- [http://www.servicecomb.io](http://www.servicecomb.io)

## Quick Start

Provider service:
```java
import io.servicecomb.*;
@RpcSchema(schemaId = "helloworld")
public class HelloWorldProvider implements HelloWorld {
    public String sayHello(String name) {
        return "Hello " + name;
    }
}
```

Consumer service:
```java
import io.servicecomb.*;
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

Project documentation is available on the ServiceComb website.

http://www.servicecomb.io/java-chassis/documentation

## Building

You don’t need to build from source to use Java Chassis (binaries in repo.servicecomb.io), but if you want to try out the latest and greatest, Java Chassis can be easily built with the maven.  You also need JDK 1.8.

      mvn clean install

The first build may take a longer than expected as Maven downloads all the dependencies.

## Automated Testing

  To build the docker image and run the integration tests with docker, you can use maven docker profile 
  
      mvn clean install -Pdocker
      
  If you are using docker machine, please use the following command
  
      mvn clean install -Pdocker -Pdocker-machine
      
## Contact

Mailing list: 

Planning/Roadmap: milestones, roadmap

Bugs: issues

## Contributing

See CONTRIBUTING for details on submitting patches and the contribution workflow.

## Reporting Issues

See reporting bugs for details about reporting any issues.
