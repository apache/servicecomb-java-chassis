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

import org.apache.servicecomb.core.CoreConst;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.filter.Filter;
import org.apache.servicecomb.core.filter.FilterNode;
import org.apache.servicecomb.core.filter.ProviderFilter;
import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.springframework.core.env.Environment;

import com.google.common.annotations.VisibleForTesting;

public class ProviderFlowControlFilter implements ProviderFilter {
  private final QpsControllerManager qpsControllerMgr;

  public ProviderFlowControlFilter(Environment environment) {
    qpsControllerMgr = new QpsControllerManager(true, environment);
  }

  @Override
  public int getOrder(InvocationType invocationType, String application, String serviceName) {
    return Filter.PROVIDER_SCHEDULE_FILTER_ORDER - 1990;
  }

  @Override
  public String getName() {
    return "provider-flow-control";
  }

  @Override
  public CompletableFuture<Response> onFilter(Invocation invocation, FilterNode nextNode) {
    if (!Config.INSTANCE.isProviderEnabled()) {
      return nextNode.onFilter(invocation);
    }

    String microserviceName = invocation.getContext(CoreConst.SRC_MICROSERVICE);
    QpsStrategy qpsStrategy = qpsControllerMgr.getOrCreate(microserviceName, invocation);
    if (qpsStrategy.isLimitNewRequest()) {
      CommonExceptionData errorData = new CommonExceptionData(
          "provider request rejected by flow control.");
      return CompletableFuture.failedFuture(new InvocationException(QpsConst.TOO_MANY_REQUESTS_STATUS, errorData));
    }
    return nextNode.onFilter(invocation);
  }

  @VisibleForTesting
  public QpsControllerManager getQpsControllerMgr() {
    return qpsControllerMgr;
  }
}
