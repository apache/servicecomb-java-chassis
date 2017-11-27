package io.servicecomb.authentication;

import io.servicecomb.AuthHandlerBoot;
import io.servicecomb.core.BootListener;
import io.servicecomb.core.BootListener.BootEvent;
import io.servicecomb.foundation.token.RSAKeypair4Auth;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.api.registry.Microservice;
import io.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import mockit.Expectations;

import org.junit.Assert;
import org.junit.Test;
public class TestAuthHandlerBoot {


  @Test
  public void testGenerateRSAKey() {
    MicroserviceInstance microserviceInstance = new MicroserviceInstance();
    Microservice microservice = new Microservice();
    microservice.setIntance(microserviceInstance);
    new Expectations(RegistryUtils.class)
    {
    	{
    		
    		RegistryUtils.getMicroserviceInstance();
    		result = microserviceInstance;
    	}
    };
    
    AuthHandlerBoot authHandlerBoot = new AuthHandlerBoot();
    BootEvent bootEvent = new BootEvent();
    bootEvent.setEventType(BootListener.EventType.BEFORE_REGISTRY);
    authHandlerBoot.onBootEvent(bootEvent);
    Assert.assertNotNull(RSAKeypair4Auth.INSTANCE.getPrivateKey());
    Assert.assertNotNull(RSAKeypair4Auth.INSTANCE.getPublicKey());
  }
}
