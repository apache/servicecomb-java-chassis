# Extend Metrics Health Checker Sample
## What is Health Checker
Because micro-services is desgin by SRP(Single Responsibility Principle),one micro-service always depend on many components such as middle-ware like queue,database etc and other micro-services,so if one micro-service report failure,we want fast know what is the cause and more details to help us solve the problem.

Health check mechanism can let user write some logic to check components of micro-service dependent and return check result.

## How to use
### Add your checker implementation
Implement *org.apache.servicecomb.foundation.metrics.health.HealthChecker* interface:  
```java
public interface HealthChecker {
  String getName();

  HealthCheckResult check();
}
```

Then add the implementation class into **META-INF.service.org.apache.servicecomb.foundation.metrics.health.HealthChecker** by SPI(Service Provider Interface) way

In this demo,we had make two checkers :
1. CustomHealthChecker    
  always return true checker   
2. MySqlHealthChecker    
  sim check mysql can connect checker  

### Add Dependency in POM
```xml
<dependency>
  <groupId>org.apache.servicecomb</groupId>
  <artifactId>metrics-core</artifactId>
  <version>{version}</version>
</dependency>
```

### Start service
```bash
mvn spring-boot:run
```

### Do check and get result
If you had config rest transport address in microservice.yaml like:
```yaml
cse:
  rest:
    address: 0.0.0.0:7777
```

Then you can invoke http://{serverip}:7777/health get summary check result : true or false.

**Only all registered health checker confirm isHealthy=true,the  summary check result will be true,otherwise false**

Also can invoke http://{serverip}:7777/health/details get details of each health checker reported.