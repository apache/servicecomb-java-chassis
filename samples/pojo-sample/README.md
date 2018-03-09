## POJO Sample

POJO is also called as Transparent Remote Procedure Call (RPC), POJO development mode based on API and API implementation. The service developer does not need to use the description of Spring MVC and JAX-RS.

### Provider Service

The transparent RPC development mode supports two service release mode: Spring XML configuration and annotation configuration. 

The Spring XML Configuration Mode:

* Define a service API. Compile the Java API definition based on the API definition defined before development. 
* Implement the service. 
* Release the service. Create the `pojoHello.bean.xml` file in the `resources/META-INF/spring` directory and declare the schema in the file. 

The Develop Method by Configure Remarks:

* Define a service API, which is the same as the Spring XML mode
* Implement the service in the same way as using Spring XML.
* Release the service. `@RpcSchema` is used to define schema during the API Hello implementation. 

[Detail information please refer to Doc](http://servicecomb.incubator.apache.org/users/develop-with-transparent-rpc/)

### Consumer Service

To call a microservice, you only need to declare a member of a service API type and add the `@RpcReference` annotation for the member, the microservice that depends on the declaration, and the `schemaID`. The sample code is as follows.

```java
@Component
public class CodeFirstConsumerMain {
    @RpcReference(microserviceName = "codefirst", schemaId = "codeFirstHello")
    private static Hello hello;
    public static void main(String[] args) throws Exception {
     	//init first
        System.out.println(hello.sayHi("World!"));
    }
}
```

[Detail information please refer to Doc](http://servicecomb.incubator.apache.org/users/develop-with-rpc/)

### Sample Quick Start

1. Start the ServiceComb/Service Center

   [how to start the service center](http://servicecomb.incubator.apache.org/users/setup-environment/#)

2. Start the POJO provider

3. Start the POJO consumer