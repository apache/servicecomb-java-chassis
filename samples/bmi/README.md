# Body Mass Index(BMI) Calculator Microservice Demo
## Architecture of BMI Calculator
There are two microservices in this demo.
* Webapp (API Gateway)
* BMI Calculator (computing service)

## Prerequisite
1. [Oracle JDK 1.8+](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html)
2. [Maven 3.x](https://maven.apache.org/install.html)
3. [Gradle 4.x](https://gradle.org/install/)(Optional)

## Quick Start(Linux)
1. Run the service center

   [how to start service center](http://servicecomb.incubator.apache.org/users/setup-environment/#)

2. Get the bmi demo's code
```bash
git clone https://github.com/apache/incubator-servicecomb-java-chassis.git
cd incubator-servicecomb-java-chassis/samples
```
3. Run microservices
* via maven
   * Run the **BMI calculator service**
      ```bash
      (cd bmi/calculator; mvn spring-boot:run)
      ```
   * Run the **webapp service**
      ```bash
      (cd bmi/webapp; mvn spring-boot:run)
      ```
* via gradle
   * Install ServiceComb Java Chassis
      ```bash
      mvn clean install -DskipTests
      ```
   * Run the **BMI calculator service**
      ```bash
      (cd bmi/calculator; gradle bootRun)
      ```
   * Run the **webapp service**
      ```bash
      (cd bmi/webapp; gradle bootRun)
      ```
4. Visit the services via **<a>http://127.0.0.1:8889</a>**.
