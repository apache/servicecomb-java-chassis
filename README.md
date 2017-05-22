# Java Chassis [![Build Status](https://travis-ci.org/ServiceComb/java-chassis.svg?branch=master)](https://travis-ci.org/ServiceComb/java-chassis.svg?branch=master)[![Coverage Status](https://coveralls.io/repos/github/WillemJiang/java-chassis/badge.svg?branch=master)](https://coveralls.io/github/WillemJiang/java-chassis?branch=master)
ServiceComb Java Chassis is a Software Development Kit (SDK) for rapid development of microservices in Java, providing service registration, service discovery, dynamic routing, and service management features

## Automated Testing
  To build the docker image and run the integration tests with docker, you can use maven docker profile 
  
      mvn clean install -Pdocker
      
  If you are using docker machine, please use the following command
  
      mvn clean install -Pdocker -Pdocker-machine
