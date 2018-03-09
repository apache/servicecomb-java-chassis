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

1. Start ServiceComb/Service Center

   [how to start service center](http://servicecomb.incubator.apache.org/users/setup-environment/#)

2. Start JAX-RS Provider

3. Start JAX-RS Consumer