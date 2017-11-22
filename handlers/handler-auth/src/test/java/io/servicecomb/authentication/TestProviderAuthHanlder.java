package io.servicecomb.authentication;

import io.servicecomb.authentication.consumer.RSACoumserTokenManager;
import io.servicecomb.authentication.provider.ProviderAuthHanlder;
import io.servicecomb.core.Const;
import io.servicecomb.core.Invocation;
import io.servicecomb.foundation.common.utils.RSAUtils;
import io.servicecomb.foundation.token.RSAKeypair4Auth;
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
		String[] privAndPubKey = RSAUtils.getEncodedKeyPair();
		RSAKeypair4Auth.INSTANCE.setPrivateKey(privAndPubKey[0]);
		RSAKeypair4Auth.INSTANCE.setPublicKey(privAndPubKey[1]);
		String token = new RSACoumserTokenManager().createToken();
		Mockito.when(invocation.getContext(Const.AUTH_TOKEN)).thenReturn(token);
	}
	
	@Test
	public void testHandle() throws Exception
	{
		ProviderAuthHanlder providerAuthHanlder = new ProviderAuthHanlder();
		providerAuthHanlder.handle(invocation, asyncResp);
		Assert.assertTrue(true);
	}
}
