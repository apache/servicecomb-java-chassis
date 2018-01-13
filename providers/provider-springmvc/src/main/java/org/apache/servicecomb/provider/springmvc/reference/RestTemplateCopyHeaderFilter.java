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

package org.apache.servicecomb.provider.springmvc.reference;

import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.common.rest.filter.HttpClientFilter;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import org.apache.servicecomb.swagger.invocation.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;

public class RestTemplateCopyHeaderFilter implements HttpClientFilter {
  private static final Logger LOGGER = LoggerFactory.getLogger(RestTemplateCopyHeaderFilter.class);

  @Override
  public int getOrder() {
    return Integer.MIN_VALUE;
  }

  @Override
  public void beforeSendRequest(Invocation invocation, HttpServletRequestEx requestEx) {
    HttpHeaders httpHeaders = (HttpHeaders) invocation.getHandlerContext().get(RestConst.CONSUMER_HEADER);
    if (httpHeaders == null) {
      return;
    }

    httpHeaders.forEach((key, values) -> {
      for (String value : values) {
        // null args should not be set to requestEx to avoid NullPointerException in Netty.
        if (null == value) {
          LOGGER.debug("header value is null, key = [{}]. Will not set this header into request", key);
          continue;
        }
        requestEx.addHeader(key, value);
      }
    });
  }

  @Override
  public Response afterReceiveResponse(Invocation invocation, HttpServletResponseEx responseEx) {
    return null;
  }
}
