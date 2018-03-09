## Customized Handler Sample

ServiceComb support users to define a customized handler and and invoke the handler in handler chain.

* Customize a handler by implement Handler interface, for example:

  ```java
  public class MyHandler implements Handler {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyHandler.class);
    @Override
    public void handle(Invocation invocation, AsyncResponse asyncResponse) throws Exception {
      //code before
      LOGGER.info("It's my handler! \r\n");
      invocation.next(response -> {
        // code after
        asyncResponse.handle(response);
      });
    }
  }
  ```

* Define handler `id` and `class` in `cse.handler.xml` `config` item under `resources/config` directory

  ```xml
  <config>
      <handler id="myhandler" 		     class="org.apache.servicecomb.samples.customerhandler.handlers.MyHandler" />
  </config>

  ```

* Configure customized `MyHandler` in `microservice.yaml` file along with other handler together

  ```yaml
  cse:
    # other configurations omitted
    handler:
      chain:
        Consumer:
          default: bizkeeper-consumer,loadbalance, myhandler
  ```

## Sample Quick Start

1. Start ServiceComb/Service Center

   [how to start service center](http://servicecomb.incubator.apache.org/users/setup-environment/#)

2. Start custom-handler-provider

3. Start custom-handler-consumer

