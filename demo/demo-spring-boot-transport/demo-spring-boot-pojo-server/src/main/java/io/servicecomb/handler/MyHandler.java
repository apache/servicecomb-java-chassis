package io.servicecomb.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.servicecomb.core.Handler;
import io.servicecomb.core.Invocation;
import io.servicecomb.demo.server.User;
import io.servicecomb.swagger.invocation.AsyncResponse;

public class MyHandler implements Handler {

  private static final Logger LOGGER = LoggerFactory.getLogger(MyHandler.class);

  public static final String SPLITPARAM_RESPONSE_USER_SUFFIX = "(modified by MyHandler)";

  @Override
  public void handle(Invocation invocation, AsyncResponse asyncResp) throws Exception {
    LOGGER.info("If you see this log, that means this demo project has been converted to ServiceComb framework.");

    invocation.next(response -> {
      if (invocation.getOperationName().equals("splitParam")) {
        User user = response.getResult();
        user.setName(user.getName() + SPLITPARAM_RESPONSE_USER_SUFFIX);
        asyncResp.handle(response);
      } else {
        asyncResp.handle(response);
      }
    });
  }
}
