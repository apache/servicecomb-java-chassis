# About Deployment Module

Deployment is a mechanism to manage how microservics interact with external services. When running a microservice in a Cloud Native environment, it will interact with many external services, like service center, config center, an APM service to report tracing data, a dashboard service to report health data, etc. Deployment Service manages the meta data of these services. Deployment queries the addresses , the parameters, the authentication information of these services and so on. 

Deployment Service is some kind of service like service center, they are differ from two aspects:
1. All microservics will report their information to service center. But Deployment Service acts as a management system and knows the meta information in deploy time. 
2. Service center provide some other functions like instance management and heartbeat, while Deployment Service only provides metadata query services and it is simple. 

This module does not provide a Deployment Service, it provides the interface to interacts with Deployment Service. Service providers can implement the interface. 

They are some design constraints need to be considered when implement Deployment interface:
1. Deployment can only read configurations of environment, microservics.yaml. It can not read dynamic configurations.
2. Deployment is initialized before bean initialization. 
