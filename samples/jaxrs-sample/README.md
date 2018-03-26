## JAX-RS Sample

ServiceComb supports developers in developing services in JAX-RS mode by using JAX-RS.

### Provider Service

* Define a service API. Compile the Java API definition based on the API definition defined before development
* Implement the service. JAX-RS is used to describe the development of service code. 
* Release the service. Add `@RestSchema` as the annotation of the service implementation class and specify schemaID, which indicates that the implementation is released as a schema of the current microservice.
* Create the jaxrsHello.bean.xml file in the resources/META-INF/spring directory and configure base-package that performs scanning

   [Detail information please refer to Doc](http://servicecomb.incubator.apache.org/users/develop-with-jax-rs/)

### Consumer Service

To consume a provider-service, only need to decalare a member of a service API type and add the `RpcReference` annotation for the member, the microservice that depends on the declaration and the `schemaID` just like pojo consumer sample.

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

2. Start the jaxrs-provider service

   - Start provider service by maven

     Compile the source code at root directory of ServiceComb Java Chassis, which is `incubator-servicecomb-java-chassis/`, and use `mvn exec` to execute the main class `JaxrsProviderMain`.

     ```bash
     cd incubator-servicecomb-java-chassis/
     mvn clean install -Psamples -DskipTests			#only need to install at first time.
     cd samples/jaxrs-sample/jaxrs-provider/
     mvn exec:java -Dexec.mainClass="org.apache.servicecomb.samples.jaxrs.provider.JaxrsProviderMain"
     ```

   - Start provider service by IDE

     Import the project by InteliJ IDEA or Eclipse, add sample module to pom.xml file in root module `incubator-servicecomb-java-chassis/pom.xml`, and add `<module>samples</module>` to `<modules></modules>` block, Then find `main` function `JaxrsProviderMain` of provider service and `RUN` it like any other Java Program.

3. Start the jaxrs-consumer service

   Just like how to start jaxrs-provider service. But the main class of jaxrs-consumer service is `JaxrsConsumerMain`. 

   ```bash
   cd samples/jaxrs-sample/jaxrs-consumer/
   mvn exec:java -Dexec.mainClass="org.apache.servicecomb.samples.jaxrs.consumer.JaxrsConsumerMain"
   ```

4. How to verify
   On the producer side, the output should contain the following stuffs if the producer starts up successfully:
   1. *'swagger: 2.0 info: version: 1.0.0 ...'* means the producer generated swagger contracts
   2. *'rest listen success. address=0.0.0.0:8080'* means the rest endpoint is listening on port 8080
   3. *'highway listen success. address=0.0.0.0:7070'* means the highway endpoint is listening on port 7070
   4. *'Register microservice instance success'* means the producer has registered successfully to service center
   
   On the consumer side, you can see the following outputs if the consumer can invoke the producer:
   1. *'Hello Java Chassis'* means the consumer calls sayhi by RpcReference successfully
   2. *'Hello person ServiceComb/Java Chassis'* means the consumer calls sayhello by RpcReference successfully
   3. *'Bye !'* means the consumer calls saybye by RestTemplate successfully