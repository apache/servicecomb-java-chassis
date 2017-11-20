package io.servicecomb.authentication;

import io.servicecomb.core.Const;
import io.servicecomb.core.Handler;
import io.servicecomb.core.Invocation;
import io.servicecomb.foundation.token.AuthenticationTokenManager;
import io.servicecomb.swagger.invocation.AsyncResponse;

public class ConsumerAuthHandler implements Handler {

	public AuthenticationTokenManager athenticationTokenManager = new RSACoumserTokenManager();

	@Override
	public void handle(Invocation invocation, AsyncResponse asyncResp) throws Exception {

		String token = athenticationTokenManager.getToken();
		invocation.addContext(Const.AUTH_TOKEN, token);
		invocation.next(asyncResp);
	}


}
