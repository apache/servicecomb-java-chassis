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
import org.apache.servicecomb.qps.strategy.FixedWindowStrategy;
import org.apache.servicecomb.qps.strategy.LeakyBucketStrategy;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProviderQpsFlowControlHandler implements Handler {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProviderQpsFlowControlHandler.class);

  private final QpsControllerManager qpsControllerMgr = new QpsControllerManager(true);

  private final Long qpsLimit = qpsControllerMgr.getGlobalQpsStrategy().getQpsLimit();

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
    QpsStrategy qpsStrategy = qpsControllerMgr.getOrCreate(microserviceName, invocation);
    isLimitNewRequest(qpsStrategy, asyncResp);
  }

  private boolean isLimitNewRequest(QpsStrategy qpsStrategy, AsyncResponse asyncResp) {
    String name = qpsStrategy.name();
    if (qpsStrategy.isLimitNewRequest()) {
      long tps = 0;
      if ("FixedWindow".equals(name)) {
        FixedWindowStrategy windowStrategy = (FixedWindowStrategy) qpsControllerMgr.getGlobalQpsStrategy();
        tps = windowStrategy.getRequestCount().longValue() - windowStrategy.getLastRequestCount() + 1;
      } else {
        LeakyBucketStrategy bucketStrategy = (LeakyBucketStrategy) qpsControllerMgr.getGlobalQpsStrategy();
        tps = bucketStrategy.getRequestCount().longValue();
      }
      LOGGER.warn("provider qps flowcontrol open, qpsLimit is {} and tps is {}", qpsLimit, tps);
      CommonExceptionData errorData = new CommonExceptionData(
          "provider request rejected by qps flowcontrol, qpsLimit is " + qpsLimit + " tps is " + tps);
      asyncResp.producerFail(new InvocationException(QpsConst.TOO_MANY_REQUESTS_STATUS, errorData));
      return true;
    } else {
      return false;
    }
  }
}
