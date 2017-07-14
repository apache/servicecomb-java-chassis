package io.servicecomb.demo.pojo.test;

import io.servicecomb.demo.CodeFirstPojoIntf;
import io.servicecomb.demo.helloworld.greeter.Hello;
import io.servicecomb.demo.server.Test;
import io.servicecomb.demo.smartcare.SmartCare;
import io.servicecomb.provider.pojo.RpcReference;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class SomePojoConfig {

  @RpcReference(microserviceName = "pojo", schemaId = "hello")
  private Hello hello;

  @RpcReference(microserviceName = "pojo", schemaId = "smartcare")
  private SmartCare smartCare;

  @RpcReference(microserviceName = "pojo", schemaId = "server")
  private Test test;

  @RpcReference(microserviceName = "pojo", schemaId = "codeFirst")
  private CodeFirstPojoIntf codeFirst;

  @Bean
  Hello hello() {
    return hello;
  }

  @Bean
  SmartCare smartCare() {
    return smartCare;
  }

  @Bean
  Test test() {
    return test;
  }

  @Bean
  CodeFirstPojoIntf codeFirst() {
    return codeFirst;
  }
}
