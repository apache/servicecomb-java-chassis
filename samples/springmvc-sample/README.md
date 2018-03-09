# Spring MVC Sample

`RestTemplate` is a RESTful API provide by the Spring framework. ServiceComb provides the API for service calling. Users can call microservices using customized URL and `RestTemplate` instance provided by ServiceComb regardless of the specific address of the service.

* The URL must be in format of ServiceComb: `cse://microserviceName/path?querystring`.
* During use of this URL format, the ServiceComb framework will perform internal microservice descovery, fallbreak, and fault tolerance and send the requests to the microservice providers.

## Sample Quick Start

1. Start the ServiceComb/Service Center

   [how to start the service center](http://servicecomb.incubator.apache.org/users/setup-environment/#)

2. Start the springmvc-provider

3. Start the springmvc-consumer

## More

[Develop with RestTemplate](http://servicecomb.incubator.apache.org/users/develop-with-rest-template/)