## Local registry

User can use local registry mechanisam and can connect server endpoints without using service center.
For local registry mechanism user should provide the local registry file at consumer side.
User can start provider service and get the provider schema file and put at the consumer side.
Consumer will use local registry function and it will connect the server end points.

Please find the details as follows.

## Provider Side

* run the provider with/without service center and get the schema file.

## Consumer Side changes

* Add local registry file registry.yaml :

  ```yaml
  localserv:
    - id: "100"
      version: "0.0.1"
      appid: localservreg
      schemaIds:
        - localservregistry
      instances:
        - endpoints:
          - rest://localhost:8080
          - highway://localhost:7070
  ```

* Add the following code at the beginning and end of the service consumer Main function

```java
    public class xxxClient {
      public static void main(String[] args) {
　　    System.setProperty("local.registry.file", "/yourpath/registry.yaml");
        //your code
　　    System.clearProperty("local.registry.file");
      }
    }
```
   If file present in this path "local.registry.file" then registry mode is Local registry mode. It will not connect to service center.

* Add provider's schema file at below location of consumer side

  src/main/resources/microservices/{microserviceName}/{schemaId.yaml}
  -here microserviceName is servers's microservice name. Like this multiple schemaId files we can put in same location.

* Run the consumer and verify the API's output.
