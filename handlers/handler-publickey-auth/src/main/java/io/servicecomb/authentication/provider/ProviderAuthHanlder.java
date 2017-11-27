package io.servicecomb.authentication.provider;

import io.servicecomb.core.Const;
import io.servicecomb.core.Handler;
import io.servicecomb.core.Invocation;
import io.servicecomb.swagger.invocation.AsyncResponse;
import io.servicecomb.swagger.invocation.context.HttpStatus;
import io.servicecomb.swagger.invocation.exception.InvocationException;

public class ProviderAuthHanlder implements Handler {

  private RSAProviderTokenManager authenticationTokenManager = new RSAProviderTokenManager();

  @Override
  public void handle(Invocation invocation, AsyncResponse asyncResp) throws Exception {

    String token = invocation.getContext(Const.AUTH_TOKEN);
    if (null != token && authenticationTokenManager.valid(token)) {
      invocation.next(asyncResp);
    } else {
      asyncResp.producerFail(new InvocationException(new HttpStatus(401, "UNAUTHORIZED"), "UNAUTHORIZED"));
    }

  }

}
