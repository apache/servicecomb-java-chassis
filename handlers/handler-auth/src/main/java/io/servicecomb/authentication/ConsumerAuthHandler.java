package io.servicecomb.authentication;

import io.servicecomb.core.Handler;
import io.servicecomb.core.Invocation;
import io.servicecomb.foundation.token.AuthenticationTokenManager;
import io.servicecomb.swagger.invocation.AsyncResponse;

import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;

public class ConsumerAuthHandler implements Handler {

	@Autowired
	public AuthenticationTokenManager athenticationTokenManager;
	
	@Override
	public void handle(Invocation invocation, AsyncResponse asyncResp)
			throws Exception {
		
		String token = athenticationTokenManager.getToken();
		Assert.assertNotNull(token, "athentication token is required");
		//TODO lwh set token in httprequest header
		invocation.next(asyncResp);
	}

	public void setAthenticationTokenManager(
			AuthenticationTokenManager athenticationTokenManager) {
		this.athenticationTokenManager = athenticationTokenManager;
	}



}
