# Write Metrics Data into Separate Text Files Sample
## Purpose
This sample show how direct get metrics data and write them into separate text files,then other monitor system can load this file in order to get run state of micro-service.

## What Contains
### metrics-write-file Module
This module contains the code how poll Monitors data from MonitorManager and convert data to a special format,then write into files.

**We had designed that will use common Logging component for write file**,and there are many logging implements like log4j,log4j2,logback etc...,so we create *MetricsFileWriter* interface for later autowire.

### metrics-write-file-log4j2-springboot Module
This module contain log4j2 MetricsFileWriter implement if your project use it as Logging component.

### metrics-write-file-log4j-springboot Module
This module contain log4j MetricsFileWriter implement if your project use it as Logging component.

## How to use
Startup ServiceApplication in  metrics-write-file-log4j2-springboot or metrics-write-file-log4j-springboot

1. If start service by maven
```bash
mvn spring-boot:run
```
you can see metric files had generated in **samples/metrics-write-file-sample/metrics-write-file-log4j(log4j2)-springboot/target/metric** folder,open your browser and make a request to http://localhost:8080/f ,wait a moment then you can see invocation metric files also be generated.

2. If start service by IDE   

you can see metric files had generated in **target/metric** folder,open your browser and make a request to http://localhost:8080/f ,wait a moment then you can see invocation metric files also be generated.
