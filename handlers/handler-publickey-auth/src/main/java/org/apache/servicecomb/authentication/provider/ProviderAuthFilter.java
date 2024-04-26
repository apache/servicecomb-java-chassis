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
package org.apache.servicecomb.authentication.provider;

import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.core.CoreConst;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.filter.AbstractFilter;
import org.apache.servicecomb.core.filter.Filter;
import org.apache.servicecomb.core.filter.FilterNode;
import org.apache.servicecomb.core.filter.ProviderFilter;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import jakarta.ws.rs.core.Response.Status;

public class ProviderAuthFilter extends AbstractFilter implements ProviderFilter {
  private static final String KEY_API_WHITE_LIST = "servicecomb.publicKey.accessControl.excludePathPatterns";

  private ProviderTokenManager authenticationTokenManager;

  private Environment env;

  @Autowired
  public void setProviderTokenManager(ProviderTokenManager providerTokenManager, Environment env) {
    this.authenticationTokenManager = providerTokenManager;
    this.env = env;
  }

  @Override
  public int getOrder() {
    return Filter.PROVIDER_SCHEDULE_FILTER_ORDER + 1010;
  }

  @Override
  public String getName() {
    return "provider-public-key";
  }

  @Override
  public CompletableFuture<Response> onFilter(Invocation invocation, FilterNode nextNode) {
    if (checkPathWhitelist(invocation.getRequestEx().getServletPath())) {
      return nextNode.onFilter(invocation);
    }
    String token = invocation.getContext(CoreConst.AUTH_TOKEN);
    if (null != token && authenticationTokenManager.valid(token)) {
      return nextNode.onFilter(invocation);
    }
    return CompletableFuture.failedFuture(
        new InvocationException(Status.UNAUTHORIZED, "public key authorization failed."));
  }

  private boolean checkPathWhitelist(String path) {
    String apiWhiteList = env.getProperty(KEY_API_WHITE_LIST, "");
    if (StringUtils.isEmpty(apiWhiteList)) {
      return false;
    }
    for (String whiteUri : apiWhiteList.split(",")) {
      if (!whiteUri.isEmpty() && isPatternMatch(path, whiteUri)) {
        return true;
      }
    }
    return false;
  }

  public static boolean isPatternMatch(String value, String pattern) {
    if (pattern.startsWith("*") || pattern.startsWith("/*")) {
      int index = 0;
      for (int i = 0; i < pattern.length(); i++) {
        if (pattern.charAt(i) != '*' && pattern.charAt(i) != '/') {
          break;
        }
        index++;
      }
      return value.endsWith(pattern.substring(index));
    }
    if (pattern.endsWith("*")) {
      return value.startsWith(pattern.substring(0, pattern.length() - 1));
    }
    return value.equals(pattern);
  }
}
