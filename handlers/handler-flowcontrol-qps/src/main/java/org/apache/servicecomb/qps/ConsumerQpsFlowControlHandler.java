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

import org.apache.servicecomb.core.Handler;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.qps.strategy.FixedWindowStrategy;
import org.apache.servicecomb.qps.strategy.LeakyBucketStrategy;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For qps flow control on consumer side.
 * Support 3 levels of microservice/schema/operation.
 */
public class ConsumerQpsFlowControlHandler implements Handler {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerQpsFlowControlHandler.class);

  private final QpsControllerManager qpsControllerMgr = new QpsControllerManager(false);

  private final Long qpsLimit = qpsControllerMgr.getGlobalQpsStrategy().getQpsLimit();

  @Override
  public void handle(Invocation invocation, AsyncResponse asyncResp) throws Exception {
    if (!Config.INSTANCE.isConsumerEnabled()) {
      invocation.next(asyncResp);
      return;
    }

    QpsStrategy qpsStrategy = qpsControllerMgr.getOrCreate(invocation.getMicroserviceName(), invocation);
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
      // return http status 429
      LOGGER.warn("consumer qps flowcontrol open, qpsLimit is {} and tps is {}", qpsLimit, tps);
      CommonExceptionData errorData = new CommonExceptionData(
          "consumer request rejected by qps flowcontrol, qpsLimit is " + qpsLimit + " and tps is " + tps);
      asyncResp.consumerFail(
          new InvocationException(QpsConst.TOO_MANY_REQUESTS_STATUS, errorData));
      return;
    }

    invocation.next(asyncResp);
  }
}
