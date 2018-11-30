## Welcome to use ServiceComb Java Chassis
This project(module) is generate by *org.apache.servicecomb.archetypes:business-service-spring-boot-starter-archetype*, it use **spring-boot-starter-provider** to develop service producer.  

### More works can be done further:
1. Modify "HelloImpl", add your business service logic, or create some new producers to provide your services. More details can be found : http://servicecomb.apache.org/users/develop-with-spring-boot-starter/
2. Modify "microservice.yaml", change APPLICATION_ID, service_description.name, version, and service center address, endpoints publish address etc. More details can be found : http://servicecomb.apache.org/users/service-definition/

### Package your service
Under project(module) root folder, run 
```bash
mvn package
```
Then you can get executable jar in target/bin folder:   
- xxxxxx-{version}-exec.jar    
```bash
java -jar xxxxxx-{version}-exec.jar
```
*Notice: If you need to modify config setting in "microservice.yaml" like service center address but don't want repackage the executable jar, **you can direct place a new "microservice.yaml" file in same folder, then settings will be overridden.***

## Spring Boot and ServiceComb
### Why ServiceComb make integration with Spring Boot
Spring Boot can accelerate develop speed of Spring application, it provides these features:
* Can create independent executable Spring application
* Tomcat embedded, Jetty as Web server, and do not need package(war)
* Provide many starters in order to simplify maven dependency

Using Spring Boot in microservice development, can greatly simplifying configure and deploy. ServiceComb is a microservice framework with full functionality of service management, focus on rapidly development of microservices, so integration with Spring Boot can obtain greater advantages.

### How ServiceComb make integration with Spring Boot
Developers often use Spring Boot in the following way:
* Java application : import `spring-boot-starter` then develop general application, does not contain WEB
* Web application : import `spring-boot-starter-web` then develop web application, also include an embedded Tomcat or Jetty server, and use Spring Web MVC framework to develop REST endpoints

The first way, do not need any refactoring, directly startup ServiceComb via Spring Boot.

The second way is replace `Spring MVC DispatcherServlet` with `ServiceComb RestServlet`.