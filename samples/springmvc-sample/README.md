# Spring MVC Sample

`RestTemplate` is a RESTful API provide by the Spring framework. ServiceComb provides the API for service calling. Users can call microservices using customized URL and `RestTemplate` instance provided by ServiceComb regardless of the specific address of the service.

* The URL must be in format of ServiceComb: `cse://microserviceName/path?querystring`.
* During use of this URL format, the ServiceComb framework will perform internal microservice descovery, fallbreak, and fault tolerance and send the requests to the microservice providers.

## Sample Quick Start

1. Start the ServiceComb/Service Center

   * [how to start the service center](http://servicecomb.incubator.apache.org/users/setup-environment/#)
   * make sure service center address is configured correctly in `microservice.yaml` file

   ```yaml
   cse:
     service:
       registry:
         address: http://127.0.0.1:30100		#service center address
   ```

2. Start the springmvc-provider service

   * Start provider service by maven

     Compile the source code at root directory of ServiceComb Java Chassis, which is `incubator-servicecomb-java-chassis/`, and use `mvn exec` to execute the main class `SpringmvcProviderMain`.

     ```bash
     cd incubator-servicecomb-java-chassis/
     mvn clean install -Psamples -DskipTests		#Only need to install at first time
     cd samples/springmvc-sample/springmvc-provider/
     mvn exec:java -Dexec.mainClass="org.apache.servicecomb.samples.springmvc.provider.SpringmvcProviderMain"
     ```

   * Start provider service by IDE

     Import the project by InteliJ IDEA or Eclipse, add sample module to pom.xml file in root module `incubator-servicecomb-java-chassis/pom.xml`, and add `<module>samples</module>` to `<modules></modules>` block, Then find `main` function of provider service and `RUN` it like any other Java Program.

3. Start the springmvc-consumer service

   Just like how to start springmvc-provider service. But the main class of springmvc-consumer service is `SpringmvcConsumerMain`. 

   ```bash
   cd samples/springmvc-sample/springmvc-consumer/
   mvn exec:java -Dexec.mainClass="org.apache.servicecomb.samples.springmvc.consumer.SpringmvcConsumerMain"
   ```

4. How to verify
   On the producer side, the output should contain the following stuffs if the producer starts up successfully:
   1. *'swagger: 2.0 info: version: 1.0.0 ...'* means the producer generated swagger contracts
   2. *'rest listen success. address=0.0.0.0:8080'* means the rest endpoint is listening on port 8080
   3. *'highway listen success. address=0.0.0.0:7070'* means the highway endpoint is listening on port 7070
   4. *'Register microservice instance success'* means the producer has registered successfully to service center
   
   On the consumer side, you can see the following outputs if the consumer can invoke the producer:
   1. *'RestTemplate consumer sayhi services: Hello Java Chassis'* means the consumer calls sayhi by RestTemplate successfully
   2. *'RestTemplate consumer sayhello services: Hello person ServiceComb/Java Chassis'* means the consumer calls sayhello by RestTemplate successfully
   3. *'POJO consumer sayhi services: Hello Java Chassis'* means the consumer calls sayhi by RpcReference successfully
   4. *'POJO consumer sayhello services: Hello person ServiceComb/Java Chassis'* means the consumer calls sayhello by RpcReference successfully
   ​
## More

[Develop with RestTemplate](http://servicecomb.incubator.apache.org/users/develop-with-rest-template/)