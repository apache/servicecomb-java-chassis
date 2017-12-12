package io.servicecomb.core.handler.impl;

import static org.mockito.Mockito.verify;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import io.servicecomb.core.Invocation;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.ServiceRegistry;
import io.servicecomb.serviceregistry.registry.ServiceRegistryFactory;
import io.servicecomb.swagger.invocation.AsyncResponse;

public class TestAccessLogHandler {

  @BeforeClass
  public static void setUp() {
    ServiceRegistry serviceRegistry = ServiceRegistryFactory.createLocal();
    serviceRegistry.init();
    RegistryUtils.setServiceRegistry(serviceRegistry);
  }

  @Test
  public void testHandle() throws Exception {
    Invocation invocation = Mockito.mock(Invocation.class);
    AsyncResponse asyncResp = Mockito.mock(AsyncResponse.class);

    new AccessLogHandler().handle(invocation, asyncResp);

    verify(invocation).next(Mockito.anyObject());
  }
}
