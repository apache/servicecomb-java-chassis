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

package org.apache.servicecomb.demo.springboot.pojo.server.handler;

import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.filter.Filter;
import org.apache.servicecomb.core.filter.FilterNode;
import org.apache.servicecomb.core.filter.ProviderFilter;
import org.apache.servicecomb.demo.springboot.pojo.server.schema.server.User;
import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.apache.servicecomb.swagger.invocation.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MyHandler implements ProviderFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(MyHandler.class);

  public static final String SPLITPARAM_RESPONSE_USER_SUFFIX = "(modified by MyHandler)";

  @Override
  public int getOrder(InvocationType invocationType, String application, String serviceName) {
    return Filter.PROVIDER_SCHEDULE_FILTER_ORDER - 100;
  }

  @Override
  public String getName() {
    return "test-my-filter";
  }

  @Override
  public CompletableFuture<Response> onFilter(Invocation invocation, FilterNode nextNode) {
    LOGGER.info("If you see this log, that means this demo project has been converted to ServiceComb framework.");

    return nextNode.onFilter(invocation).whenComplete((response, throwable) -> {
      if (invocation.getOperationMeta().getSchemaQualifiedName().equals("server.splitParam")) {
        User user = response.getResult();
        user.setName(user.getName() + SPLITPARAM_RESPONSE_USER_SUFFIX);
      }
    });
  }
}
