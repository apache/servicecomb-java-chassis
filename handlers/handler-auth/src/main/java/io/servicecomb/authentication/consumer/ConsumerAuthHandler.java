package io.servicecomb.authentication.consumer;

import java.util.Optional;

import io.servicecomb.core.Const;
import io.servicecomb.core.Handler;
import io.servicecomb.core.Invocation;
import io.servicecomb.foundation.token.AuthenticationTokenManager;
import io.servicecomb.swagger.invocation.AsyncResponse;

/**
 * 
 * add token to context
 * Provider will get token for authentication
 *
 */
public class ConsumerAuthHandler implements Handler {

	public AuthenticationTokenManager athenticationTokenManager = new RSACoumserTokenManager();

	@Override
	public void handle(Invocation invocation, AsyncResponse asyncResp) throws Exception {

		String token = athenticationTokenManager.getToken();
		Optional.ofNullable(token).ifPresent(t -> invocation.addContext(Const.AUTH_TOKEN, t));
		invocation.next(asyncResp);
	}


}
