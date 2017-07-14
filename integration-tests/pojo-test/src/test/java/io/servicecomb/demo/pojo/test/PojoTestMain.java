package io.servicecomb.demo.pojo.test;

import io.servicecomb.demo.CodeFirstPojoIntf;
import io.servicecomb.demo.helloworld.greeter.Hello;
import io.servicecomb.demo.server.Test;
import io.servicecomb.demo.smartcare.SmartCare;
import io.servicecomb.foundation.common.utils.BeanUtils;
import io.servicecomb.foundation.common.utils.Log4jUtils;
import javax.inject.Inject;
import org.springframework.stereotype.Component;

@Component
public class PojoTestMain {

  static Hello hello;
  static SmartCare smartCare;
  static io.servicecomb.demo.server.Test test;
  static CodeFirstPojoIntf codeFirst;

  public static void main(String[] args) throws Exception {
    Log4jUtils.init();
    BeanUtils.init();
  }

  @Inject
  public void setHello(Hello hello) {
    PojoTestMain.hello = hello;
  }

  @Inject
  public void setSmartCare(SmartCare smartCare) {
    PojoTestMain.smartCare = smartCare;
  }

  @Inject
  public void setTest(Test test) {
    PojoTestMain.test = test;
  }

  @Inject
  public void setCodeFirst(CodeFirstPojoIntf codeFirst) {
    PojoTestMain.codeFirst = codeFirst;
  }

}
