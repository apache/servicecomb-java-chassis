## ServiceComb-Kie Client for Java

   Sample Java client for ServiceComb-Kie HTTP OPENAPI. If you want more information about the ServiceComb-Kie, go [here](https://github.com/apache/servicecomb-kie).
   
### Build & Install
   
   local Build from source:
   ```
    maven clean install
   ```
   
   add dependency to maven:
   ```
    <dependency>
        <groupId>org.apache.servicecomb</groupId>
        <artifactId>kie-client</artifactId>
        <version>2.0.0-SNAPSHOT</version>
    </dependency>
   ```


### Basic Usage

#### Case 1: Setting host and port for connecting server 
```java
//setting host= 192.168.88.229, port = 30111 and unset projectName
KieClient kieClient = new KieClient("192.168.88.299",30111,null);

//unsetting host,port,projectName and use default
KieClient kieClient = new KieClient();
```

#### Case 2: Create value of a key
```java
KieClient kieClient = new KieClient();

//state a KVBody Object
KVBody kvBody = new KVBody();
kvBody.setValue("testValue");
Map<String, String> labels = new HashMap<>();
labels.put("app", "111");
kvBody.setLabels(labels);

//create key-value
kieClient1.putKeyValue("test",kvBody)  
```

#### Case 3: Get value of a key
```java
KieClient kieClient = new KieClient();

//get key-value by key
List<KVResponse> kvResponses = kieClient.getValueOfKey("test");
```

#### Case 4: Search key-value by lables
```java
KieClient kieClient = new KieClient();

//state a Map<String,String> as labels
Map<String, String> labels = new HashMap<>();
labels.put("app", "111");

//get key-value by labels
List<KVResponse> searchKVResponses = kieClient.searchKeyValueByLabels(labels);
```

#### Case 5: Delete key-value
```java
KieClient kieClient = new KieClient(kieRawClient);

//get key-value by key
List<KVResponse> kvResponses = kieClient.getValueOfKey("test");

//delete all key-value of key
for(KVResponse kvResponse : kvResponses){
  for (KVDoc kvDoc : kvResponse.getData()){
    kieClient.deleteKeyValue(kvDoc);
  }
}
```

### More development

- Support for SSL authentication
- Implement dynamic config

### Contact
Bugs/Feature : [issues](https://github.com/apache/servicecomb-java-chassis/issues)
