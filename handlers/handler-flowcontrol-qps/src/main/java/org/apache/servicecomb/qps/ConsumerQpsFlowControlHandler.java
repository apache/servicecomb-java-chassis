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
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;

/**
 * For qps flow control on consumer side.
 * Support 3 levels of microservice/schema/operation.
 */
public class ConsumerQpsFlowControlHandler implements Handler {
  static final QpsControllerManager qpsControllerMgr = new QpsControllerManager()
      .setConfigKeyPrefix(Config.CONSUMER_LIMIT_KEY_PREFIX);

  @Override
  public void handle(Invocation invocation, AsyncResponse asyncResp) throws Exception {
    if (!Config.INSTANCE.isConsumerEnabled()) {
      invocation.next(asyncResp);
      return;
    }

    QpsController qpsController = qpsControllerMgr.getOrCreate(invocation.getMicroserviceName(), invocation);
    if (qpsController.isLimitNewRequest()) {
      // return http status 429
      CommonExceptionData errorData = new CommonExceptionData("rejected by qps flowcontrol");
      asyncResp.consumerFail(
          new InvocationException(QpsConst.TOO_MANY_REQUESTS_STATUS, errorData));
      return;
    }

    invocation.next(asyncResp);
  }
}
