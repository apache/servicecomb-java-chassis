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
import org.apache.servicecomb.match.RequestMarkHandler;
import org.apache.servicecomb.match.policy.RateLimitingPolicy;
import org.apache.servicecomb.match.service.PolicyService;
import org.apache.servicecomb.match.service.PolicyServiceImpl;
import org.apache.servicecomb.qps.strategy.LeakyBucketStrategy;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.springframework.util.StringUtils;

public class ProviderQpsFlowControlHandler implements Handler {
  static final QpsControllerManager qpsControllerMgr = new QpsControllerManager()
      .setLimitKeyPrefix(Config.PROVIDER_LIMIT_KEY_PREFIX)
      .setBucketKeyPrefix(Config.PROVIDER_BUCKET_KEY_PREFIX)
      .setGlobalQpsStrategy(Config.PROVIDER_LIMIT_KEY_GLOBAL, Config.PROVIDER_BUCKET_KEY_GLOBAL);

  private PolicyService policyService = new PolicyServiceImpl();

  private LeakyBucketStrategy qpsStrategy = new LeakyBucketStrategy();

  @Override
  public void handle(Invocation invocation, AsyncResponse asyncResp) throws Exception {
    // The real executing position of this handler is no longer in handler chain, but in AbstractRestInvocation.
    // Therefore, the Invocation#next() method should not be called below.
    if (!Config.INSTANCE.isProviderEnabled()) {
      return;
    }

    if (isRateLimiting(invocation, asyncResp)) {
      CommonExceptionData errorData = new CommonExceptionData("rejected by qps flowcontrol");
      asyncResp.producerFail(new InvocationException(QpsConst.TOO_MANY_REQUESTS_STATUS, errorData));
    }
  }

  private boolean isRateLimiting(Invocation invocation, AsyncResponse asyncResp) throws Exception {
    String match = invocation.getContext().get(RequestMarkHandler.MARK_KEY);
    RateLimitingPolicy ratePolicy = (RateLimitingPolicy) policyService.getCustomPolicy("RateLimiting", match);
    if (ratePolicy == null) {
      return oldProcess(invocation, asyncResp);
    }
    if (invocation.getHandlerIndex() > 1) {
      invocation.next(asyncResp);
      return false;
    }
    qpsStrategy.setQpsLimit(ratePolicy.getRate().longValue());
    return qpsStrategy.isLimitNewRequest();
  }

  private boolean oldProcess(Invocation invocation, AsyncResponse asyncResp) throws Exception {
    if (invocation.getHandlerIndex() > 0) {
      invocation.next(asyncResp);
      return false;
    }
    String microserviceName = invocation.getContext(Const.SRC_MICROSERVICE);
    QpsStrategy qpsStrategy =
        StringUtils.isEmpty(microserviceName)
            ? qpsControllerMgr.getGlobalQpsStrategy()
            : qpsControllerMgr.getOrCreate(microserviceName, invocation);
    return qpsStrategy.isLimitNewRequest();
  }
}
