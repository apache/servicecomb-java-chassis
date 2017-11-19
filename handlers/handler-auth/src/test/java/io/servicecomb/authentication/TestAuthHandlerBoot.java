package io.servicecomb.authentication;

import io.servicecomb.AuthHandlerBoot;
import io.servicecomb.core.BootListener;
import io.servicecomb.core.BootListener.BootEvent;
import io.servicecomb.foundation.token.RSAKeypair4Auth;

import org.junit.Assert;
import org.junit.Test;

public class TestAuthHandlerBoot {

	
	@Test
	public void testGenerateRSAKey()
	{
		AuthHandlerBoot authHandlerBoot = new AuthHandlerBoot();
		BootEvent bootEvent = new BootEvent();
		bootEvent.setEventType(BootListener.EventType.BEFORE_REGISTRY);
		authHandlerBoot.onBootEvent(bootEvent);
		Assert.assertNotNull(RSAKeypair4Auth.INSTANCE.getPrivateKey());
		Assert.assertNotNull(RSAKeypair4Auth.INSTANCE.getPublicKey());
	}
}
