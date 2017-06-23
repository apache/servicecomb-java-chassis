# Custom Handler Sample

It's the sample of customer handler sample

It will introduce how to write a customer handler 


### 1. Implements AbstractHandler
```java
public class MyHandler extends AbstractHandler {
    @Override
    public void handle(Invocation invocation, AsyncResponse asyncResp) throws Exception {
        // code before

        invocation.next(response -> {
            // code after
            asyncResp.handle(response);
        });
    }
}
```

### 2. Add config config/cse.handler.xml of handler 

```xml
<config>
    <handler id="myhandler" class="xxx.xxx.xxx.MyHandler" />
</config>
```

### 3 add handler in microservice.yaml

```yaml
cse:
  handler:
    chain:
      Provider:
        default: bizkeeper-provider,tracing-provider,sla-provider,perf-stats,myhandler
```

## How to run sample

### 1. Start the ServiceComb/Service Center

[how to start the service center](http://servicecomb.io/docs/start-sc/)

### 2.Start the Microservice server

```bash
mvn test -Pserver
```

### 3.Start the Microservice client

```bash
mvn test -Pclient
```