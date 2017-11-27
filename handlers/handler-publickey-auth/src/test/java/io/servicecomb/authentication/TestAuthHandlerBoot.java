package io.servicecomb.authentication;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import io.servicecomb.AuthHandlerBoot;
import io.servicecomb.core.BootListener;
import io.servicecomb.core.BootListener.BootEvent;
import io.servicecomb.foundation.token.RSAKeypair4Auth;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.api.registry.Microservice;
import io.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
@RunWith(PowerMockRunner.class)
@PrepareForTest(RegistryUtils.class)
public class TestAuthHandlerBoot {


  @Test
  public void testGenerateRSAKey() {
    MicroserviceInstance microserviceInstance = new MicroserviceInstance();
    Microservice microservice = new Microservice();
    microservice.setIntance(microserviceInstance);
    PowerMockito.mockStatic(RegistryUtils.class);
    PowerMockito.when(RegistryUtils.getMicroservice()).thenReturn(microservice);
    PowerMockito.when(RegistryUtils.getMicroserviceInstance()).thenReturn(microserviceInstance);
    
    AuthHandlerBoot authHandlerBoot = new AuthHandlerBoot();
    BootEvent bootEvent = new BootEvent();
    bootEvent.setEventType(BootListener.EventType.BEFORE_REGISTRY);
    authHandlerBoot.onBootEvent(bootEvent);
    Assert.assertNotNull(RSAKeypair4Auth.INSTANCE.getPrivateKey());
    Assert.assertNotNull(RSAKeypair4Auth.INSTANCE.getPublicKey());
  }
}
