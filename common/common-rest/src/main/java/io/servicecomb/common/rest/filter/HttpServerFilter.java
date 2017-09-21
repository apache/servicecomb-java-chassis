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

package io.servicecomb.common.rest.filter;

import io.servicecomb.core.Invocation;
import io.servicecomb.core.definition.OperationMeta;
import io.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import io.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import io.servicecomb.swagger.invocation.Response;

public interface HttpServerFilter {
  int getOrder();

  default boolean needCacheRequest(OperationMeta operationMeta) {
    return false;
  }

  // if finished, then return a none null response
  // if return a null response, then sdk will call next filter.afterReceiveRequest
  Response afterReceiveRequest(Invocation invocation, HttpServletRequestEx requestEx);

  // invocation maybe null
  void beforeSendResponse(Invocation invocation, HttpServletResponseEx responseEx);
}
