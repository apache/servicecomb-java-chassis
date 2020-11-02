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

package org.apache.servicecomb.config.center.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.servicecomb.config.center.client.exception.OperationException;
import org.apache.servicecomb.config.center.client.model.QueryConfigurationsRequest;
import org.apache.servicecomb.config.center.client.model.QueryConfigurationsResponse;
import org.apache.servicecomb.http.client.common.HttpRequest;
import org.apache.servicecomb.http.client.common.HttpResponse;
import org.apache.servicecomb.http.client.common.HttpTransport;
import org.apache.servicecomb.http.client.common.HttpUtils;

import com.fasterxml.jackson.core.type.TypeReference;

public class ConfigCenterClient implements ConfigCenterOperation {
  public static final String DEFAULT_APP_SEPARATOR = "@";

  public static final String DEFAULT_SERVICE_SEPARATOR = "#";

  public static final String REVISION = "revision";

  public static final String APPLICATION_CONFIG = "application";

  private HttpTransport httpTransport;

  private AddressManager addressManager;

  public ConfigCenterClient(AddressManager addressManager, HttpTransport httpTransport) {
    this.addressManager = addressManager;
    this.httpTransport = httpTransport;
  }

  @Override
  public QueryConfigurationsResponse queryConfigurations(QueryConfigurationsRequest request) {
    String dimensionsInfo = buildDimensionsInfo(request, true);
    QueryConfigurationsResponse queryConfigurationsResponse = new QueryConfigurationsResponse();

    Map<String, Object> configurations = new HashMap<>();
    try {
      Map<String, String> headers = new HashMap<>();
      headers.put("x-environment", request.getEnvironment());
      HttpRequest httpRequest = new HttpRequest(addressManager.address() + "/configuration/items?dimensionsInfo="
          + HttpUtils.encodeURLParam(dimensionsInfo) + "&revision=" + request.getRevision(), headers, null,
          HttpRequest.GET);

      HttpResponse httpResponse = httpTransport.doRequest(httpRequest);
      if (httpResponse.getStatusCode() == HttpStatus.SC_OK) {
        Map<String, Map<String, Object>> allConfigMap = HttpUtils.deserialize(
            httpResponse.getContent(),
            new TypeReference<Map<String, Map<String, Object>>>() {
            });

        if (allConfigMap.get(REVISION) != null) {
          queryConfigurationsResponse.setRevision((String) allConfigMap.get(REVISION).get("version"));
        }

        if (allConfigMap.get(APPLICATION_CONFIG) != null) {
          configurations.putAll(allConfigMap.get(APPLICATION_CONFIG));
        }

        if (allConfigMap.get(buildDimensionsInfo(request, false)) != null) {
          configurations.putAll(allConfigMap.get(buildDimensionsInfo(request, false)));
        }

        if (allConfigMap.get(dimensionsInfo) != null) {
          configurations.putAll(allConfigMap.get(dimensionsInfo));
        }
        queryConfigurationsResponse.setConfigurations(configurations);
        queryConfigurationsResponse.setChanged(true);
        return queryConfigurationsResponse;
      } else if (httpResponse.getStatusCode() == HttpStatus.SC_NOT_MODIFIED) {
        queryConfigurationsResponse.setChanged(false);
        return queryConfigurationsResponse;
      } else if (httpResponse.getStatusCode() == HttpStatus.SC_BAD_REQUEST) {
        throw new OperationException("Bad request for query configurations.");
      } else {
        throw new OperationException(
            "read response failed. status:"
                + httpResponse.getStatusCode()
                + "; message:"
                + httpResponse.getMessage()
                + "; content:"
                + httpResponse.getContent());
      }
    } catch (IOException e) {
      addressManager.nextAddress();
      throw new OperationException("", e);
    }
  }

  private String buildDimensionsInfo(QueryConfigurationsRequest request, boolean withVersion) {
    String result =
        request.getServiceName() + DEFAULT_APP_SEPARATOR
            + request.getApplication();
    if (withVersion && !StringUtils.isEmpty(request.getVersion())) {
      result = result + DEFAULT_SERVICE_SEPARATOR + request
          .getVersion();
    }
    return result;
  }
}
