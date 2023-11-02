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
package org.apache.servicecomb.demo.edge.service.encrypt.filter;

import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.filter.AbstractFilter;
import org.apache.servicecomb.core.filter.EdgeFilter;
import org.apache.servicecomb.core.filter.Filter;
import org.apache.servicecomb.core.filter.FilterNode;
import org.apache.servicecomb.demo.edge.service.EdgeConst;
import org.apache.servicecomb.demo.edge.service.encrypt.EncryptContext;
import org.apache.servicecomb.swagger.invocation.Response;
import org.springframework.stereotype.Component;

@Component
public class UserIdFilter extends AbstractFilter implements EdgeFilter {
  @Override
  public int getOrder() {
    return Filter.CONSUMER_LOAD_BALANCE_ORDER - 1790;
  }

  @Override
  public String getName() {
    return "test-edge-user-id";
  }

  @Override
  public CompletableFuture<Response> onFilter(Invocation invocation, FilterNode nextNode) {
    EncryptContext encryptContext = (EncryptContext) invocation.getHandlerContext().get(EdgeConst.ENCRYPT_CONTEXT);
    if (encryptContext == null) {
      return nextNode.onFilter(invocation);
    }

    String userId = encryptContext.getUserId();
    if (userId != null) {
      invocation.getRequestEx().setParameter("userId", userId);
    }

    return nextNode.onFilter(invocation);
  }
}
