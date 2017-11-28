package io.servicecomb.authentication;

import io.servicecomb.authentication.provider.ProviderAuthHanlder;
import io.servicecomb.core.Const;
import io.servicecomb.core.Invocation;
import io.servicecomb.swagger.invocation.AsyncResponse;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class TestProviderAuthHanlder {
  Invocation invocation = null;

  AsyncResponse asyncResp = null;

  @Before
  public void setUp() throws Exception {
    invocation = Mockito.mock(Invocation.class);
    asyncResp = Mockito.mock(AsyncResponse.class);
    Mockito.when(invocation.getContext(Const.AUTH_TOKEN)).thenReturn("testtoken");
  }

  @Test
  public void testHandle() throws Exception {
    ProviderAuthHanlder providerAuthHanlder = new ProviderAuthHanlder();
    providerAuthHanlder.handle(invocation, asyncResp);
    Assert.assertTrue(true);
  }
}
