package io.servicecomb.authentication;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.context.event.ContextClosedEvent;

import io.servicecomb.core.CseApplicationListener;
import io.servicecomb.foundation.common.utils.BeanUtils;

public class BaseTest {

  
  @BeforeClass
  public static void init() throws Exception
  {
    AuthProviderMain.main(new String[0]);
  }
  
  @AfterClass
  public static void shutdown() throws Exception {
    CseApplicationListener cal = BeanUtils.getBean("io.servicecomb.core.CseApplicationListener");
    ContextClosedEvent event = new ContextClosedEvent(BeanUtils.getContext());
    cal.onApplicationEvent(event);
  }
}
