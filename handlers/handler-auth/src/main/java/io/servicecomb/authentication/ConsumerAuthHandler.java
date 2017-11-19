package io.servicecomb.authentication;

import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import io.servicecomb.core.Const;
import io.servicecomb.core.Handler;
import io.servicecomb.core.Invocation;
import io.servicecomb.foundation.token.AuthenticationTokenManager;
import io.servicecomb.swagger.invocation.AsyncResponse;

public class ConsumerAuthHandler implements Handler {

	@Autowired
	@Qualifier("coumserTokenManager")
	public AuthenticationTokenManager athenticationTokenManager;

	@Override
	public void handle(Invocation invocation, AsyncResponse asyncResp) throws Exception {

		String token = athenticationTokenManager.getToken();
		Assert.assertNotNull(token, "athentication token is required");
		invocation.addContext(Const.AUTH_TOKEN, token);
		invocation.next(asyncResp);
	}

	public void setAthenticationTokenManager(AuthenticationTokenManager athenticationTokenManager) {
		this.athenticationTokenManager = athenticationTokenManager;
	}

}
