/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.servicecomb.qps;

import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.Handler;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.springframework.util.StringUtils;

public class ProviderQpsFlowControlHandler implements Handler {
  static final QpsControllerManager qpsControllerMgr = new QpsControllerManager()
      .setLimitKeyPrefix(Config.PROVIDER_LIMIT_KEY_PREFIX)
      .setBucketKeyPrefix(Config.PROVIDER_BUCKET_KEY_PREFIX)
      .setGlobalQpsStrategy(Config.PROVIDER_LIMIT_KEY_GLOBAL, Config.PROVIDER_BUCKET_KEY_GLOBAL);

  @Override
  public void handle(Invocation invocation, AsyncResponse asyncResp) throws Exception {
    if (invocation.getHandlerIndex() > 0) {
      // handlerIndex > 0, which means this handler is executed in handler chain.
      // As this flow control logic has been executed in advance, this time it should be ignored.
      invocation.next(asyncResp);
      return;
    }

    // The real executing position of this handler is no longer in handler chain, but in AbstractRestInvocation.
    // Therefore, the Invocation#next() method should not be called below.
    if (!Config.INSTANCE.isProviderEnabled()) {
      return;
    }

    String microserviceName = invocation.getContext(Const.SRC_MICROSERVICE);
    QpsStrategy qpsStrategy =
        StringUtils.isEmpty(microserviceName)
            ? qpsControllerMgr.getGlobalQpsStrategy()
            : qpsControllerMgr.getOrCreate(microserviceName, invocation);
    isLimitNewRequest(qpsStrategy, asyncResp);
  }

  private boolean isLimitNewRequest(QpsStrategy qpsStrategy, AsyncResponse asyncResp) {
    if (qpsStrategy.isLimitNewRequest()) {
      CommonExceptionData errorData = new CommonExceptionData("rejected by qps flowcontrol");
      asyncResp.producerFail(new InvocationException(QpsConst.TOO_MANY_REQUESTS_STATUS, errorData));
      return true;
    } else {
      return false;
    }
  }
}
