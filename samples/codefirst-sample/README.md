## Code First Sample

ServiceComb Java Chassis supports generating provider-service API implicitly. When the service is started, an API is automatically generated and registered to the service center.

When you develop a microservice in transparent RPC mode, the code does not show how you want to define an API, and all generated APIs are POST methods, The input parameters of all the methods will be packaged as a class and transferred as body parameters. Therefore, if you develop providers using implicit APIs, you are advised to choose Spring MVC or JAX-RS mode to obtain complete RESTful statements.

For detail information please refer to [Doc](http://servicecomb.incubator.apache.org/users/service-contract/)



## Sample Quick Start

1. Start ServiceComb/Service Center

   [how to start service center](http://servicecomb.incubator.apache.org/users/setup-environment/#)

2. Start codefirst-provider

3. Start codefirst-consumer

