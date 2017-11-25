package io.servicecomb.authentication.consumer;

import java.util.Optional;

import io.servicecomb.core.Const;
import io.servicecomb.core.Handler;
import io.servicecomb.core.Invocation;
import io.servicecomb.swagger.invocation.AsyncResponse;

/**
 * 
 * add token to context
 * Provider will get token for authentication
 *
 */
public class ConsumerAuthHandler implements Handler {

  private RSACoumserTokenManager athenticationTokenManager = new RSACoumserTokenManager();

  @Override
  public void handle(Invocation invocation, AsyncResponse asyncResp) throws Exception {

    Optional<String> token = Optional.ofNullable(athenticationTokenManager.getToken());
    if(!token.isPresent())
    {
      asyncResp.consumerFail(
          new Error("rejected by consumer authentication handler"));
    }
    invocation.addContext(Const.AUTH_TOKEN, token.get());
    invocation.next(asyncResp);
  }

  public void setAuthenticationTokenManager(RSACoumserTokenManager authenticationTokenManager) {
    this.athenticationTokenManager = authenticationTokenManager;
  }

}
