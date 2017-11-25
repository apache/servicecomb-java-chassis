# Body Mass Index(BMI) Calculator Microservice Demo
## Architecture of BMI Calculator
There are two microservices in this demo.
* Webapp (API Gateway)
* BMI Calculator (computing service)

## Prerequisite
1. [Oracle JDK 1.8+](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html)
2. [Maven 3.x](https://maven.apache.org/install.html)

## Quick Start(Linux)
1. Run the service center
```bash
wget https://github.com/ServiceComb/service-center/releases/download/0.1.1/service-center-0.1.1-linux-amd64.tar.gz
tar xvf service-center-0.1.1-linux-amd64.tar.gz
(cd service-center-0.1.1-linux-amd64; bash start.sh)
```
2. Get the bmi demo's code
```bash
git clone https://github.com/ServiceComb/ServiceComb-Java-Chassis
cd ServiceComb-Java-Chassis/samples
```
3. Run microservices
* Run the **BMI calculator service**
```bash
(cd bmi/calculator; mvn spring-boot:run)
```
* Run the **webapp service**
```bash
(cd bmi/webapp; mvn spring-boot:run)
```
4. Visit the services via **<a>http://127.0.0.1:8889</a>**.