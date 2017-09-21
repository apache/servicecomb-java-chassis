/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.provider.springmvc.reference;

import org.springframework.http.HttpHeaders;

import io.servicecomb.common.rest.RestConst;
import io.servicecomb.common.rest.filter.HttpClientFilter;
import io.servicecomb.core.Invocation;
import io.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import io.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import io.servicecomb.swagger.invocation.Response;

public class RestTemplateCopyHeaderFilter implements HttpClientFilter {
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
        requestEx.addHeader(key, value);
      }
    });
  }

  @Override
  public Response afterReceiveResponse(Invocation invocation, HttpServletResponseEx responseEx) {
    return null;
  }
}
