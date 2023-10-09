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

import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.filter.ConsumerFilter;
import org.apache.servicecomb.core.filter.Filter;
import org.apache.servicecomb.core.filter.FilterNode;
import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.springframework.core.env.Environment;

import com.google.common.annotations.VisibleForTesting;

public class ConsumerFlowControlFilter implements ConsumerFilter {
  private final QpsControllerManager qpsControllerMgr;

  public ConsumerFlowControlFilter(Environment environment) {
    qpsControllerMgr = new QpsControllerManager(false, environment);
  }

  @VisibleForTesting
  public QpsControllerManager getQpsControllerMgr() {
    return qpsControllerMgr;
  }

  @Override
  public int getOrder(InvocationType invocationType, String application, String serviceName) {
    return Filter.CONSUMER_LOAD_BALANCE_ORDER - 1990;
  }

  @Override
  public String getName() {
    return "consumer-flow-control";
  }

  @Override
  public CompletableFuture<Response> onFilter(Invocation invocation, FilterNode nextNode) {
    if (!Config.INSTANCE.isConsumerEnabled()) {
      return nextNode.onFilter(invocation);
    }

    QpsStrategy qpsStrategy = qpsControllerMgr.getOrCreate(invocation.getMicroserviceName(), invocation);
    if (qpsStrategy.isLimitNewRequest()) {
      CommonExceptionData errorData = new CommonExceptionData(
          "consumer request rejected by flow control.");
      return CompletableFuture.failedFuture(new InvocationException(QpsConst.TOO_MANY_REQUESTS_STATUS, errorData));
    }

    return nextNode.onFilter(invocation);
  }
}
