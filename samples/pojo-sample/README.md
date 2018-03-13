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

   - [how to start the service center](http://servicecomb.incubator.apache.org/users/setup-environment/#)
   - make sure service center address is configured correctly in `microservice.yaml` file

   ```yaml
   cse:
     service:
       registry:
         address: http://127.0.0.1:30100		#service center address
   ```

2. Start the pojo-provider service

   - Start provider service by maven

     Compile the source code at root directory of ServiceComb Java Chassis, which is `incubator-servicecomb-java-chassis/`, and use `mvn exec` to execute the main class `PojoProviderMain`.

     ```bash
     cd incubator-servicecomb-java-chassis/
     mvn clean install -Psamples -DskipTests			#only need to install at first time.
     cd samples/pojo-sample/pojo-provider/
     mvn exec:java -Dexec.mainClass="org.apache.servicecomb.samples.pojo.provider.PojoProviderMain"
     ```

   - Start provider service by IDE

     Import the project by InteliJ IDEA or Eclipse, add sample module to pom.xml file in root module `incubator-servicecomb-java-chassis/pom.xml`, and add `<module>samples</module>` to `<modules></modules>` block, Then find `main` function `PojoProviderMain` of provider service and `RUN` it like any other Java program.

3. Start the pojo-consumer service 

   Just like how to start pojo-provider service. But the main class of pojo-consumer service is `PojoConsumerMain`. 

   ```bash
   cd samples/pojo-sample/pojo-consumer/
   mvn exec:java -Dexec.mainClass="org.apache.servicecomb.samples.pojo.consumer.PojoConsumerMain"
   ```

4. How to verify
   On the producer side, the output should contain the following stuffs if the producer starts up successfully:
   1. *'swagger: 2.0 info: version: 1.0.0 ...'* means the producer generated swagger contracts
   2. *'rest listen success. address=0.0.0.0:8080'* means the rest endpoint is listening on port 8080
   3. *'highway listen success. address=0.0.0.0:7070'* means the highway endpoint is listening on port 7070
   4. *'Register microservice instance success'* means the producer has registered successfully to service center
   
   On the consumer side, you can see the following outputs if the consumer can invoke the producer:
   1. *'Hello person ServiceComb/Java Chassis'* means the consumer calls sayHello by RpcReference successfully
   2. *'a=1, b=2, result=3'* means the consumer calls compute.add by RpcReference successfully
   ​