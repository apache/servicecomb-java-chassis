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

* Start ServiceComb/Service Center

  [how to start service center](http://servicecomb.incubator.apache.org/users/setup-environment/#)

* Start auth-provider

* Start auth-consumer
