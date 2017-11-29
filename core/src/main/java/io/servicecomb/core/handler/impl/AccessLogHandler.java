package io.servicecomb.core.handler.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.servicecomb.core.Const;
import io.servicecomb.core.Handler;
import io.servicecomb.core.Invocation;
import io.servicecomb.swagger.invocation.AsyncResponse;

/**
 * access log printer handler
 */
public class AccessLogHandler implements Handler {
  private static final Logger LOGGER = LoggerFactory.getLogger("accesslog");

  @Override
  public void handle(Invocation invocation, AsyncResponse asyncResp) throws Exception {
    invocation.next(response -> {
      LOGGER.info("{} - {} \"{}\" {}",
          (String) invocation.getContext(Const.SRC_MICROSERVICE),
          (String) invocation.getContext(Const.SRC_INSTANCE),
          invocation.getInvocationQualifiedName(),
          response.getStatusCode());
      asyncResp.handle(response);
    });
  }
}
