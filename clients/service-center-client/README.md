## ServiceComb-Service-Center Client for Java

   Sample Java client for ServiceComb-Service-Center HTTP API.   
   If you want more information about the ServiceComb-Service-Center HTTP API, go [here](https://github.com/apache/servicecomb-service-center/blob/master/docs/openapi/v4.yaml).
   
### Build & Install
   
   local Build from source :
   ```
    maven clean install
   ```
   
   add dependency to maven
   ```
    <dependency>
        <groupId>org.apache.servicecomb</groupId>
        <artifactId>service-center-client</artifactId>
    </dependency>
   ```


### Basic Usage

#### Case 1: Health check
```Java
ServiceCenterClient client = new ServiceCenterClient();

//get service-center instance
MicroserviceInstancesResponse instances = client.getServiceCenterInstances();
```

#### Case 2: Register microservice
```Java
ServiceCenterClient client = new ServiceCenterClient();

// state Microservice object, and you have to set a serviceName
Microservice microservice = new Microservice();
microservice.setServiceName("Test");
microservice.setServiceId("111111");

//register microservice
String response = client.registerMicroservice(microservice);
```

#### Case 3: Get microservice list
```Java
ServiceCenterClient client = new ServiceCenterClient();

//get services lists
MicroservicesResponse services = client.getMicroserviceList();
```

#### Case 4: Register service instance
```Java
ServiceCenterClient client = new ServiceCenterClient();

//state MicroserviceInstance object, you have to set existed serviceId
MicroserviceInstance instance = new MicroserviceInstance();
instance.setServiceId("222222");

//register service instanceId, return instanceId
String instanceId = client.registerMicroserviceInstance(instance, "222222");
```

#### Case 5: Get service instances list
```Java
ServiceCenterClient client = new ServiceCenterClient();

// get instances list with servcieId being "222222"
MicroserviceInstancesResponse instances = client.getMicroserviceInstanceList("222222");
```

#### Case 6: Heartbeats
```Java
ServiceCenterClient client = new ServiceCenterClient();

//all services and all instances send heartbeats
MicroservicesResponse services = client.getMicroserviceList();
for(Microservice microservice : services.getServices())
{
  for (MicroserviceInstance instance: client.getMicroserviceInstanceList(microservice.getServiceId()).getInstances())
  { 
    client.sendHeartBeats(new HeartbeatsRequest(microservice.getServiceId(),instance.getInstanceId())); 
  }
}
```

#### Other API
You can see client API code and tests, go [here](https://github.com/apache/servicecomb-java-chassis/blob/master/clients/service-center-client/src/main/java/org/apache/servicecomb/service/center/client/ServiceCenterClient.java) 


### More development

- Support All Service-center HTTP API

### Contact
Bugs/Feature : [issues](https://github.com/apache/servicecomb-java-chassis/issues)
