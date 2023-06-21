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

package org.apache.servicecomb.core.governance;

import java.util.Map;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.governance.marker.GovernanceRequestExtractor;
import org.apache.servicecomb.swagger.SwaggerUtils;

public final class MatchType {
  private static class GovernanceRequestExtractorImpl implements GovernanceRequestExtractor {
    private final Invocation invocation;

    private GovernanceRequestExtractorImpl(Invocation invocation) {
      this.invocation = invocation;
    }

    @Override
    public String apiPath() {
      if (MatchType.REST.equalsIgnoreCase(invocation.getOperationMeta().getConfig().getGovernanceMatchType())) {
        if (invocation.isConsumer()) {
          return concatAbsolutePath(SwaggerUtils.getBasePath(invocation.getSchemaMeta().getSwagger()),
              invocation.getOperationMeta().getOperationPath());
        }
        // not highway
        if (invocation.getRequestEx() != null) {
          return invocation.getRequestEx().getRequestURI();
        }
      }

      if (invocation.isConsumer()) {
        return invocation.getOperationMeta().getMicroserviceQualifiedName();
      }
      return invocation.getOperationMeta().getSchemaQualifiedName();
    }

    @Override
    public String method() {
      return invocation.getOperationMeta().getHttpMethod();
    }

    @Override
    public String header(String key) {
      Map<String, Object> arguments = invocation.getSwaggerArguments();
      if (arguments != null && arguments.get(key) != null) {
        return arguments.get(key).toString();
      }

      if (invocation.getContext(key) != null) {
        return invocation.getContext(key);
      }

      if (invocation.getRequestEx() != null) {
        return invocation.getRequestEx().getHeader(key);
      }

      return null;
    }

    @Override
    public String instanceId() {
      if (invocation.isConsumer()) {
        if (invocation.getEndpoint() != null && invocation.getEndpoint().getMicroserviceInstance() != null) {
          return invocation.getEndpoint().getMicroserviceInstance().getInstanceId();
        }
      }
      return null;
    }

    @Override
    public String serviceName() {
      if (invocation.isConsumer()) {
        return invocation.getMicroserviceName();
      }
      return null;
    }

    @Override
    public Object sourceRequest() {
      return invocation;
    }
  }

  public static final String REST = "rest";

  public static final String RPC = "rpc";

  public static GovernanceRequestExtractor createGovHttpRequest(Invocation invocation) {
    return new GovernanceRequestExtractorImpl(invocation);
  }

  /**
   * Concat the two paths to an absolute path, without end of '/'.
   *
   * e.g. "/" + "/ope" = /ope
   * e.g. "/prefix" + "/ope" = /prefix/ope
   */
  private static String concatAbsolutePath(String basePath, String operationPath) {
    return ("/" + nonNullify(basePath) + "/" + nonNullify(operationPath))
        .replaceAll("/{2,}", "/");
  }

  private static String nonNullify(String path) {
    return path == null ? "" : path;
  }
}
