/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.transport.rest.client.http;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.HttpMethod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.servicecomb.common.rest.filter.HttpClientFilter;
import io.servicecomb.foundation.common.utils.SPIServiceUtils;

public final class HttpMethodFactory {
  private static final Logger LOGGER = LoggerFactory.getLogger(HttpMethodFactory.class);

  private static Map<String, VertxHttpMethod> httpMethodMap = new HashMap<>();

  private static List<HttpClientFilter> httpClientFilters = SPIServiceUtils.getSortedService(HttpClientFilter.class);

  static {
    for (HttpClientFilter filter : httpClientFilters) {
      LOGGER.info("Found HttpClientFilter: {}.", filter.getClass().getName());
    }

    addHttpMethod(HttpMethod.GET, VertxGetMethod.INSTANCE);
    addHttpMethod(HttpMethod.POST, VertxPostMethod.INSTANCE);
    addHttpMethod(HttpMethod.PUT, VertxPutMethod.INSTANCE);
    addHttpMethod(HttpMethod.DELETE, VertxDeleteMethod.INSTANCE);
  }

  static void addHttpMethod(String httpMethod, VertxHttpMethod instance) {
    instance.setHttpClientFilters(httpClientFilters);
    httpMethodMap.put(httpMethod, instance);
  }

  private HttpMethodFactory() {
  }

  public static VertxHttpMethod findHttpMethodInstance(String method) throws Exception {
    VertxHttpMethod httpMethod = httpMethodMap.get(method);
    if (httpMethod == null) {
      throw new Exception(String.format("Http method %s is not supported", method));
    }

    return httpMethod;
  }
}
