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

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.common.rest.codec.RestObjectMapperFactory;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.filter.ConsumerFilter;
import org.apache.servicecomb.core.filter.Filter;
import org.apache.servicecomb.core.filter.FilterNode;
import org.apache.servicecomb.demo.edge.authentication.encrypt.Hcr;
import org.apache.servicecomb.demo.edge.service.EdgeConst;
import org.apache.servicecomb.demo.edge.service.encrypt.EncryptContext;
import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.apache.servicecomb.swagger.invocation.Response;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

@Component
public class DecodeBodyFilter implements ConsumerFilter {
  private JavaType bodyType = TypeFactory.defaultInstance().constructMapType(Map.class, String.class, String[].class);

  @Override
  public int getOrder(InvocationType invocationType, String application, String serviceName) {
    return Filter.CONSUMER_LOAD_BALANCE_ORDER - 1790;
  }

  @Override
  public String getName() {
    return "test-edge-decode-body";
  }

  @Override
  public CompletableFuture<Response> onFilter(Invocation invocation, FilterNode nextNode) {
    EncryptContext encryptContext = (EncryptContext) invocation.getHandlerContext().get(EdgeConst.ENCRYPT_CONTEXT);
    if (encryptContext == null) {
      return nextNode.onFilter(invocation);
    }
    Hcr hcr = encryptContext.getHcr();

    String encodedBody = invocation.getRequestEx().getParameter("body");
    if (encodedBody == null) {
      return nextNode.onFilter(invocation);
    }

    encodedBody = encodedBody.substring(hcr.getBodyKey().length());

    try {
      Map<String, String[]> decodedBody = RestObjectMapperFactory.getRestObjectMapper()
          .readValue(encodedBody, bodyType);
      invocation.getRequestEx().getParameterMap().putAll(decodedBody);
    } catch (Throwable e) {
      // should be a meaning exception response
      return CompletableFuture.failedFuture(e);
    }
    return nextNode.onFilter(invocation);
  }
}
