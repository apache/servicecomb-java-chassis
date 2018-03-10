## Auth-Sample

To make sure the security between the interfaces of services, users can enable `RSA Authorizaiton` by simple configuration as below.

## Consumer Service

* Add dependence in pom.xml file:

  ```xml
  <dependency>
        <groupId>org.apache.servicecomb</groupId>
        <artifactId>handler-publickey-auth</artifactId>
   </dependency>
  ```

* Add handler chain in microservice.yaml:

  ```yaml
  cse:
    # other configurations omitted
    handler:
      chain:
        Consumer:
          default: auth-consumer
  ```

## Provider Service

* Add dependence in pom.xml file:

  ```xml
  <dependency>
        <groupId>org.apache.servicecomb</groupId>
        <artifactId>handler-publickey-auth</artifactId>
   </dependency>
  ```

* Add handler chain in microservice.yaml:

  ```yaml
  cse:
    # other configurations omitted
    handler:
      chain:
        Consumer:
          default: auth-provider
  ```

## Sample Quick Start

Auth sample use `RestTemplate` to present RSA communication between provider and consumer.

1. Start the ServiceComb/Service Center

   - [how to start the service center](http://servicecomb.incubator.apache.org/users/setup-environment/#)
   - make sure service center address is configured correctly in `microservice.yaml` file

   ```yaml
   cse:
     service:
       registry:
         address: http://127.0.0.1:30100		#service center address
   ```

2. Start the auth-provider service

   - Start provider service by maven

     Compile the source code at root directory of ServiceComb Java Chassis, which is `incubator-servicecomb-java-chassis/`, and use `mvn exec` to execute the main class `AuthProviderMain`.

     ```bash
     cd incubator-servicecomb-java-chassis/
     mvn clean install -Psamples -DskipTests			#only need to install at first time.
     cd samples/auth-sample/auth-provider/
     mvn exec:java -Dexec.mainClass="org.apache.servicecomb.samples.springmvc.provider.AuthProviderMain"
     ```

   - Start provider service by IDE

     Import the project by InteliJ IDEA or Eclipse, add sample module to pom.xml file in root module `incubator-servicecomb-java-chassis/pom.xml`, and add `<module>samples</module>` to `<modules></modules>` block, Then find `main` function `AuthProviderMain` of provider service and `RUN` it like any other Java program.

3. Start the auth-consumer service

   Just like how to start auth-provider service. But the main class of auth-consumer service is `AuthConsumerMain`. 

   ```bash
   cd samples/auth-sample/auth-consumer/
   mvn exec:java -Dexec.mainClass="org.apache.servicecomb.samples.springmvc.consumer.AuthConsumerMain"
   ```

   ​