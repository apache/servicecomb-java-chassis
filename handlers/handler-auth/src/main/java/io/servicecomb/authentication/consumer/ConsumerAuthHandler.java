package io.servicecomb.authentication.consumer;

import io.servicecomb.core.Const;
import io.servicecomb.core.Handler;
import io.servicecomb.core.Invocation;
import io.servicecomb.foundation.token.AuthenticationTokenManager;
import io.servicecomb.swagger.invocation.AsyncResponse;

import java.util.Optional;

/**
 * 
 * add token to context
 * Provider will get token for authentication
 *
 */
public class ConsumerAuthHandler implements Handler {

	private AuthenticationTokenManager athenticationTokenManager = new RSACoumserTokenManager();

	@Override
	public void handle(Invocation invocation, AsyncResponse asyncResp) throws Exception {

		String token = athenticationTokenManager.getToken();
		Optional.ofNullable(token).ifPresent(t -> invocation.addContext(Const.AUTH_TOKEN, t));
		invocation.next(asyncResp);
	}
	
	public void setAuthenticationTokenManager(AuthenticationTokenManager authenticationTokenManager)
	{
		this.athenticationTokenManager = authenticationTokenManager;
	}

}
