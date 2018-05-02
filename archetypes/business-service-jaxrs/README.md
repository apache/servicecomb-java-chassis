## Welcome to use ServiceComb Java Chassis
This project(module) is generate by *org.apache.servicecomb.archetypes:business-service-jaxrs-archetype*, it use **jaxrs provider** to develop service producer.  

### More works can be done further:
1. Modify "HelloImpl", add your business service logic, or create some new producers to provide your services. More details can be found : http://servicecomb.incubator.apache.org/users/develop-with-jax-rs/
2. Modify "microservice.yaml", change APPLICATION_ID, service_description.name, version, and service center address, endpoints publish address etc. More details can be found : http://servicecomb.incubator.apache.org/users/service-definition/
3. Modify setting value of "mainClass" in pom.xml for package.

### Package your service
Under project(module) root folder, run 
```bash
mvn package
```
Then you can get outputs in target folder:   
- lib : contains all dependencies jars   
- xxxxxx-{version}.jar   
```bash
java -jar xxxxxx-{version}.jar
```
*Notice: If you need to modify configurations in "microservice.yaml" like service center address but don't want repackage the executable jar, **you can directly place a new "microservice.yaml" file under the same folder, then configurations will be overridden.***