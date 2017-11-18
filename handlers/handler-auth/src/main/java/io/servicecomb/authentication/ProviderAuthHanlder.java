package io.servicecomb.authentication;

import org.springframework.beans.factory.annotation.Autowired;

import io.servicecomb.core.Const;
import io.servicecomb.core.Handler;
import io.servicecomb.core.Invocation;
import io.servicecomb.foundation.token.AuthenticationTokenManager;
import io.servicecomb.swagger.invocation.AsyncResponse;
import io.servicecomb.swagger.invocation.context.HttpStatus;
import io.servicecomb.swagger.invocation.exception.InvocationException;

public class ProviderAuthHanlder implements Handler {

	@Autowired
	private AuthenticationTokenManager authenticationTokenManager;

	@Override
	public void handle(Invocation invocation, AsyncResponse asyncResp) throws Exception {

		String token = invocation.getContext(Const.AUTH_TOKEN);
		if (authenticationTokenManager.vaild(token)) {
			invocation.next(asyncResp);
		} else {
			asyncResp.producerFail(
					new InvocationException(new HttpStatus(401, "UNAUTHORIZED"), "reject by authentication"));
		}

	}

	public void setAuthenticationTokenManager(AuthenticationTokenManager authenticationTokenManager) {
		this.authenticationTokenManager = authenticationTokenManager;
	}

}
