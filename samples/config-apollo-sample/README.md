## Use Apollo as Configuration Center

To use Apollo as configuration source in ServiceComb Java Chassis services:

* Start Apollo service and create a project to associate with Chassis service, then generate a token

  [How to use Apollo configuration center](http://servicecomb.incubator.apache.org/cn/users/dynamic-config/)


* Import `config-apollo` in pom:

  ```xml
  <dependency>
        <groupId>org.apache.servicecomb</groupId>
        <artifactId>config-apollo</artifactId>
   </dependency>
  ```

* Configurations for Apollo itself in `microservice.yaml`, for example:

  ```yaml
  apollo:
    config:
      serverUri: http://127.0.0.1:8070	#Apollo portal server address
      serviceName: apollo-test		#service name use AppId in apollo
      env: DEV				#default value DEV
      clusters: default		#default value default
      namespace: application		#default value application
      token: 				#get token from Apollo web pages
  ```

* Start Chassis service and update configurations in Apollo portal service.

  [Need to start service center first](http://servicecomb.incubator.apache.org/users/setup-environment/#)

  Compile the source code at root directory of ServiceComb Java Chassis, which is `incubator-servicecomb-java-chassis/`, and use `mvn exec` to execute the main class `MainServer`.

  ```bash
  cd incubator-servicecomb-java-chassis/
  mvn clean install -Psamples -DskipTests			#only need to install at first time.
  cd samples/config-apollo-sample/
  mvn exec:java -Dexec.mainClass="MainServer"
  ```

* Verify configurations can be configured dynamically.

  Before publishing a new configuration items from apollo portal, `MainServer` will just print `DynamicProperty: {name=timeout, current value=default}` 

  After `timeout` configuration is published with value `100`, `MainServer` will print `DynamicProperty: {name=timeout, current value=100}` 


## More

[Apollo Doc](https://github.com/ctripcorp/apollo/wiki)

[Use Apollo In ServiceComb](http://servicecomb.incubator.apache.org/cn/users/dynamic-config/)

