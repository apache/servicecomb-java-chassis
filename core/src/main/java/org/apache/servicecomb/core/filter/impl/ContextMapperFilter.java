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
package org.apache.servicecomb.core.filter.impl;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.core.CoreConst;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.filter.AbstractFilter;
import org.apache.servicecomb.core.filter.EdgeFilter;
import org.apache.servicecomb.core.filter.FilterNode;
import org.apache.servicecomb.core.filter.ProviderFilter;
import org.apache.servicecomb.core.governance.MatchType;
import org.apache.servicecomb.governance.handler.MapperHandler;
import org.apache.servicecomb.governance.marker.GovernanceRequestExtractor;
import org.apache.servicecomb.governance.processor.mapping.Mapper;
import org.apache.servicecomb.swagger.invocation.Response;
import org.springframework.util.CollectionUtils;

public class ContextMapperFilter extends AbstractFilter implements ProviderFilter, EdgeFilter {
  private final MapperHandler mapperHandler;

  public ContextMapperFilter(MapperHandler mapperHandler) {
    this.mapperHandler = mapperHandler;
  }

  @Override
  public boolean enabledForTransport(String transport) {
    return CoreConst.RESTFUL.equals(transport);
  }

  @Override
  public int getOrder() {
    return -1995;
  }

  @Override
  public String getName() {
    return "context-mapper";
  }

  @Override
  public CompletableFuture<Response> onFilter(Invocation invocation, FilterNode nextNode) {
    GovernanceRequestExtractor request = MatchType.createGovHttpRequest(invocation);
    Mapper mapper = mapperHandler.getActuator(request);
    if (mapper == null || CollectionUtils.isEmpty(mapper.target())) {
      return nextNode.onFilter(invocation);
    }
    Map<String, String> properties = mapper.target();
    properties.forEach((k, v) -> {
      if (StringUtils.isEmpty(v)) {
        return;
      }
      if ("$U".equals(v)) {
        invocation.addContext(k, request.apiPath());
      } else if ("$M".equals(v)) {
        invocation.addContext(k, request.method());
      } else if (v.startsWith("$H{") && v.endsWith("}")) {
        invocation.addContext(k, request.header(v.substring(3, v.length() - 1)));
      } else if (v.startsWith("$Q{") && v.endsWith("}")) {
        invocation.addContext(k, request.query(v.substring(3, v.length() - 1)));
      } else {
        invocation.addContext(k, v);
      }
    });
    return nextNode.onFilter(invocation);
  }
}
