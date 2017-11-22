package io.servicecomb.authentication;

import io.servicecomb.authentication.consumer.ConsumerAuthHandler;
import io.servicecomb.core.Invocation;
import io.servicecomb.foundation.common.utils.RSAUtils;
import io.servicecomb.foundation.token.RSAKeypair4Auth;
import io.servicecomb.swagger.invocation.AsyncResponse;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class TestConsumerAuthHandler {

	Invocation invocation = null;
	AsyncResponse asyncResp = null;

	@Test
	public void testHandler() throws Exception {

		ConsumerAuthHandler consumerAuthHandler = new ConsumerAuthHandler();
		consumerAuthHandler.handle(invocation, asyncResp);
		Assert.assertTrue(true);
	}

	@Before
	public void setUp() throws Exception {
		invocation = Mockito.mock(Invocation.class);
		asyncResp = Mockito.mock(AsyncResponse.class);
		String[] privAndPubKey = RSAUtils.getEncodedKeyPair();
		RSAKeypair4Auth.INSTANCE.setPrivateKey(privAndPubKey[0]);
		RSAKeypair4Auth.INSTANCE.setPublicKey(privAndPubKey[1]);
	}
}
