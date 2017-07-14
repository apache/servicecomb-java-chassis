package io.servicecomb.demo.pojo.test;

import io.servicecomb.demo.CodeFirstPojoIntf;
import io.servicecomb.demo.helloworld.greeter.Hello;
import io.servicecomb.demo.server.Test;
import io.servicecomb.demo.smartcare.SmartCare;
import javax.inject.Inject;
import org.springframework.stereotype.Component;

@Component
public class PojoService {

  static Hello hello;
  static SmartCare smartCare;
  static io.servicecomb.demo.server.Test test;
  static CodeFirstPojoIntf codeFirst;

  @Inject
  public void setHello(Hello hello) {
    PojoService.hello = hello;
  }

  @Inject
  public void setSmartCare(SmartCare smartCare) {
    PojoService.smartCare = smartCare;
  }

  @Inject
  public void setTest(Test test) {
    PojoService.test = test;
  }

  @Inject
  public void setCodeFirst(CodeFirstPojoIntf codeFirst) {
    PojoService.codeFirst = codeFirst;
  }

}
