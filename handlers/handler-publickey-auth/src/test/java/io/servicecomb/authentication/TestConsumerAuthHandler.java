package io.servicecomb.authentication;

import io.servicecomb.authentication.consumer.ConsumerAuthHandler;
import io.servicecomb.authentication.consumer.RSACoumserTokenManager;
import io.servicecomb.core.Invocation;
import io.servicecomb.swagger.invocation.AsyncResponse;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class TestConsumerAuthHandler {

  Invocation invocation = null;

  AsyncResponse asyncResp = null;

  RSACoumserTokenManager tokenManager = null;

  @Test
  public void testHandler() throws Exception {
    tokenManager = Mockito.mock(RSACoumserTokenManager.class);
    Mockito.when(tokenManager.getToken()).thenReturn("testtoken");
    ConsumerAuthHandler consumerAuthHandler = new ConsumerAuthHandler();
    consumerAuthHandler.setAuthenticationTokenManager(tokenManager);
    consumerAuthHandler.handle(invocation, asyncResp);
    Assert.assertTrue(true);
  }
  
  
  @Test
  public void testHandlerException() throws Exception {
    tokenManager = Mockito.mock(RSACoumserTokenManager.class);
    Mockito.when(tokenManager.getToken()).thenReturn(null);
    ConsumerAuthHandler consumerAuthHandler = new ConsumerAuthHandler();
    consumerAuthHandler.setAuthenticationTokenManager(tokenManager);
    consumerAuthHandler.handle(invocation, asyncResp);
    
  }

  @Before
  public void setUp() throws Exception {
    invocation = Mockito.mock(Invocation.class);
    asyncResp = Mockito.mock(AsyncResponse.class);

   
  }
}
